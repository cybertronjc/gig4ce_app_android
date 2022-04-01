package com.gigforce.common_ui.remote

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.viewdatamodels.gig.*
import retrofit2.Response
import retrofit2.http.*

interface GigerAttendanceService {

    @GET("gigAttendance/tlAttendanceReport")
    suspend fun getGigersAttendance(
        @Query("attendanceDate") dateInYYYMMDD: String
    ): Response<List<GigAttendanceApiModel>>


    @POST("gigerAttendanceReport/attendanceHistoryList")
    suspend fun getGigOrderAttendanceMonthly(
        @Body request : GigAttendanceRequest
    ): Response<List<GigApiModel>>

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