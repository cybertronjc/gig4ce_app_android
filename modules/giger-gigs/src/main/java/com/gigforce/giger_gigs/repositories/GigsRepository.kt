package com.gigforce.giger_gigs.repositories

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.gigpage.GigAttendance
import com.gigforce.core.datamodels.gigpage.JobProfileFull
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.models.GigStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    open fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    fun markAttendance(markAttendance: GigAttendance, gigId: String) {
        getCollectionReference().document(gigId).update(markAttendance.tableName, markAttendance)
    }

    suspend fun markCheckIn(
            gigId: String,
            latitude: Double,
            longitude: Double,
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
                    "attendance.checkInLat" to latitude,
                    "attendance.checkInLong" to longitude,
                    "attendance.checkInMarked" to true,
                    "attendance.checkInTime" to checkInTime,
                    "gigStatus" to GigStatus.ONGOING.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkInAddress" to locationPhysicalAddress,
                    "attendance.checkInImage" to image,
                    "attendance.checkInLat" to latitude,
                    "attendance.checkInLong" to longitude,
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
    }

    suspend fun markCheckOut(
            gigId: String,
            latitude: Double,
            longitude: Double,
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
                    "attendance.checkOutLat" to latitude,
                    "attendance.checkOutLong" to longitude,
                    "attendance.checkOutMarked" to true,
                    "attendance.checkOutTime" to checkOutTime,
                    "gigStatus" to GigStatus.COMPLETED.getStatusString()
            )
        } else {
            mapOf(
                    "attendance.checkOutAddress" to locationPhysicalAddress,
                    "attendance.checkOutImage" to image,
                    "attendance.checkOutLat" to latitude,
                    "attendance.checkOutLong" to longitude,
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

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}