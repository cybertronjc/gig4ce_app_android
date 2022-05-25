package com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.gigforce.app.domain.models.tl_workspace.compliance.GetComplianceResponse
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceComplianceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceComplianceRepositoryImpl @Inject constructor() : TLWorkspaceComplianceRepository {

    override suspend fun getComplianceData(
        filter: FiltersItemApiModel?
    ): GetComplianceResponse {
        TODO("Not yet implemented")
    }
}