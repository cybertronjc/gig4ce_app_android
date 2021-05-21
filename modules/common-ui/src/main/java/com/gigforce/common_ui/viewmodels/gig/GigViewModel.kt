package com.gigforce.common_ui.viewmodels.gig

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.gigpage.models.AttendanceType
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.models.GigStatus
import com.gigforce.common_ui.repository.gig.GigerProfileFirebaseRepository
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.extensions.*
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    private val profileFirebaseRepository =
        GigerProfileFirebaseRepository()
    private var mWatchUpcomingRepoRegistration: ListenerRegistration? = null
    private var mWatchSingleGigRegistration: ListenerRegistration? = null
    private var mWatchTodaysGigRegistration: ListenerRegistration? = null

    var currentGig: Gig? = null

    private val currentUser: FirebaseUser by lazy {
        FirebaseAuth.getInstance().currentUser!!
    }

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

    private val _markingAttendanceState = MutableLiveData<Lce<AttendanceType>>()
    val markingAttendanceState: LiveData<Lce<AttendanceType>> get() = _markingAttendanceState

    fun markAttendance(
        latitude: Double,
        longitude: Double,
        locationPhysicalAddress: String,
        image: String,
        checkInTimeAccToUser: Timestamp?,
        remarks: String?
    ) = viewModelScope.launch {
        val gig = currentGig ?: return@launch

        if (!gig.isCheckInMarked()) {

            markCheckIn(
                gigId = gig.gigId,
                latitude = latitude,
                longitude = longitude,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = remarks
            )
        } else if (!gig.isCheckOutMarked()) {

            markCheckOut(
                gigId = gig.gigId,
                latitude = latitude,
                longitude = longitude,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkOutTimeAccToUser = checkInTimeAccToUser,
                remarks = remarks
            )
        } else {
            FirebaseCrashlytics.getInstance().apply {
                log("GigViewModel : Gig Id - ${gig.gigId}")
                recordException(IllegalStateException("GigViewModel : markAttendance called but check-in and checkout both are marked"))
            }
        }
    }

    private suspend fun markCheckIn(
        gigId: String,
        latitude: Double,
        longitude: Double,
        locationPhysicalAddress: String,
        image: String,
        checkInTimeAccToUser: Timestamp?,
        remarks: String?
    ) {
        _markingAttendanceState.postValue(Lce.loading())

        try {
            gigsRepository.markCheckIn(
                gigId = gigId,
                latitude = latitude,
                longitude = longitude,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkInTime = Timestamp.now(),
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = remarks
            )
            _markingAttendanceState.postValue(Lce.content(AttendanceType.CHECK_IN))
//            _markingAttendanceState.postValue(null)
        } catch (e: Exception) {
            _markingAttendanceState.postValue(Lce.error(e.toString()))
//            _markingAttendanceState.postValue(null)
        }
    }

    private suspend fun markCheckOut(
        gigId: String,
        latitude: Double,
        longitude: Double,
        locationPhysicalAddress: String,
        image: String,
        checkOutTimeAccToUser: Timestamp?,
        remarks: String?
    ) {
        _markingAttendanceState.postValue(Lce.loading())

        try {
            gigsRepository.markCheckOut(
                gigId = gigId,
                latitude = latitude,
                longitude = longitude,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkOutTime = Timestamp.now(),
                checkOutTimeAccToUser = checkOutTimeAccToUser,
                remarks = remarks
            )
            _markingAttendanceState.value = Lce.content(AttendanceType.CHECK_OUT)
//            _markingAttendanceState.value = null
        } catch (e: Exception) {
            _markingAttendanceState.postValue(Lce.error(e.toString()))
//            _markingAttendanceState.postValue(null)
        }
    }

    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot) {
        val userGigs: MutableList<Gig> = extractGigs(querySnapshot)

        val upcomingGigs = userGigs.filter {
            val gigStatus = GigStatus.fromGig(it)
            gigStatus == GigStatus.UPCOMING || gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW
        }.sortedBy {
            it.startDateTime.seconds
        }
        _upcomingGigs.value = Lce.content(upcomingGigs)
    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        return querySnapshot.documents.map { t ->
            t.toObject(Gig::class.java)!!
        }.toMutableList()
    }


    /**
     * Specific Gig
     */

    private val _gigDetails = MutableLiveData<Lce<Gig>>()
    val gigDetails: LiveData<Lce<Gig>> get() = _gigDetails

    fun watchGig(gigId: String, shouldGetContactdetails: Boolean = false) {
        _gigDetails.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
            .getCollectionReference()
            .document(gigId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot != null) {
                    extractGigData(documentSnapshot, shouldGetContactdetails)
                } else {
                    _gigDetails.value = Lce.error(firebaseFirestoreException!!.message!!)
                }
            }
    }

    fun getGigWithDetails(gigId: String) = viewModelScope.launch {
        _gigDetails.value = Lce.loading()
        Log.d("GigViewModel", "Fetching gig details ")

        try {
            val getGigQuery = gigsRepository
                .getCollectionReference()
                .document(gigId)
                .get().await()

            val gig = getGigQuery.toObject(Gig::class.java)!!
            val jobDetails = gigsRepository.getJobDetails(gig.profile.id!!)

            gig.bannerImage = jobDetails.illustrationImage

            val jobRequirementsMatch = jobDetails.info.find { it.title == "requirements" }
            if (jobRequirementsMatch != null) {
                gig.gigRequirements = jobRequirementsMatch.pointsData
            }

            val jobResponsibilitiesMatch = jobDetails.info.find { it.title == "responsibilities" }
            if (jobResponsibilitiesMatch != null) {
                gig.gigResponsibilities = jobResponsibilitiesMatch.pointsData
            }

            val jobDescriptionMatch = jobDetails.info.find { it.title == "description" }
            if (jobDescriptionMatch != null) {
                gig.description = jobDescriptionMatch.pointsData.firstOrNull() ?: ""
            }

            val jobPayoutMatch = jobDetails.info.find { it.title == "payout" }
            if (jobPayoutMatch != null) {
                gig.payoutDetails = jobPayoutMatch.pointsData.firstOrNull() ?: ""
            }

            val keywordsMatch = jobDetails.info.find { it.title == "keywords" }
            if (keywordsMatch != null) {
                gig.keywords = keywordsMatch.pointsData
            }

            Log.d("GigViewModel", "Gig : $gig")
            _gigDetails.value = Lce.content(gig)
        } catch (e: Exception) {
            Log.e("GigViewModel", "Error while retriving gig details", e)
            e.printStackTrace()
            _gigDetails.value = Lce.error(e.message!!)
        }
    }

    private fun extractGigData(
        documentSnapshot: DocumentSnapshot,
        shouldGetContactdetails: Boolean = false
    ) = viewModelScope.launch {
        runCatching {
            val gig = documentSnapshot.toObject(Gig::class.java) ?: throw IllegalArgumentException()
            gig.gigId = documentSnapshot.id
            currentGig = gig

            val gigAttachmentWithLinks = gig.gigUserFeedbackAttachments.map {
                getDownloadLinkFor("gig_feedback_images", it)
            }

            if (shouldGetContactdetails && gig.businessContact != null && gig.businessContact!!.uid != null) {

                val profile =
                    profileFirebaseRepository.getProfileDataIfExist(gig.businessContact!!.uid)
                profile?.let {
                    gig.businessContact?.profilePicture =
                        if (!it.profileAvatarThumbnail.isNullOrBlank()) {

                            try {
                                firebaseStorage.reference.child("profile_pics/${it.profileAvatarThumbnail}")
                                    .getDownloadUrlOrThrow().toString()
                            } catch (e: Exception) {
                                ""
                            }
                        } else if (profile.profileAvatarName.isNotBlank()) {

                            try {
                                firebaseStorage.reference.child("profile_pics/${it.profileAvatarName}")
                                    .getDownloadUrlOrThrow().toString()
                            } catch (e: Exception) {
                                ""
                            }
                        } else {
                            ""
                        }
                }
            }

            if (shouldGetContactdetails && gig.agencyContact != null && gig.agencyContact!!.uid != null) {

                val profile =
                    profileFirebaseRepository.getProfileDataIfExist(gig.agencyContact!!.uid)
                profile?.let {
                    gig.agencyContact?.profilePicture =
                        if (!it.profileAvatarThumbnail.isNullOrBlank()) {

                            try {
                                firebaseStorage.reference.child("profile_pics/${it.profileAvatarThumbnail}")
                                    .getDownloadUrlOrThrow().toString()
                            } catch (e: Exception) {
                                ""
                            }
                        } else if (profile.profileAvatarName.isNotBlank()) {

                            try {
                                firebaseStorage.reference.child("profile_pics/${it.profileAvatarName}")
                                    .getDownloadUrlOrThrow().toString()
                            } catch (e: Exception) {
                                ""
                            }
                        } else {
                            ""
                        }
                }
            }

            gig.gigUserFeedbackAttachments = gigAttachmentWithLinks
            gig
        }.onSuccess {
            _gigDetails.value = Lce.content(it)
        }.onFailure {
            it.message?.let { it1 -> _gigDetails.value = Lce.error(it1) }
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
            gigsRepository.getCollectionReference()
                .document(gigId)
                .updateOrThrow(
                    mapOf(
                        "gigRating" to rating,
                        "gigUserFeedback" to feedback,
                        "gigUserFeedbackAttachments" to files
                    )
                )

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

            gigsRepository
                .getCollectionReference()
                .document(gig.gigId)
                .updateOrThrow(
                    mapOf(
                        "gigStatus" to GigStatus.DECLINED.getStatusString(),
                        "declinedBy" to gig.gigerId,
                        "declineReason" to reason,
                        "declinedOn" to Timestamp.now()
                    )
                )
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
                gigsRepository
                    .getCollectionReference()
                    .document(gig.gigId)
                    .updateOrThrow(
                        mapOf(
                            "gigStatus" to GigStatus.DECLINED.getStatusString(),
                            "declinedBy" to gig.gigerId,
                            "declineReason" to reason,
                            "declinedOn" to Timestamp.now()
                        )
                    )
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
                    val todaysGigs = extractGigs(querySnapshot).filter {
                        it.endDateTime.toLocalDate().isBefore(tomorrow)
                    }
                    val upcomingAndPendingGigs = todaysGigs.filter {
                        val gigStatus = GigStatus.fromGig(it)
                        gigStatus == GigStatus.PENDING || gigStatus == GigStatus.UPCOMING
                    }

                    _todaysGigs.value = Lce.content(upcomingAndPendingGigs)
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
            val todaysGigs = extractGigs(querySnapshot).filter {
                it.endDateTime.toLocalDate().isBefore(tomorrow)
            }
            val upcomingAndPendingGigs = todaysGigs.filter {
                val gigStatus = GigStatus.fromGig(it)
                gigStatus == GigStatus.PENDING || gigStatus == GigStatus.UPCOMING
            }

            _todaysGigs.value = Lce.content(upcomingAndPendingGigs)
//            _todaysGigs.value = null
        } catch (e: Exception) {
            _todaysGigs.value = Lce.error(e.message!!)
//            _todaysGigs.value = null
        }

    }

    private val _monthlyGigs = MutableLiveData<Lce<List<Gig>>>()
    val monthlyGigs: LiveData<Lce<List<Gig>>> get() = _monthlyGigs

    fun getGigsForMonth(
        gigOrderId: String,
        month: Int,
        year: Int
    ) = viewModelScope.launch {

        val monthStart = LocalDateTime.of(year, month, 1, 0, 0)
        val monthEnd = monthStart.plusMonths(1).withDayOfMonth(1).minusDays(1)

        try {
            _monthlyGigs.value = Lce.loading()
            val querySnap = gigsRepository
                .getCurrentUserGigs()
                .whereEqualTo("gigerId", currentUser.uid)
                .whereEqualTo("gigOrderId", gigOrderId)
                .whereGreaterThan("startDateTime", monthStart.toDate)
                .whereLessThan("startDateTime", monthEnd.toDate)
                .getOrThrow()

            val gigs = extractGigs(querySnap)
            _monthlyGigs.value = Lce.content(gigs)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().apply {
                recordException(e)
            }
            e.printStackTrace()
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
//            val gigRegularisationRequest = GigRegularisationRequest().apply {
//                checkInTime = punchInTime
//                checkOutTime = punchOutTime
//                requestedOn = Timestamp.now()
//            }
//
//            gigsRepository.getCollectionReference()
//                    .document(gigId)
//                    .updateOrThrow("regularisationRequest", gigRegularisationRequest)
//
//            _requestAttendanceRegularisation.value = Lse.success()
        } catch (e: Exception) {
            _requestAttendanceRegularisation.value = Lse.error(
                e.message
                    ?: "Unable to submit regularisation attendance"
            )
        }
    }

    private val _observableProfile: MutableLiveData<ProfileData> = MutableLiveData()
    val observableProfile: MutableLiveData<ProfileData> = _observableProfile

    fun checkIfTeamLeadersProfileExists(loginMobile: String) = viewModelScope.launch {
        checkForChatProfile(loginMobile)
    }

    suspend fun checkForChatProfile(loginMobile: String) {
        try {

            val profiles =
                gigsRepository.db.collection("Profiles").whereEqualTo("loginMobile", loginMobile)
                    .get().await()
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