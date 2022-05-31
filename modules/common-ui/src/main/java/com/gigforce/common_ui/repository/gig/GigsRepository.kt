package com.gigforce.common_ui.repository.gig

import android.location.Location
import com.gigforce.app.data.remote.bodyOrThrow
import com.gigforce.app.data.repositoriesImpl.gigs.GigService
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigInfoBasicApiModel
import com.gigforce.core.datamodels.gigpage.BussinessLocation
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigOrder
import com.gigforce.core.datamodels.gigpage.JobProfileFull
import com.gigforce.core.extensions.*
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.user_tracking.models.UserGigLocationTrack
import com.gigforce.user_tracking.models.UserLocation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class GigsRepository @Inject constructor(
    private val gigService: com.gigforce.app.data.repositoriesImpl.gigs.GigService
) : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String =
        COLLECTION_NAME

    val getCurrentUserGigs: Query by lazy {
        getCollectionReference().whereEqualTo(
            "gigerId",
            getUID()
        )
    }

    private val userLocationCollectionRef: CollectionReference by lazy {
        db.collection("UserLocations")
    }

    private val profileRepository: ProfileFirebaseRepository by lazy {
        ProfileFirebaseRepository()
    }

    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }

    suspend fun markCheckIn(
        gigId: String,
        location: Location?,
        distanceBetweenGigAndUser: Float,
        locationPhysicalAddress: String,
        image: String,
        checkInTime: Timestamp,
        checkInTimeAccToUser: Timestamp? = null,
        remarks: String? = null
    ) {

        val attendanceUpdateMap: Map<String, Any?> = if (checkInTimeAccToUser == null) {
            mapOf(
                "attendance.checkInAddress" to locationPhysicalAddress,
                "attendance.checkInImage" to image,
                "attendance.checkInLat" to location?.latitude,
                "attendance.checkInLong" to location?.longitude,
                "attendance.checkInLocationAccuracy" to location?.accuracy,
                "attendance.checkInLocationFake" to location?.isFromMockProvider,
                "attendance.checkInGeoPoint" to if (location != null) GeoPoint(
                    location.latitude,
                    location.longitude
                ) else null,
                "attendance.checkInMarked" to true,
                "attendance.checkInTime" to checkInTime,
                "attendance.checkInDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                "attendance.checkInSource" to "from_gig_in_app",
                "gigStatus" to GigStatus.ONGOING.getStatusString(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        } else {
            mapOf(
                "attendance.checkInAddress" to locationPhysicalAddress,
                "attendance.checkInImage" to image,
                "attendance.checkInLat" to location?.latitude,
                "attendance.checkInLong" to location?.longitude,
                "attendance.checkInLocationAccuracy" to location?.accuracy,
                "attendance.checkInLocationFake" to location?.isFromMockProvider,
                "attendance.checkInGeoPoint" to if (location != null) GeoPoint(
                    location.latitude,
                    location.longitude
                ) else null,
                "attendance.checkInMarked" to true,
                "attendance.checkInTime" to checkInTime,
                "attendance.checkInDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                "regularisationRequest.requestedOn" to Timestamp.now(),
                "regularisationRequest.regularisationSettled" to false,
                "regularisationRequest.checkInTimeAccToUser" to checkInTimeAccToUser,
                "regularisationRequest.checkOutTimeAccToUser" to null,
                "regularisationRequest.remarksFromUser" to remarks,
                "regularisationRequest.remarksFromManager" to null,
                "attendance.checkInSource" to "from_gig_in_app",
                "gigStatus" to GigStatus.ONGOING.getStatusString(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        }

        getCollectionReference()
            .document(gigId)
            .updateOrThrow(attendanceUpdateMap)

        if (location != null) {
            submitUserLocationInTrackingCollection(
                geoPoint = GeoPoint(location.latitude, location.longitude),
                couldBeAFakeLocation = location.isFromMockProvider,
                locationAccuracy = location.accuracy,
                gigId = gigId,
                fullAddressFromGps = locationPhysicalAddress
            )
        }
    }

    suspend fun markCheckOut(
        gigId: String,
        location: Location?,
        distanceBetweenGigAndUser: Float,
        locationPhysicalAddress: String,
        image: String,
        checkOutTime: Timestamp,
        checkOutTimeAccToUser: Timestamp? = null,
        remarks: String? = null
    ) {

        val attendanceUpdateMap: Map<String, Any?> = if (checkOutTimeAccToUser == null) {
            mapOf(
                "attendance.checkOutAddress" to locationPhysicalAddress,
                "attendance.checkOutImage" to image,
                "attendance.checkOutLat" to location?.latitude,
                "attendance.checkOutLong" to location?.longitude,
                "attendance.checkOutLocationAccuracy" to location?.accuracy,
                "attendance.checkOutLocationFake" to location?.isFromMockProvider,
                "attendance.checkOutGeoPoint" to if (location != null) GeoPoint(
                    location.latitude,
                    location.longitude
                ) else null,
                "attendance.checkOutMarked" to true,
                "attendance.checkOutTime" to checkOutTime,
                "attendance.checkOutDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                "attendance.checkOutSource" to "from_gig_in_app",
                "gigStatus" to GigStatus.COMPLETED.getStatusString(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        } else {
            mapOf(
                "attendance.checkOutAddress" to locationPhysicalAddress,
                "attendance.checkOutImage" to image,
                "attendance.checkOutLat" to location?.latitude,
                "attendance.checkOutLong" to location?.longitude,
                "attendance.checkOutLocationAccuracy" to location?.accuracy,
                "attendance.checkOutLocationFake" to location?.isFromMockProvider,
                "attendance.checkOutGeoPoint" to if (location != null) GeoPoint(
                    location.latitude,
                    location.longitude
                ) else null,
                "attendance.checkOutMarked" to true,
                "attendance.checkOutTime" to checkOutTime,
                "attendance.checkOutDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                "regularisationRequest.requestedOn" to Timestamp.now(),
                "regularisationRequest.regularisationSettled" to false,
                "regularisationRequest.checkOutTimeAccToUser" to checkOutTimeAccToUser,
                "regularisationRequest.remarksFromUser" to remarks,
                "regularisationRequest.remarksFromManager" to null,
                "attendance.checkOutSource" to "from_gig_in_app",
                "gigStatus" to GigStatus.COMPLETED.getStatusString(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        }

        getCollectionReference()
            .document(gigId)
            .updateOrThrow(attendanceUpdateMap)

        if (location != null) {
            submitUserLocationInTrackingCollection(
                geoPoint = GeoPoint(location.latitude, location.longitude),
                couldBeAFakeLocation = location.isFromMockProvider,
                locationAccuracy = location.accuracy,
                gigId = gigId,
                fullAddressFromGps = locationPhysicalAddress
            )
        }
    }

    private suspend fun submitUserLocationInTrackingCollection(
        gigId: String,
        geoPoint: GeoPoint,
        couldBeAFakeLocation: Boolean,
        locationAccuracy: Float,
        fullAddressFromGps: String,
    ) {

        val profile = try {
            profileRepository.getProfileDataIfExist()
        } catch (e: Exception) {
            null
        }

        try {
            userLocationCollectionRef
                .document(gigId)
                .setOrThrow(
                    UserGigLocationTrack(
                        uid = getUID(),
                        userName = profile?.name,
                        userPhoneNumber = user?.phoneNumber,
                        locations = listOf(
                            UserLocation(
                                location = geoPoint,
                                fakeLocation = couldBeAFakeLocation,
                                locationAccuracy = locationAccuracy,
                                locationCapturedTime = Timestamp.now(),
                                fullAddressFromGps = fullAddressFromGps
                            )
                        )
                    )
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getGig(gigId: String) = suspendCoroutine<Gig> { cont ->
        getCollectionReference()
            .document(gigId)
            .get()
            .addOnSuccessListener {

                runCatching {
                    val gig = it.toObject(Gig::class.java)
                    gig?.gigId = it.id
                    gig!!
                }.onSuccess {
                    cont.resume(it)
                }.onFailure {
                    cont.resumeWithException(it)
                }
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getTodaysUpcomingGigs(date: LocalDate): List<Gig> {

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val querySnap =
            getCurrentUserGigs
                .whereGreaterThan("startDateTime", dateFull)
                .getOrThrow()

        val tomorrow = date.plusDays(1)
        return extractGigs(querySnap)
            .filter {
                it.startDateTime > Timestamp.now()
                        && it.endDateTime.toLocalDate().isBefore(tomorrow)
            }

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

    suspend fun getJobDetails(jobId: String): JobProfileFull {
        val getJobProfileQuery = db.collection("Job_Profiles")
            .document(jobId)
            .getOrThrow()

        return getJobProfileQuery.toObject(JobProfileFull::class.java)!!
    }

    suspend fun getGigLocationFromGigOrder(
        gigOrderId: String
    ): Location? {

        val gigOrder = getGigOrder(gigOrderId) ?: return null
        val officeLocation = gigOrder.workOrderOffice ?: return null
        val officeLocationId = officeLocation.id ?: return null
        val bussinessLocation = getBussinessLocation(officeLocationId) ?: return null

        return Location(
            "Office Location"
        ).apply {
            this.latitude = bussinessLocation.geoPoint?.latitude ?: 0.0
            this.longitude = bussinessLocation.geoPoint?.longitude ?: 0.0
        }
    }

    suspend fun getGigOrder(gigOrderId: String): GigOrder? {
        val getGigOrderQuery = db.collection("Gig_Order")
            .document(gigOrderId)
            .getOrThrow()

        if (!getGigOrderQuery.exists())
            return null

        return getGigOrderQuery.toObject(GigOrder::class.java)!!
    }

    private suspend fun getBussinessLocation(bussinessLocationId: String): BussinessLocation? {
        val getBussinessLocationQuery = db.collection("Business_Locations")
            .document(bussinessLocationId)
            .getOrThrow()

        if (!getBussinessLocationQuery.exists())
            return null

        return getBussinessLocationQuery.toObject(BussinessLocation::class.java)!!
    }

    suspend fun getOngoingAndUpcomingGigsFor(
        date: LocalDate
    ): List<Gig> {

        val dateStart = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val nextDay = date.plusDays(1)
        val dateEnd = Date.from(nextDay.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val querySnap =
            getCurrentUserGigs
                .whereGreaterThan("startDateTime", dateStart)
                .whereLessThan("startDateTime", dateEnd)
                .getOrThrow()

        val currentTime = LocalDateTime.now()
        return extractGigs(querySnap)
            .filter {
                it.endDateTime.toLocalDateTime().isAfter(currentTime)
            }
    }

    fun getUpcomingGigs(): Flow<List<Gig>> = flow {
        val upcomingGigs = gigService.getNext7DaysUpcomingGigs().bodyOrThrow()
        val mappedGigs = upcomingGigs.map {
            it.toGig()
        }.filter {
            !it.isCheckInAndCheckOutMarked()
        }
        emit(mappedGigs)
    }

    fun getGigsForADay(
        localDate: LocalDate
    ): Flow<List<com.gigforce.app.data.repositoriesImpl.gigs.models.GigInfoBasicApiModel>> = flow {
        val finalDateInYYMMDD = DateTimeFormatter.ISO_DATE.format(localDate)
        emit(gigService.getGigsForDate(finalDateInYYMMDD).bodyOrThrow())
    }

    suspend fun getGigDetails(
        gigId: String
    ): Gig {
       val gigDataFromApi =  gigService.getGigDetails(
            gigId
        ).bodyOrThrow()
            .first()

        val gig = gigDataFromApi.toGigModel()
        return gig
    }

    companion object {
        const val COLLECTION_NAME = "Gigs"
    }
}