package com.gigforce.app.modules.gigPage2.repositories

import android.location.Location
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.modules.gigPage2.models.Gig
import com.gigforce.app.modules.gigPage2.models.GigStatus
import com.gigforce.app.modules.gigPage2.models.JobProfileFull
import com.gigforce.app.modules.userLocationCapture.models.UserLocation
import com.gigforce.app.utils.addOrThrow
import com.gigforce.app.utils.getOrThrow
import com.gigforce.app.utils.updateOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    open fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    private val userLocationCollectionRef: CollectionReference by lazy {
        db.collection("UserLocations")
    }

    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }

    suspend fun markCheckIn(
            gigId: String,
            location: Location,
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
                    "attendance.checkInLat" to location.latitude,
                    "attendance.checkInLong" to location.longitude,
                    "attendance.checkInLocationFake" to location.isFromMockProvider,
                    "attendance.checkInGeoPoint" to GeoPoint(location.latitude,location.longitude),
                    "attendance.checkInMarked" to true,
                    "attendance.checkInTime" to checkInTime,
                    "gigStatus" to GigStatus.ONGOING.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkInAddress" to locationPhysicalAddress,
                    "attendance.checkInImage" to image,
                    "attendance.checkInLat" to location.latitude,
                    "attendance.checkInLong" to location.longitude,
                    "attendance.checkInLocationFake" to location.isFromMockProvider,
                    "attendance.checkInGeoPoint" to GeoPoint(location.latitude,location.longitude),
                    "attendance.checkInMarked" to true,
                    "attendance.checkInTime" to checkInTime,
                    "regularisationRequest.requestedOn" to Timestamp.now(),
                    "regularisationRequest.regularisationSettled" to false,
                    "regularisationRequest.checkInTimeAccToUser" to checkInTimeAccToUser,
                    "regularisationRequest.checkOutTimeAccToUser" to null,
                    "regularisationRequest.remarksFromUser" to remarks,
                    "regularisationRequest.remarksFromManager" to null,
                    "gigStatus" to GigStatus.ONGOING.getStatusString()
            )
        }

        getCollectionReference()
                .document(gigId)
                .updateOrThrow(attendanceUpdateMap)

        submitUserLocationInTrackingCollection(
                geoPoint = GeoPoint(location.latitude, location.longitude),
                couldBeAFakeLocation = location.isFromMockProvider,
                locationAccuracy = location.accuracy,
                userName = null,
                gigId = gigId,
                fullAddressFromGps = locationPhysicalAddress
        )
    }

    suspend fun markCheckOut(
            gigId: String,
            location : Location,
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
                    "attendance.checkOutLat" to location.latitude,
                    "attendance.checkOutLong" to location.longitude,
                    "attendance.checkOutLocationFake" to location.isFromMockProvider,
                    "attendance.checkOutGeoPoint" to GeoPoint(location.latitude,location.longitude),
                    "attendance.checkOutMarked" to true,
                    "attendance.checkOutTime" to checkOutTime,
                    "gigStatus" to GigStatus.COMPLETED.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkOutAddress" to locationPhysicalAddress,
                    "attendance.checkOutImage" to image,
                    "attendance.checkOutLat" to location.latitude,
                    "attendance.checkOutLong" to location.longitude,
                    "attendance.checkOutLocationFake" to location.isFromMockProvider,
                    "attendance.checkOutGeoPoint" to GeoPoint(location.latitude,location.longitude),
                    "attendance.checkOutMarked" to true,
                    "attendance.checkOutTime" to checkOutTime,
                    "regularisationRequest.requestedOn" to Timestamp.now(),
                    "regularisationRequest.regularisationSettled" to false,
                    "regularisationRequest.checkOutTimeAccToUser" to checkOutTimeAccToUser,
                    "regularisationRequest.remarksFromUser" to remarks,
                    "regularisationRequest.remarksFromManager" to null,
                    "gigStatus" to GigStatus.COMPLETED.getStatusString()
            )
        }

        getCollectionReference()
                .document(gigId)
                .updateOrThrow(attendanceUpdateMap)

        submitUserLocationInTrackingCollection(
                geoPoint = GeoPoint(location.latitude, location.longitude),
                couldBeAFakeLocation = location.isFromMockProvider,
                locationAccuracy = location.accuracy,
                userName = null,
                gigId = gigId,
                fullAddressFromGps = locationPhysicalAddress
        )
    }

    private suspend fun submitUserLocationInTrackingCollection(
            geoPoint: GeoPoint,
            couldBeAFakeLocation: Boolean,
            locationAccuracy: Float,
            fullAddressFromGps : String,
            userName: String?,
            gigId: String?
    ) {

        try {
            userLocationCollectionRef
                    .addOrThrow(UserLocation(
                            location = geoPoint,
                            fakeLocation = couldBeAFakeLocation,
                            locationAccuracy = locationAccuracy,
                            locationCapturedTime = Timestamp.now(),
                            uid = getUID(),
                            userName = userName,
                            userPhoneNumber = user?.phoneNumber,
                            gigId = gigId,
                            fullAddressFromGps = fullAddressFromGps
                    ))
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
                getCurrentUserGigs()
                        .whereGreaterThan("startDateTime", dateFull)
                        .getOrThrow()

        val tomorrow = date.plusDays(1)
        return extractGigs(querySnap)
                .filter {
                    it.startDateTime > Timestamp.now()
                            && it.endDateTime.toLocalDate().isBefore(tomorrow)
                }

    }

    suspend fun getOngoingAndUpcomingGigsFor(
            date: LocalDate
    ): List<Gig> {

        val dateStart = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val nextDay = date.plusDays(1)
        val dateEnd = Date.from(nextDay.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val querySnap =
                getCurrentUserGigs()
                        .whereGreaterThan("startDateTime", dateStart)
                        .whereLessThan("startDateTime", dateEnd)
                        .getOrThrow()

        val currentTime = LocalDateTime.now()
        return extractGigs(querySnap)
                .filter {
                    it.endDateTime.toLocalDateTime().isAfter(currentTime)
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
                .get()
                .await()

        return getJobProfileQuery.toObject(JobProfileFull::class.java)!!
    }

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}