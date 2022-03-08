package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.gig.GigApiModel
import com.gigforce.common_ui.viewdatamodels.gig.GigAttendanceRequest
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import retrofit2.Response
import retrofit2.http.*

interface GigerAttendanceService {

    @GET("gigs/tlwise")
    suspend fun getGigersAttendance(
        @Query("date") dateInYYYMMDD: String,
        @Query("loginMobile") managerLoginMobile: String
    ): Response<List<GigerAttendance>>


    @POST("gigerAttendanceReport/attendanceHistoryList")
    suspend fun getGigOrderAttendanceMonthly(
        @Body request : GigAttendanceRequest
    ): Response<List<GigApiModel>>
}