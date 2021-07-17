package com.gigforce.app.eventbridge

import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.POST
import retrofit2.http.Url

interface EventBridgeService {
    @POST
    suspend fun setStatus(@Url url : String, @Body data : EventBridgeModel)

    @POST
    suspend fun setMapStatus(@Url url : String, @Body data : JsonObject)
}