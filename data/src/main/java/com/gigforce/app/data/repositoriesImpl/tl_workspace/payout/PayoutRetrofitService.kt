package com.gigforce.app.data.repositoriesImpl.tl_workspace.payout

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutDataRequest
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST

interface PayoutRetrofitService {

    @Headers("LoggedInUserUid: 729ldLw3aieVLEdaQdYIpDVRZfe2")
    @POST("https://dk2gichyyc.execute-api.ap-south-1.amazonaws.com/prod/operationsChampion/tlWorkspace/payout")
    suspend fun getGigerPayoutData(
        @Body request: GetGigerPayoutDataRequest
    ): Response<BaseResponse<GetGigerPayoutResponse>>
}