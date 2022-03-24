package com.gigforce.giger_gigs.repositories

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.GigerAttendanceService
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.gig.GigAttendanceRequest
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GigersAttendanceRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val gigerAttendanceService: GigerAttendanceService
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

    private val gigsCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(GigsRepository.COLLECTION_NAME)
    }

    suspend fun getAttendance(
        date: LocalDate
    ): List<GigAttendanceApiModel> {

        return gigerAttendanceService.getGigersAttendance(
            dateInYYYMMDD = date.format(dateFormatter)
        ).bodyOrThrow()
    }


    suspend fun markUserAttendanceAsPresent(
        gigId: String
    ) = gigsCollectionRef.document(gigId)
        .updateOrThrow(
            mapOf(
                "attendance.checkInAddress" to "",
                "attendance.checkInImage" to null,
                "attendance.checkInLat" to null,
                "attendance.checkInLong" to null,
                "attendance.checkInLocationAccuracy" to null,
                "attendance.checkInLocationFake" to false,
                "attendance.checkInGeoPoint" to null,
                "attendance.checkInMarked" to true,
                "attendance.checkInTime" to Timestamp.now(),
                "attendance.checkInDistanceBetweenGigAndUser" to -1.0f,
                "attendance.checkInSource" to "from_tl_attendance",
                "attendance.checkInDoneByTL" to firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                "gigStatus" to GigStatus.ONGOING.getStatusString(),
                "declinedBy" to FieldValue.delete(),
                "declineReason" to FieldValue.delete(),
                "declinedOn" to FieldValue.delete(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        )

    suspend fun getAttendanceMonthly(
        gigOrderId : String,
        month : Int,
        year : Int
    ) : List<Gig>{
       return gigerAttendanceService.getGigOrderAttendanceMonthly(
            GigAttendanceRequest(
                month = month,
                year = year,
                gigOrderId = gigOrderId
            )
        ).bodyOrThrow().map { it.toGigModel()}
    }


    companion object {
        const val TAG = "GigersAttendanceRepository"
    }
}