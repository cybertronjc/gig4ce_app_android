package com.gigforce.app.data.repositoriesImpl.gigs


import com.gigforce.app.data.repositoriesImpl.gigs.models.GigApiModel
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigInfoBasicApiModel
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

    @GET("gigAttendance/getGigersGigs")
    suspend fun getPastGigs(
        @Query("type") type : String = "past",
        @Query("limit") limit : Long,
        @Query("offset") offset : Long
    ): Response<List<GigInfoBasicApiModel>>


    @GET("gigAttendance/getGigersGigs")
    suspend fun getUpcomingGigs(
        @Query("type") type : String = "upcoming",
        @Query("limit") limit : Long,
        @Query("offset") offset : Long
    ): Response<List<GigInfoBasicApiModel>>

}