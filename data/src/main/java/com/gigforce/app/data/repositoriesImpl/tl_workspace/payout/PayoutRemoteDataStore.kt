package com.gigforce.app.data.repositoriesImpl.tl_workspace.payout

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutDataRequest
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayoutRemoteDataStore @Inject constructor(
    private val remoteService: PayoutRetrofitService
) {

    suspend fun getGigerPayoutData(
        request: GetGigerPayoutDataRequest
    ): GetGigerPayoutResponse = remoteService.getGigerPayoutData(
        request
    ).bodyFromBaseResponseElseThrow()
}