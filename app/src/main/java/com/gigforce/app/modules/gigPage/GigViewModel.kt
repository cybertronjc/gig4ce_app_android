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
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.setOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
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

    var currentGig : Gig? = null

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
                it.endDateTime!!.toDate().time > currentDate.time
            } else {

                it.startDateTime!!.toDate().time > currentDate.time
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
            _gigDetails.value = Lce.error(it.message!!)
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

    suspend fun getGigNow(gigId : String) = suspendCoroutine<Gig>{ cont ->
        gigsRepository
            .getCollectionReference()
            .document(gigId)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot != null) {
                    val gig = documentSnapshot.toObject(Gig::class.java) ?: throw IllegalArgumentException()
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

    fun declineGig(gigId: String, reason: String) = viewModelScope.launch{
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

    fun declineGigs(gigIds: List<String>, reason: String) = viewModelScope.launch{
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

    fun startWatchingTodaysOngoingAndUpcomingGig(date : LocalDate){
        Log.d("GigViewModel", "Started Watching gigs for $date")

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        _todaysGigs.value = Lce.loading()
        mWatchTodaysGigRegistration = gigsRepository
            .getCurrentUserGigs()
            .whereGreaterThan("startDateTime", dateFull)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                val tomorrow = date.plusDays(1)

                if (querySnapshot != null) {
                   val todaysUpcomingGigs =  extractGigs(querySnapshot).filter {
                       it.startDateTime!! > Timestamp.now() && ( it.endDateTime == null || it.endDateTime!!.toLocalDate().isBefore(tomorrow))
                   }
                    _todaysGigs.value = Lce.content(todaysUpcomingGigs)
                } else {
                    _upcomingGigs.value = Lce.error(firebaseFirestoreException!!.message!!)
                }
            }

    }

}