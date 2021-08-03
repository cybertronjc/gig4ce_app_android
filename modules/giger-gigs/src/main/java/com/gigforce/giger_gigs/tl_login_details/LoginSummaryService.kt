package com.gigforce.giger_gigs.tl_login_details

import com.gigforce.giger_gigs.models.AddNewSummaryReqModel
import com.gigforce.giger_gigs.models.LoginSummaryBusiness
import com.gigforce.giger_gigs.models.LoginSummaryCity
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface LoginSummaryService {

    @GET("gigerAttendanceReport/cities")
    suspend fun getLoginSummaryCities(
    ): Response<List<LoginSummaryCity>>

    @GET("gigerAttendanceReport/businessByCity/{cityId}")
    suspend fun getBusinessByCity(
        @Path("cityId") cityId: String
    ): Response<List<LoginSummaryBusiness>>

    @POST("gigerAttendanceReport/submit")
    suspend fun submitLoginSummary(
        @Body body: AddNewSummaryReqModel
    ) : Response<ResponseBody>
}