package com.gigforce.giger_app.service

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface APPRenderingService {
    @POST
    suspend fun notifyToServer(@Url url : String,@Body data : JsonObject)
}