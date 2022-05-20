package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.gigforce.app.domain.models.tl_workspace.compliance.GetComplianceResponse

interface TLWorkspaceComplianceRepository {

    suspend fun getComplianceData(
        filter: FiltersItemApiModel?
    ): GetComplianceResponse
}