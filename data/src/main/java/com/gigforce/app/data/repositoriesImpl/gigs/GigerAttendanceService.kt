package com.gigforce.app.data.repositoriesImpl.gigs

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.data.repositoriesImpl.gigs.models.*

import retrofit2.Response
import retrofit2.http.*

interface GigerAttendanceService {

    @POST("operationsChampion/tlWorkspace/tlAttendanceReport")
    suspend fun getGigersAttendance(
        @Body request : GetGigersAttendanceRequest
    ): Response<BaseResponse<GetGigersAttendanceResponse>>

    @POST("gigAttendance/attendanceHistory")
    suspend fun getGigOrderAttendanceMonthly(
        @Body request : GigAttendanceRequest
    ): Response<List<GigInfoBasicApiModel>>

    @POST("gigAttendance/markAttendance")
    suspend fun markAttendance(
        @Body markAttendanceRequest: MarkAttendanceRequest
    ): Response<MarkAttendanceResponse>

    @POST("gigAttendance/resolveAttendance")
    suspend fun resolveAttendanceConflict(
        @Body request: ResolveAttendanceRequest
    ): Response<MarkAttendanceResponse>

    @GET("gigAttendance/getAttendanceInfo")
    suspend fun getGigDetailsWithAttendanceInfo(
        @Query("gigId") gigId : String
    ): Response<List<GigAttendanceApiModel>>

}