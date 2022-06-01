package com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.GetUpcomingGigersResponse
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.compliance.GetComplianceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TLWorkCompliancePendingService {

    @POST("operationsChampion/tlWorkspace/compliancePending")
    suspend fun getCompliancePendingData(
        @Body request: GetUpcomingGigersRequest
    ): Response<BaseResponse<GetComplianceResponse>>
}