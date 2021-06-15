package com.gigforce.giger_gigs.repositories

import com.gigforce.common_ui.remote.GigerAttendanceService
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GigersAttendanceRepository constructor(
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuthStateListener: FirebaseAuthStateListener = FirebaseAuthStateListener.getInstance(),
    private val gigerAttendanceService: GigerAttendanceService = RetrofitFactory.createService(GigerAttendanceService::class.java)
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD


    suspend fun getAttendance(
        date: LocalDate
    ): List<GigerAttendance> {

        val loggedInUser = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
        val getGigersAttendanceResponse = gigerAttendanceService.getGigersAttendance(
            dateInYYYMMDD = /*date.format(dateFormatter)*/ "2021-02-25",
            managerLoginMobile = /*loggedInUser.phoneNumber!!*/ "+917406777383"
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


    companion object {
        const val TAG = "GigersAttendanceRepository"
    }
}