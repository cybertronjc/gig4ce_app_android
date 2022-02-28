package com.gigforce.common_ui.remote

import com.gigforce.common_ui.datamodels.payouts.GetPayoutFilters
import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.common_ui.viewdatamodels.PendingJoiningItemDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface PayoutRetrofitService {

    @POST("paymentcycle/fetchGigerPayoutList")
    suspend fun getPayouts(
        @Body filters : GetPayoutFilters
    ): Response<List<Payout>>

    @GET("paymentcycle/fetchGigerPayoutList")
    suspend fun getPayoutDetails(
        @Query("id") payoutId : String
    ): Response<Payout>
}