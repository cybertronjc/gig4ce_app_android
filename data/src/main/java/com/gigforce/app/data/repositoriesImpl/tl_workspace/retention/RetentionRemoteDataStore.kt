package com.gigforce.app.data.repositoriesImpl.tl_workspace.retention

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetentionRemoteDataStore @Inject constructor(
    private val remoteService: RetentionRetrofitService
) {

    suspend fun getRetentionData(
        request: GetRetentionDataRequest
    ): GetRetentionResponse = remoteService.getRetentionData(
        request
    ).bodyFromBaseResponseElseThrow()
}