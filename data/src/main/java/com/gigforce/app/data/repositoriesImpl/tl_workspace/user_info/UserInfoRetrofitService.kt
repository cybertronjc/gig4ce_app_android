package com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info

import com.gigforce.app.data.remote.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserInfoRetrofitService {

    @POST("operationsChampion/tlWorkspace/gigerInfo")
    suspend fun getUserInformationData(
        @Body request: GigerInfoRequest
    ): Response<BaseResponse<GigerInfoApiModel>>
}