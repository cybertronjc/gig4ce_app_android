package com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TLWorkUpcomingGigersService {

    @POST("operationsChampion/tlWorkspace/getUpcomingGigers")
    suspend fun getUpcomingGigers(
        @Body request: GetUpcomingGigersRequest
    ): Response<BaseResponse<GetUpcomingGigersResponse>>
}