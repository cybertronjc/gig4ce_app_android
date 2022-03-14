package com.gigforce.common_ui.remote

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.common_ui.viewdatamodels.gig.GigApiModel
import com.gigforce.common_ui.viewdatamodels.gig.GigAttendanceRequest
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.common_ui.viewdatamodels.gig.MarkAttendanceRequest
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

    @POST("attendance/markAttendance")
    suspend fun markAttendance(
        @Body markAttendanceRequest: MarkAttendanceRequest
    ): Response<BaseResponse<String>>
}