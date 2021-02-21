package com.gigforce.app.modules.gigPage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.gigPage.models.GigRegularisationRequest
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.*
import com.gigforce.core.utils.EventLogs.getOrThrow
import com.gigforce.core.utils.EventLogs.setOrThrow
import com.gigforce.core.utils.EventLogs.updateOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GigViewModel constructor(
        private val gigsRepository: GigsRepository = GigsRepository(),
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private var mWatchUpcomingRepoRegistration: ListenerRegistration? = null
    private var mWatchSingleGigRegistration: ListenerRegistration? = null
    private var mWatchTodaysGigRegistration: ListenerRegistration? = null

    var currentGig: Gig? = null

    private val _upcomingGigs = MutableLiveData<Lce<List<Gig>>>()
    val upcomingGigs: LiveData<Lce<List<Gig>>> get() = _upcomingGigs

    fun watchUpcomingGigs() {
        _upcomingGigs.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
                .getCurrentUserGigs()
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if (querySnapshot != null) {
                        extractUpcomingGigs(querySnapshot)
                    } else {
                        _upcomingGigs.value = Lce.error(firebaseFirestoreException!!.message!!)
                    }
                }
    }


    fun markAttendance(markAttendance: GigAttendance, gigId: String) {
        gigsRepository.markAttendance(markAttendance, gigId)
    }

    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot) {
        val userGigs: MutableList<Gig> = extractGigs(querySnapshot)

        val currentDate = Date()
        val upcomingGigs = userGigs.filter {

            if (it.endDateTime != null) {
                it.endDateTime!!.toDate().time > currentDate.time && !it.isCheckInAndCheckOutMarked()
            } else {
                it.startDateTime!!.toDate().time > currentDate.time && !it.isCheckInAndCheckOutMarked()
            }
        }.sortedBy {
            it.startDateTime!!.seconds
        }
        _upcomingGigs.value = Lce.content(upcomingGigs)
    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        val userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }
        return userGigs
    }


    /**
     * Specific Gig
     */

    private val _gigDetails = MutableLiveData<Lce<Gig>>()
    val gigDetails: LiveData<Lce<Gig>> get() = _gigDetails

    fun watchGig(gigId: String, shouldConvertToDownloadLink: Boolean = false) {
        _gigDetails.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
                .getCollectionReference()
                .document(gigId)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                    if (documentSnapshot != null) {
                        extractGigData(documentSnapshot)
                    } else {
                        _gigDetails.value = Lce.error(firebaseFirestoreException!!.message!!)
                    }
                }
    }

    private fun extractGigData(documentSnapshot: DocumentSnapshot) = viewModelScope.launch {
        runCatching {
            val gig = documentSnapshot.toObject(Gig::class.java) ?: throw IllegalArgumentException()
            gig.gigId = documentSnapshot.id
            currentGig = gig

            val gigAttachmentWithLinks = gig.gigUserFeedbackAttachments.map {
                getDownloadLinkFor("gig_feedback_images", it)
            }
            gig.gigUserFeedbackAttachments = gigAttachmentWithLinks
            gig
        }.onSuccess {
            _gigDetails.value = Lce.content(it)
        }.onFailure {
            it.message?.let { it1->_gigDetails.value =  Lce.error(it1) }
        }
    }

    suspend fun getDownloadLinkFor(folder: String, file: String) =
            suspendCoroutine<String> { cont ->
                firebaseStorage
                        .getReference(folder)
                        .child(file)
                        .downloadUrl
                        .addOnSuccessListener {
                            cont.resume(it.toString())
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }


    fun getGig(gigId: String) {
        _gigDetails.value = Lce.loading()
        gigsRepository
                .getCollectionReference()
                .document(gigId)
                .get()
                .addOnSuccessListener { snap ->

                    if (snap != null) {
                        extractGigData(snap)
                    }
                }
                .addOnFailureListener {
                    _gigDetails.value = Lce.error(it.message!!)
                }

    }

    suspend fun getGigNow(gigId: String) = suspendCoroutine<Gig> { cont ->
        gigsRepository
                .getCollectionReference()
                .document(gigId)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    if (documentSnapshot != null) {
                        val gig = documentSnapshot.toObject(Gig::class.java)
                                ?: throw IllegalArgumentException()
                        gig.gigId = documentSnapshot.id
                        cont.resume(gig)
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }

    fun favoriteGig(gigId: String) {
        gigsRepository.getCollectionReference()
                .document(gigId)
                .update("isFavourite", true)
    }

    fun unFavoriteGig(gigId: String) {
        gigsRepository.getCollectionReference()
                .document(gigId)
                .update("isFavourite", false)

    }

    override fun onCleared() {
        super.onCleared()
        mWatchUpcomingRepoRegistration?.remove()
        mWatchSingleGigRegistration?.remove()
        mWatchTodaysGigRegistration?.remove()
    }


    private val _submitGigRatingState = MutableLiveData<Lse>()
    val submitGigRatingState: LiveData<Lse> get() = _submitGigRatingState

    fun submitGigFeedback(
            gigId: String,
            rating: Float,
            feedback: String,
            files: List<Uri>
    ) = viewModelScope.launch {
        _submitGigRatingState.value = Lse.loading()

        try {
            val gig = gigsRepository.getGig(gigId)
            gig.gigRating = rating
            gig.gigUserFeedback = feedback

            gig.gigUserFeedbackAttachments = uploadFilesAndReturnNamesOnServer(files)
            gigsRepository.getCollectionReference()
                    .document(gigId)
                    .setOrThrow(gig)

            _submitGigRatingState.value = Lse.success()
        } catch (e: Exception) {
            _submitGigRatingState.value = Lse.error(e.message!!)
        }
    }


    private suspend fun uploadFilesAndReturnNamesOnServer(files: List<Uri>): List<String> {
        return files.map {
            if (it.toString().startsWith("http", true)) {
                it.lastPathSegment!!.substringAfterLast("/")
            } else {
                uploadImage(it)
            }
        }
    }


    private fun prepareUniqueImageName(): String {
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        return gigsRepository.getUID() + timeStamp + ".jpg"
    }

    private suspend fun uploadImage(image: Uri) =
            suspendCoroutine<String> { continuation ->
                val fileNameAtServer = prepareUniqueImageName()
                firebaseStorage.reference
                        .child("gig_feedback_images")
                        .child(fileNameAtServer)
                        .putFile(image)
                        .addOnSuccessListener {
                            continuation.resume(fileNameAtServer)
                        }
                        .addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
            }

    fun deleteUserFeedbackAttachment(
            gigId: String,
            attachmentToDeleteName: String
    ) {

        gigsRepository.getCollectionReference()
                .document(gigId)
                .update("gigUserFeedbackAttachments", FieldValue.arrayRemove(attachmentToDeleteName))
    }

    fun deleteUserReceivedFeedbackAttachment(
            gigId: String,
            attachmentToDeleteName: String
    ) {

        gigsRepository.getCollectionReference()
                .document(gigId)
                .update("ratingUserReceivedAttachments", FieldValue.arrayRemove(attachmentToDeleteName))
    }


    private val _declineGig = MutableLiveData<Lse>()
    val declineGig: LiveData<Lse> get() = _declineGig

    fun declineGig(gigId: String, reason: String) = viewModelScope.launch {
        _declineGig.value = Lse.loading()

        try {
            val gig = getGigNow(gigId)
            gig.declinedBy = gig.gigerId
            gig.declineReason = reason
            gig.gigerId = ""

            gigsRepository.getCollectionReference().document(gig.gigId).setOrThrow(gig)
            _declineGig.value = Lse.success()
        } catch (e: Exception) {
            _declineGig.value = Lse.error(e.message!!)
            FirebaseCrashlytics.getInstance().apply {
                log("Unable to decline gig")
                recordException(e)
            }
        }
    }

    fun declineGigs(gigIds: List<String>, reason: String) = viewModelScope.launch {
        _declineGig.value = Lse.loading()

        try {
            gigIds.forEach {

                val gig = getGigNow(it)
                gig.declinedBy = gig.gigerId
                gig.declineReason = reason
                gig.gigerId = ""

                gigsRepository.getCollectionReference().document(gig.gigId).setOrThrow(gig)
            }

            _declineGig.value = Lse.success()
        } catch (e: Exception) {
            _declineGig.value = Lse.error(e.message!!)
            FirebaseCrashlytics.getInstance().apply {
                log("Unable to decline gig")
                recordException(e)
            }
        }
    }


    private val _todaysGigs = MutableLiveData<Lce<List<Gig>>>()
    val todaysGigs: LiveData<Lce<List<Gig>>> get() = _todaysGigs

    fun startWatchingTodaysOngoingAndUpcomingGig(date: LocalDate) {
        Log.d("GigViewModel", "Started Watching gigs for $date")

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        _todaysGigs.value = Lce.loading()
        mWatchTodaysGigRegistration = gigsRepository
                .getCurrentUserGigs()
                .whereGreaterThan("startDateTime", dateFull)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    val tomorrow = date.plusDays(1)

                    if (querySnapshot != null) {
                        val todaysUpcomingGigs = extractGigs(querySnapshot).filter {
                            it.startDateTime!! > Timestamp.now() && (it.endDateTime == null || it.endDateTime!!.toLocalDate()
                                    .isBefore(tomorrow))
                        }
                        _todaysGigs.value = Lce.content(todaysUpcomingGigs)
                    } else {
                        _todaysGigs.value = Lce.error(firebaseFirestoreException!!.message!!)
                    }
                }
    }

    fun getTodaysUpcomingGig(date: LocalDate) = viewModelScope.launch {
        Log.d("GigViewModel", "getting gigs for $date")

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        _todaysGigs.value = Lce.loading()
        try {
            val querySnapshot = gigsRepository
                    .getCurrentUserGigs()
                    .whereGreaterThan("startDateTime", dateFull)
                    .getOrThrow()

            val tomorrow = date.plusDays(1)
            val todaysUpcomingGigs = extractGigs(querySnapshot).filter {
                it.startDateTime!! > Timestamp.now() && (it.endDateTime == null || it.endDateTime!!.toLocalDate()
                        .isBefore(tomorrow))
            }
            _todaysGigs.value = Lce.content(todaysUpcomingGigs)
            _todaysGigs.value = null
        } catch (e: Exception) {
            _todaysGigs.value = Lce.error(e.message!!)
            _todaysGigs.value = null
        }

    }

    private val _monthlyGigs = MutableLiveData<Lce<List<Gig>>>()
    val monthlyGigs: LiveData<Lce<List<Gig>>> get() = _monthlyGigs

    fun getGigsForMonth(companyName: String, month: Int, year: Int) = viewModelScope.launch {

        val monthStart = LocalDateTime.of(year, month, 1, 0, 0)
        val monthEnd = monthStart.plusMonths(1).withDayOfMonth(1).minusDays(1);

        try {
            _monthlyGigs.value = Lce.loading()
            val querySnap = gigsRepository
                    .getCurrentUserGigs()
//                .whereGreaterThan("startDateTime", monthStart)
//                .whereLessThan("startDateTime", monthEnd)
                    .whereEqualTo("companyName", companyName)
                    .getOrThrow()

            val gigs = extractGigs(querySnap)
            _monthlyGigs.value = Lce.content(gigs)
        } catch (e: Exception) {
            _monthlyGigs.value = Lce.error(e.message!!)
        }
    }

    private val _requestAttendanceRegularisation = MutableLiveData<Lse>()
    val requestAttendanceRegularisation: LiveData<Lse> get() = _requestAttendanceRegularisation

    fun requestRegularisation(
            gigId: String,
            punchInTime: Timestamp,
            punchOutTime: Timestamp
    ) = viewModelScope.launch {
        _requestAttendanceRegularisation.value = Lse.loading()

        try {
            val gigRegularisationRequest = GigRegularisationRequest().apply {
                checkInTime = punchInTime
                checkOutTime = punchOutTime
                requestedOn = Timestamp.now()
            }

            gigsRepository.getCollectionReference()
                    .document(gigId)
                    .updateOrThrow("regularisationRequest", gigRegularisationRequest)

            _requestAttendanceRegularisation.value = Lse.success()
        } catch (e: Exception) {
            _requestAttendanceRegularisation.value = Lse.error(e.message
                    ?: "Unable to submit regularisation attendance")
        }
    }

    private val _observableProfile: MutableLiveData<ProfileData> = MutableLiveData()
    val observableProfile: MutableLiveData<ProfileData> = _observableProfile

    fun checkIfTeamLeadersProfileExists(loginMobile: String) = viewModelScope.launch {
        checkForChatProfile(loginMobile)
    }
    suspend fun checkForChatProfile(loginMobile: String) {
        try {

            val profiles = gigsRepository.db.collection("Profiles").whereEqualTo("loginMobile", loginMobile).get().await()
            if (!profiles.documents.isNullOrEmpty()) {
                val toObject = profiles.documents[0].toObject(ProfileData::class.java)
                toObject?.id = profiles.documents[0].id
                _observableProfile.value = toObject
            }

        } catch (e: Exception) {

        }
    }

    fun getUid(): String {
        return gigsRepository.getUID()
    }


}