package com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.compliance.GetComplianceResponse
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceComplianceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceComplianceRepositoryImpl @Inject constructor(
    private val compliancePendingService : TLWorkCompliancePendingService
) : TLWorkspaceComplianceRepository {

    override suspend fun getComplianceData(
        filter: FiltersItemApiModel?
    ): GetComplianceResponse {
        return  compliancePendingService
            .getCompliancePendingData(
                GetUpcomingGigersRequest(
                    filter = filter
                )
            ).bodyFromBaseResponseElseThrow()
    }
}