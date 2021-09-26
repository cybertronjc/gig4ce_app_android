package com.gigforce.common_ui.repository

import com.gigforce.core.datamodels.AccessLogDataObject
import com.gigforce.core.datamodels.AccessLogResponse
import com.gigforce.core.retrofit.AccessLogsService
import com.gigforce.core.retrofit.RetrofitFactory


interface IBannerCardRepository {
    suspend fun createLogs(
        url: String,
        uuid: String,
        screen: String,
        bannerName: String,
        id: String
    ): AccessLogResponse

    suspend fun updateLogs(
        url: String,
        uuid: String,
        _id: String,
        screen: String
    ): AccessLogResponse
}

class BannerCardRepository : IBannerCardRepository {
    private val accessLogsApi: AccessLogsService =
        RetrofitFactory.createService(AccessLogsService::class.java)

    override suspend fun createLogs(
        url: String,
        uuid: String,
        screen: String,
        bannerName: String,
        id: String
    ): AccessLogResponse {
        val accessLogsRequest = accessLogsApi.createUpateLogs(
            url,
            AccessLogDataObject(uuid = uuid, screen = screen, bannerName = bannerName, id = id)
        )
        if (!accessLogsRequest.isSuccessful) {
            throw Exception(accessLogsRequest.message())
        } else {
            return accessLogsRequest.body()!!
        }
    }

    override suspend fun updateLogs(
        url: String,
        uuid: String,
        _id: String,
        screen: String
    ): AccessLogResponse {
        val accessLogsRequest = accessLogsApi.createUpateLogs(
            url,
            AccessLogDataObject(uuid = uuid, _id = _id, screen = screen)
        )
        if (!accessLogsRequest.isSuccessful) {
            throw Exception(accessLogsRequest.message())
        } else {
            return accessLogsRequest.body()!!
        }
    }
}