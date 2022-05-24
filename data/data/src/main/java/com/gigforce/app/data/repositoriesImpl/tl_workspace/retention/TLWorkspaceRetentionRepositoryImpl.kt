package com.gigforce.app.data.repositoriesImpl.tl_workspace.retention

import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionResponse
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceRetentionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceRetentionRepositoryImpl @Inject constructor(

): TLWorkspaceRetentionRepository {
    override suspend fun getRetentionData(request: GetRetentionDataRequest): GetRetentionResponse {
        TODO("Not yet implemented")
    }
}