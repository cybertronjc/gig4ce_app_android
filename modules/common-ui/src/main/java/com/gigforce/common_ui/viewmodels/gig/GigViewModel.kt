package com.gigforce.common_ui.viewmodels.gig

import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.common_ui.repository.gig.GigerProfileFirebaseRepository
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigAttendance
import com.gigforce.core.datamodels.gigpage.GigOrder
import com.gigforce.core.datamodels.gigpage.models.AttendanceType
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.extensions.*
import com.gigforce.core.logger.GigforceLogger
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class GigViewModel @Inject constructor(
    private val gigsRepository: GigsRepository,
    private val firebaseStorage: FirebaseStorage,
    private val attendanceRepository: GigAttendanceRepository,
    private val logger: GigforceLogger,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val profileFirebaseRepository : GigerProfileFirebaseRepository
) : ViewModel() {

    companion object {
        const val TAG = "GigViewModel"
        const val REMOTE_CONFIG_MIN_TIME_BTW_CHECK_IN_CHECK_OUT = "min_time_btw_check_in_check_out"
    }

    private var mWatchUpcomingRepoRegistration: ListenerRegistration? = null
    private var mWatchSingleGigRegistration: ListenerRegistration? = null
    private var mWatchTodaysGigRegistration: ListenerRegistration? = null

    //SharedViewModels

    var currentGig: Gig? = null
    private lateinit var gigSharedViewModel: SharedGigViewModel


    private val _upcomingGigs = MutableLiveData<Lce<List<Gig>>>()
    val upcomingGigs: LiveData<Lce<List<Gig>>> get() = _upcomingGigs

    private val declineButtonVisibilityChangeCheckRunnable = CheckInButtonEnableCheckingRunnable()
    private val scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor by lazy {
        ScheduledThreadPoolExecutor(1)
    }

    fun setSharedGigViewModel(
        gigSharedViewModel: SharedGigViewModel
    ) = viewModelScope.launch{
        this@GigViewModel.gigSharedViewModel = gigSharedViewModel
        this@GigViewModel.gigSharedViewModel.gigSharedViewModelState.collect {

            when(it){
                is SharedGigViewState.UserDeclinedGig -> gigsDeclined(
                    gigIds = it.gigIds,
                    reason = it.reason
                )
                else -> {}
            }
        }
    }

    fun watchUpcomingGigs() {
        _upcomingGigs.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
            .getCurrentUserGigs
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (querySnapshot != null) {
                    extractUpcomingGigs(querySnapshot)
                } else {
                    _upcomingGigs.value = Lce.error(firebaseFirestoreException!!.message!!)
                }
            }
    }

    private val _markingAttendanceState = MutableLiveData<Lce<AttendanceType>?>()
    val markingAttendanceState: LiveData<Lce<AttendanceType>?> get() = _markingAttendanceState

    fun markAttendance(
        location: Location?,
        distanceBetweenGigAndUser: Float,
        locationPhysicalAddress: String,
        image: String,
        checkInTimeAccToUser: Timestamp?,
        remarks: String?
    ) = viewModelScope.launch {
        val gig = currentGig ?: return@launch

        if (!gig.isCheckInMarked()) {

            markCheckIn(
                gig = gig,
                gigId = gig.gigId,
                location = location,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = remarks,
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
            )

        } else if (!gig.isCheckOutMarked()) {

            val checkInTime = gig.attendance!!.checkInTime?.toLocalDateTime()
            val currentTime = LocalDateTime.now()
            val minutes = Duration.between(checkInTime, currentTime).toMinutes()

            val minTimeBtwCheckInCheckOut = getMinAllowedTimeBetweenCheckInAndCheckOut()
            if (minutes < minTimeBtwCheckInCheckOut) {
                Log.d(
                    "GigViewModel",
                    "Ignoring checkout call as difference between checkin-time and current time is less than 15 mins"
                )
                return@launch
            }

            markCheckOut(
                gig = gig,
                gigId = gig.gigId,
                location = location,
                locationPhysicalAddress = locationPhysicalAddress,
                image = image,
                checkOutTimeAccToUser = checkInTimeAccToUser,
                remarks = remarks,
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
            )
        } else {
            FirebaseCrashlytics.getInstance().apply {
                log("GigViewModel : Gig Id - ${gig.gigId}")
                recordException(IllegalStateException("GigViewModel : markAttendance called but check-in and checkout both are marked"))
            }
        }
    }

    private fun getMinAllowedTimeBetweenCheckInAndCheckOut(): Long {
        val minTimeBtwCheckInCheckOutString = try {
            firebaseRemoteConfig.getLong(
                REMOTE_CONFIG_MIN_TIME_BTW_CHECK_IN_CHECK_OUT
            )
        } catch (e: Exception) {
            0L
        }
        return if (minTimeBtwCheckInCheckOutString < 1L) {
            2L
        } else {
            minTimeBtwCheckInCheckOutString
        }
    }

    private suspend fun markCheckIn(
        gig: Gig,
        gigId: String,
        location: Location?,
        distanceBetweenGigAndUser: Float,
        locationPhysicalAddress: String,
        image: String,
        checkInTimeAccToUser: Timestamp?,
        remarks: String?
    ) {
        _markingAttendanceState.value = Lce.loading()

        try {
            attendanceRepository.markCheckIn(
                gigId = gigId,
                imagePathInFirebase = image,
                latitude = location?.latitude,
                longitude = location?.longitude,
                markingAddress = locationPhysicalAddress,
                locationFake = location?.isFromMockProvider,
                locationAccuracy = location?.accuracy,
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
            )
            _markingAttendanceState.value = Lce.content(AttendanceType.CHECK_IN)
            _markingAttendanceState.value = null


            val updatedGig = gig.copy(
                attendance = GigAttendance(
                    checkInMarked = true,
                    checkInTime = Date(),
                    checkInLat = location?.latitude ?: 0.0,
                    checkInLong = location?.longitude ?: 0.0,
                    checkInImage = image,
                    checkInAddress = locationPhysicalAddress
                )
            )
            currentGig = updatedGig
            _gigDetails.postValue(Lce.content(updatedGig))
            attachCheckInButtonVisibilityCheckRunnable()
        } catch (e: Exception) {
            _markingAttendanceState.value = Lce.error(e.message ?: "Unable to mark check-in")
            _markingAttendanceState.value = null
        }
    }

    private suspend fun markCheckOut(
        gig: Gig,
        gigId: String,
        location: Location?,
        distanceBetweenGigAndUser: Float,
        locationPhysicalAddress: String,
        image: String,
        checkOutTimeAccToUser: Timestamp?,
        remarks: String?
    ) {
        _markingAttendanceState.value = Lce.loading()

        try {
            attendanceRepository.markCheckOut(
                gigId = gigId,
                imagePathInFirebase = image,
                latitude = location?.latitude,
                longitude = location?.longitude,
                markingAddress = locationPhysicalAddress,
                locationFake = location?.isFromMockProvider,
                locationAccuracy = location?.accuracy,
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
            )
            _markingAttendanceState.value = Lce.content(AttendanceType.CHECK_OUT)
            _markingAttendanceState.value = null

            val updatedAttendanceItem = gig.attendance ?: GigAttendance(
                checkInMarked = true,
                checkInTime = Date(),
                checkInLat = location?.latitude ?: 0.0,
                checkInLong = location?.longitude ?: 0.0,
                checkInImage = image,
                checkInAddress = locationPhysicalAddress
            )
            updatedAttendanceItem.apply {
                setCheckout(
                    checkOutMarked = true,
                    checkOutTime = Date(),
                    checkOutLat = location?.latitude ?: 0.0,
                    checkOutLong = location?.longitude ?: 0.0,
                    checkOutImage = image,
                    checkOutAddress = locationPhysicalAddress
                )
            }
            val updatedGig = gig.copy(
                attendance = updatedAttendanceItem
            )
            currentGig = updatedGig
            _gigDetails.postValue(Lce.content(updatedGig))
        } catch (e: Exception) {
            _markingAttendanceState.value = Lce.error(e.message ?: "Unable to mark check-out")
            _markingAttendanceState.value = null
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

        val gigs = mutableListOf<Gig>()
        querySnapshot.documents.map { t ->
            try {
                t.toObject(Gig::class.java)?.let {
                    it.gigId = t.id
                    gigs.add(it)
                }
            } catch (e: Exception) {
                CrashlyticsLogger.e(
                    "GigViewModel - extractGigs",
                    "while desearializing gig data",
                    e
                )
            }
        }

        return gigs
    }


    /**
     * Specific Gig
     */

    private val _gigDetails = MutableLiveData<Lce<Gig>>()
    val gigDetails: LiveData<Lce<Gig>> get() = _gigDetails

    fun fetchGigDetails(
        gigId: String,
        shouldGetContactdetails: Boolean = false
    ) = viewModelScope.launch {
        _gigDetails.value = Lce.loading()
        logger.d(TAG, "Fetching gig details $gigId...")

        try {

            val gig = gigsRepository.getGigDetails(gigId)
            currentGig = gig

            _gigDetails.value = Lce.content(gig)
            logger.d(TAG, "[Success] gig details fetched")
        } catch (e: Exception) {

            _gigDetails.value = Lce.error(
                e.message ?: "Unable to load Gig Details"
            )
            logger.e(TAG, "[Failure] gig details fetched error", e)
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

            if (shouldGetContactdetails
                && gig.businessContact != null
                && (gig.businessContact?.uid != null || gig.businessContact?.uuid != null)
            ) {

                val businessContactUid = gig.businessContact?.uid ?: gig.businessContact?.uuid
                val profile =
                    profileFirebaseRepository.getProfileDataIfExist(businessContactUid)
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

            if (shouldGetContactdetails
                && gig.agencyContact != null
                && (gig.agencyContact?.uid != null || gig.agencyContact?.uuid != null)
            ) {

                val agencyContactUid = gig.agencyContact?.uid ?: gig.agencyContact!!.uuid
                val profile =
                    profileFirebaseRepository.getProfileDataIfExist(agencyContactUid)
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

            if (shouldGetContactdetails) {
                val location = gigsRepository.getGigLocationFromGigOrder(gig.gigOrderId)
                location?.let {

                    gig.latitude = it.latitude
                    gig.longitude = it.longitude
                }
            }

            gig.gigUserFeedbackAttachments = gigAttachmentWithLinks

            gig
        }.onSuccess {
            //gigOrder = getGigOrder(it.gigOrderId)
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

    suspend fun getGigOrder(gigorderId: String): GigOrder? {
        try {
            var myGIgOrder: GigOrder? = GigOrder()
            val await = gigsRepository.db.collection("Gig_Order")
                .document(gigorderId)
                .get().await()
            if (await.exists()) {
                myGIgOrder = await.toObject(GigOrder::class.java)
            }

            return myGIgOrder
        } catch (e: Exception) {
            return GigOrder()
        }
    }

    suspend fun getGigNow(gigId: String) = suspendCoroutine<Gig> { cont ->
        gigsRepository
            .getCollectionReference()
            .document(gigId)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot != null) {
                    if (!documentSnapshot.exists()) {
                        cont.resumeWithException(IllegalArgumentException("No gig found in db with id $gigId"))
                    } else {
                        val gig = documentSnapshot.toObject(Gig::class.java)
                            ?: throw IllegalArgumentException()
                        gig.gigId = documentSnapshot.id
                        cont.resume(gig)
                    }
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
        detachCheckInButtonVisibilityCheckRunnable()
    }


    private val _submitGigRatingState = MutableLiveData<Lse>()
    val submitGigRatingState: LiveData<Lse> get() = _submitGigRatingState

    fun submitGigFeedback(
        sharedGigViewModel: SharedGigViewModel,
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

            sharedGigViewModel.userRatedGig(
                rating,
                feedback
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

    fun declineGig(
        gigId: String,
        reason: String,
        isDeclinedByTL: Boolean
    ) = viewModelScope.launch {
        _declineGig.value = Lse.loading()

        try {
            attendanceRepository.markDecline(
                gigId = gigId,
                reasonId = reason,
                reason = reason
            )

            _declineGig.value = Lse.success()
            gigsDeclined(
                listOf(gigId),
                reason
            )
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
                attendanceRepository.markDecline(
                    gigId = gig.gigId,
                    reasonId = reason,
                    reason = reason
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


    private val _todaysGigs = MutableLiveData<Lce<List<Gig>>?>()
    val todaysGigs: LiveData<Lce<List<Gig>>?> get() = _todaysGigs

    fun startWatchingTodaysOngoingAndUpcomingGig(date: LocalDate) {
        Log.d("GigViewModel", "Started Watching gigs for $date")

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        _todaysGigs.value = Lce.loading()
        mWatchTodaysGigRegistration = gigsRepository
            .getCurrentUserGigs
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
                .getCurrentUserGigs
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
            _todaysGigs.value = null
        } catch (e: Exception) {
            _todaysGigs.value = Lce.error(e.message!!)
            _todaysGigs.value = null
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

    fun getUid(): String {
        return gigsRepository.getUID()
    }


    private inner class CheckInButtonEnableCheckingRunnable : Runnable {

        override fun run() {
            val gig = currentGig ?: return

            val checkInTime = gig.attendance!!.checkInTime?.toLocalDateTime()
            val currentTime = LocalDateTime.now()
            val minutes = Duration.between(checkInTime, currentTime).toMinutes()

            val minTimeBtwCheckInCheckOut = getMinAllowedTimeBetweenCheckInAndCheckOut()
            if (minutes < minTimeBtwCheckInCheckOut) {
                logger.d(
                    TAG,
                    "CheckInButtonEnableCheckingRunnable() - Diff between checkin and earliest checkout time - $minutes , min minutes btw checkin and checkout $minTimeBtwCheckInCheckOut mins"
                )
            } else {
                logger.d(
                    TAG,
                    "CheckInButtonEnableCheckingRunnable() - Enabling checkout button..."
                )

                _gigDetails.postValue(Lce.content(gig))
                detachCheckInButtonVisibilityCheckRunnable()
            }
        }
    }

    private fun attachCheckInButtonVisibilityCheckRunnable() {
        logger.d(
            TAG,
            "attaching declineButtonVisibilityChangeCheckRunnable..."
        )

        try {
            scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                declineButtonVisibilityChangeCheckRunnable,
                1L,
                1L,
                TimeUnit.MINUTES
            )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "Unable to detach checkRu runnable",
                e
            )
        }

    }

    private fun detachCheckInButtonVisibilityCheckRunnable() {

        try {
            logger.d(
                TAG,
                "detaching declineButtonVisibilityChangeCheckRunnable..."
            )

            scheduledThreadPoolExecutor.remove(declineButtonVisibilityChangeCheckRunnable)
            scheduledThreadPoolExecutor.purge()
        } catch (e: Exception) {
            logger.e(
                TAG,
                "Unable to detach detachCheckInButtonVisibilityCheckRunnable runnable",
                e
            )
        }
    }

    fun userRatedTheGig(rating: Float, feedback: String?) {
        val gig = currentGig ?: return
        gig.gigUserFeedback = feedback
        gig.gigRating = rating
        _gigDetails.postValue(Lce.content(gig))
    }

    private fun gigsDeclined(
        gigIds :List<String>,
        reason: String
    ){

        if (currentGig?.gigId != null && gigIds.contains(currentGig!!.gigId)) {

            val gig = currentGig ?: return
            val updatedGig = gig.copy(
                gigStatus = GigStatus.DECLINED.getStatusString(),
                declineReason = reason
            )
            currentGig= updatedGig
            _gigDetails.postValue(Lce.content(updatedGig))
        }
    }
}