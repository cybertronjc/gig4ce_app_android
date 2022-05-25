package com.gigforce.app.data.repositoriesImpl.tl_workspace.retention

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.GetUpcomingGigersResponse
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetentionRetrofitService {

    @POST("operationsChampion/tlWorkspace/retention")
    suspend fun getRetentionData(
        @Body request: GetRetentionDataRequest
    ): Response<BaseResponse<GetRetentionResponse>>
}