package com.gigforce.giger_gigs.repositories

import com.gigforce.common_ui.remote.GigerAttendanceService
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GigersAttendanceRepository constructor(
    private val iBuildConfigVM: IBuildConfigVM,
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuthStateListener: FirebaseAuthStateListener = FirebaseAuthStateListener.getInstance(),
    private val gigerAttendanceService: GigerAttendanceService = RetrofitFactory.createService(
        GigerAttendanceService::class.java
    )
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

    private val gigsCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(GigsRepository.COLLECTION_NAME)
    }

    suspend fun getAttendance(
        date: LocalDate
    ): List<GigerAttendance> {

        val loggedInUser = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
        val getGigersAttendanceResponse = gigerAttendanceService.getGigersAttendance(
            iBuildConfigVM.getGigersUnderTlUrl(),
            dateInYYYMMDD = date.format(dateFormatter)/*"2021-02-25"*/,
            managerLoginMobile = loggedInUser.phoneNumber!! /*+917406777383*/
        )

        if (getGigersAttendanceResponse.isSuccessful) {
            return getGigersAttendanceResponse.body()!!
        } else {
            CrashlyticsLogger.e(
                TAG,
                "fetching gigers attendance with params, date=${date.format(dateFormatter)}, managerLoginMobile=${loggedInUser.phoneNumber}",
                Exception(getGigersAttendanceResponse.message())
            )

            throw Exception("Unable to fetch users attendance")
        }
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
            )
        )


    suspend fun markUserAttendanceAsGigDeclined(
        gigId: String,
        reason: String
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
                "attendance.checkInMarked" to false,
                "attendance.checkInDistanceBetweenGigAndUser" to -1.0f,
                "attendance.checkInSource" to "tl_app",
                "attendance.checkInDataOverWrittenBy" to "tl_app",
                "gigStatus" to GigStatus.DECLINED.getStatusString(),
                "declinedBy" to firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                "declineReason" to reason
            )
        )


    companion object {
        const val TAG = "GigersAttendanceRepository"
    }
}