package com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.GetUpcomingGigersResponse
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserInfoRetrofitService {

    @POST("operationsChampion/tlWorkspace/gigerInfo")
    suspend fun getRetentionData(
        @Body request: GigerInfoRequest
    ): Response<BaseResponse<GigerInfoApiModel>>
}