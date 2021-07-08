package com.gigforce.app.eventbridge

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface EventBridgeService {
    @POST
    suspend fun setStatus(@Url url : String, @Body data : EventBridgeModel)
}