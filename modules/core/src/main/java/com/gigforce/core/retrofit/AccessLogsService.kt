package com.gigforce.core.retrofit

import com.gigforce.core.datamodels.AccessLogDataObject
import com.gigforce.core.datamodels.AccessLogResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import retrofit2.Response

interface AccessLogsService {
    @POST
    suspend fun createUpateLogs(@Url url : String, @Body data : AccessLogDataObject): Response<AccessLogResponse>
}