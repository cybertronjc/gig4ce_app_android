package com.gigforce.giger_gigs.tl_login_details

import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigResponse
import com.gigforce.giger_gigs.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface LoginSummaryService {

    @GET("gigerAttendanceReport/cities")
    suspend fun getLoginSummaryCities(): Response<List<LoginSummaryCity>>

    @GET("gigerAttendanceReport/businessByCity/{cityId}")
    suspend fun getBusinessByCity(
        @Path("cityId") cityId : String
    ): Response<List<LoginSummaryBusiness>>

    @POST("gigerAttendanceReport/submit")
    suspend fun submitLoginSummary(
        @Body body: AddNewSummaryReqModel
    ) : Response<ResponseBody>

    @GET("gigerAttendanceReport/listingForTL/{userUid}")
    suspend fun getListingForTL(
        @Path("userUid") userUid: String,
        @Query("page") page: Int,
        @Query("pagesize") pagesize: Int
    ) : Response<List<ListingTLModel>>


    @GET("tlDailyReport/listingForTL/{userUid}")
    suspend fun getDailyLoginReportListingForTL(
        @Path("userUid") userUid: String,
        @Query("searchCity") searchCity: String,
        @Query("searchDate") searchDate: String,
        @Query("page") page: Int,
        @Query("pagesize") pagesize: Int
    ) : Response<List<DailyLoginReport>>

    @POST("tlDailyReport/submit")
    suspend fun submitLoginReport(
        @Body body: DailyTlAttendanceReport
    ) : Response<AssignGigResponse>


    @GET("gigerAttendanceReport/gigerPresent/{userUid}")
    suspend fun checkIfTLMarked(
        @Path("userUid") userUid: String
    ) : Response<CheckMark>

    @GET
    suspend fun getBusinessByCity(
        @Url getListingUrl : String,
        @Query("searchCity") searchCity: String,
        @Query("searchDate") searchDate: String,
        @Query("page") page: Int,
        @Query("pagesize") pagesize: Int
    ) : Response<List<LoginSummaryBusiness>>

    //TODO("replace with di provided url")
    @GET("https://dk2gichyyc.execute-api.ap-south-1.amazonaws.com/dev/gigerAttendanceReport/getDetailsByTLandCity/{cityId}")
    suspend fun getBusinessByCityWithLoginCount(
        @Path("cityId") cityId : String
    ): Response<List<LoginSummaryBusiness>>
}