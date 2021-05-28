package com.gigforce.common_ui.repository.gig

import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.updateOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import android.location.Location
import com.gigforce.core.datamodels.gigpage.*
import com.google.firebase.firestore.GeoPoint

open class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String =
        COLLECTION_NAME

    open fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    fun markAttendance(markAttendance: GigAttendance, gigId: String) {
        getCollectionReference().document(gigId).update(markAttendance.tableName, markAttendance)
    }

    suspend fun markCheckIn(
            gigId: String,
            location : Location?,
            distanceBetweenGigAndUser : Float,
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
                    "attendance.checkInGeoPoint" to if(location != null) GeoPoint(location.latitude,location.longitude) else null,
                    "attendance.checkInMarked" to true,
                    "attendance.checkInTime" to checkInTime,
                    "attendance.checkInDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                    "gigStatus" to GigStatus.ONGOING.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkInAddress" to locationPhysicalAddress,
                    "attendance.checkInImage" to image,
                    "attendance.checkInLat" to location?.latitude,
                    "attendance.checkInLong" to location?.longitude,
                    "attendance.checkInLocationAccuracy" to location?.accuracy,
                    "attendance.checkInLocationFake" to location?.isFromMockProvider,
                    "attendance.checkInGeoPoint" to if(location != null) GeoPoint(location.latitude,location.longitude) else null,
                    "attendance.checkInMarked" to true,
                    "attendance.checkInTime" to checkInTime,
                    "attendance.checkInDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
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
    }

    suspend fun markCheckOut(
            gigId: String,
            location : Location?,
            distanceBetweenGigAndUser : Float,
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
                    "attendance.checkOutGeoPoint" to if(location != null) GeoPoint(location.latitude,location.longitude) else null,
                    "attendance.checkOutMarked" to true,
                    "attendance.checkOutTime" to checkOutTime,
                    "attendance.checkOutDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
                    "gigStatus" to GigStatus.COMPLETED.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkOutAddress" to locationPhysicalAddress,
                    "attendance.checkOutImage" to image,
                    "attendance.checkOutLat" to location?.latitude,
                    "attendance.checkOutLong" to location?.longitude,
                    "attendance.checkOutLocationAccuracy" to location?.accuracy,
                    "attendance.checkOutLocationFake" to location?.isFromMockProvider,
                    "attendance.checkOutGeoPoint" to if(location != null) GeoPoint(location.latitude,location.longitude) else null,
                    "attendance.checkOutMarked" to true,
                    "attendance.checkOutTime" to checkOutTime,
                    "attendance.checkOutDistanceBetweenGigAndUser" to distanceBetweenGigAndUser,
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
                            &&  it.endDateTime.toLocalDate().isBefore(tomorrow)
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

     suspend fun getJobDetails(jobId : String) : JobProfileFull {
        val getJobProfileQuery = db.collection("Job_Profiles")
                .document(jobId)
                .get()
                .await()

       return getJobProfileQuery.toObject(JobProfileFull::class.java)!!
    }

    suspend fun getGigLocationFromGigOrder(
            gigOrderId: String
    ) : Location? {

        val gigOrder = getGigOrder(gigOrderId) ?: return null
        val officeLocation = gigOrder.workOrderOffice ?: return null
        val officeLocationId = officeLocation.id ?: return null
        val bussinessLocation =   getBussinessLocation(officeLocationId) ?: return null

        return Location(
                "Office Location"
        ).apply {
            this.latitude = bussinessLocation.geoPoint?.latitude ?: 0.0
            this.longitude = bussinessLocation.geoPoint?.longitude ?: 0.0
        }
    }

    private suspend fun getGigOrder(gigOrderId: String): GigOrder? {
        val getGigOrderQuery = db.collection("Gig_Order")
                .document(gigOrderId)
                .get().await()

        if (!getGigOrderQuery.exists())
            return null

       return getGigOrderQuery.toObject(GigOrder::class.java)!!
    }

    private suspend fun getBussinessLocation(bussinessLocationId: String): BussinessLocation? {
        val getBussinessLocationQuery = db.collection("Business_Locations")
                .document(bussinessLocationId)
                .get().await()

        if (!getBussinessLocationQuery.exists())
            return null

       return getBussinessLocationQuery.toObject(BussinessLocation::class.java)!!
    }

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}