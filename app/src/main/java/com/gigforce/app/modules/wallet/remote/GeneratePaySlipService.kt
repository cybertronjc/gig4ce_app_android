package com.gigforce.app.modules.wallet.remote

import com.gigforce.app.modules.wallet.models.PaySlipResponseModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface GeneratePaySlipService {
    @GET
    suspend fun generatePayslip(
        @Url apiUrl: String,
        @Query("payslipId") payslipId: String
    ): Response<PaySlipResponseModel>

    @GET
    suspend fun downloadPaySlip(
        @Url paySlipUrl : String
    ) : Response<ResponseBody>
}