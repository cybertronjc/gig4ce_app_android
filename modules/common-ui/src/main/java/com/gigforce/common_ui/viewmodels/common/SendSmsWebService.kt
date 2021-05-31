package com.gigforce.common_ui.viewmodels.common

import com.gigforce.common_ui.viewmodels.common.models.SendSmsRequest
import com.gigforce.common_ui.viewmodels.common.models.SendSmsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SendSmsWebService {
    @POST
    suspend fun sendSms(
            @Url fullUrl: String,
            @Body postData: SendSmsRequest
    ): Response<SendSmsResponse>
}