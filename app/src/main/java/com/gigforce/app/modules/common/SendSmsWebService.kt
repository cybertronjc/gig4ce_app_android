package com.gigforce.app.modules.common

import com.gigforce.app.modules.common.models.SendSmsRequest
import com.gigforce.app.modules.common.models.SendSmsResponse
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