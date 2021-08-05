package com.gigforce.giger_gigs.tl_login_details

import com.gigforce.giger_gigs.models.AddNewSummaryReqModel
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.models.LoginSummaryBusiness
import com.gigforce.giger_gigs.models.LoginSummaryCity
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface LoginSummaryService {

    @GET
    suspend fun getLoginSummaryCities(
        @Url getLoginSummaryCitiesUrl : String,
    ): Response<List<LoginSummaryCity>>

    @GET
    suspend fun getBusinessByCity(
        @Url getBusinessByCityUrl : String
    ): Response<List<LoginSummaryBusiness>>

    @POST
    suspend fun submitLoginSummary(
        @Url getSubmitUrl : String,
        @Body body: AddNewSummaryReqModel
    ) : Response<ResponseBody>

    @GET
    suspend fun getListingForTL(
        @Url getListingUrl : String,
        @Query("searchDate") searchDate: String,
        @Query("searchCity") searchCity: String,
        @Query("page") page: Int,
        @Query("pagesize") pagesize: Int

    ) : Response<List<ListingTLModel>>
}