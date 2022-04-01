package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.gig.GigApiModel
import com.gigforce.common_ui.viewdatamodels.gig.GigInfoBasicApiModel
import retrofit2.Response
import retrofit2.http.*

interface GigService {

    @GET("gigAttendance/gigsScheduled")
    suspend fun getGigsForDate(
        @Query("date") dateInYYYMMDD: String
    ): Response<List<GigInfoBasicApiModel>>

    @GET("gigAttendance/gigsScheduled")
    suspend fun getNext7DaysUpcomingGigs(): Response<List<GigInfoBasicApiModel>>

    @GET("gigAttendance/getGigDetails")
    suspend fun getGigDetails(
        @Query("gigId") gigId: String
    ): Response<List<GigApiModel>>

}