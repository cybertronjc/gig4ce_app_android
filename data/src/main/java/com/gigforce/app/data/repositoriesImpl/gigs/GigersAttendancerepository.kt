package com.gigforce.app.data.repositoriesImpl.gigs

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.data.remote.bodyOrThrow
import com.gigforce.app.data.repositoriesImpl.gigs.models.GetGigersAttendanceRequest
import com.gigforce.app.data.repositoriesImpl.gigs.models.GetGigersAttendanceRequestFilter
import com.gigforce.app.data.repositoriesImpl.gigs.models.GetGigersAttendanceResponse
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceRequest
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
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

    suspend fun getAttendance(
        date: LocalDate
    ): GetGigersAttendanceResponse {

        return gigerAttendanceService.getGigersAttendance(
            GetGigersAttendanceRequest(
                filter = GetGigersAttendanceRequestFilter(
                    date = date
                )
            )
        ).bodyFromBaseResponseElseThrow()
    }

    suspend fun getAttendanceMonthly(
        jobProfileId: String,
        userId : String?,
        month: Int,
        year: Int
    ): List<Gig> {
        return gigerAttendanceService.getGigOrderAttendanceMonthly(
            GigAttendanceRequest(
                month = month,
                year = year,
                jobProfileId = jobProfileId,
                gigerId = userId ?: firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
        ).bodyFromBaseResponseElseThrow()
            .map {
                it.toGig()
            }
    }


    companion object {
        const val TAG = "GigersAttendanceRepository"
    }
}