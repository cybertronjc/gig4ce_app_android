package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface GigerAttendanceService {

    @GET
    suspend fun getGigersAttendance(
        @Url getGigersAttendanceUrl : String,
        @Query("date") dateInYYYMMDD: String,
        @Query("loginMobile") managerLoginMobile: String
    ): Response<List<GigerAttendance>>
}