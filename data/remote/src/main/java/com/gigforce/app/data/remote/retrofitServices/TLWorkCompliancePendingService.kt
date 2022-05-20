package com.gigforce.app.data.remote.retrofitServices

import com.gigforce.app.data.remote.models.BaseResponse
import com.gigforce.app.data.remote.models.tl_workspace.GetUpcomingGigersResponse
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TLWorkCompliancePendingService {

    @POST("operationsChampion/tlWorkspace/compliancePending")
    suspend fun getComplaincePendingData(
        @Body request: GetUpcomingGigersRequest
    ): Response<BaseResponse<GetUpcomingGigersResponse>>
}