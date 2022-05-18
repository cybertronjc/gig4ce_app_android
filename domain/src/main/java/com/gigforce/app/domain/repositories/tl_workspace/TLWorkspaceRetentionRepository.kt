package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionResponse

interface TLWorkspaceRetentionRepository {

    suspend fun getRetentionData(
        request: GetRetentionDataRequest
    ): GetRetentionResponse
}