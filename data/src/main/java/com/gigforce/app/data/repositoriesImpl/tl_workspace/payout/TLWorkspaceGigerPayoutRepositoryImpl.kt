package com.gigforce.app.data.repositoriesImpl.tl_workspace.payout

import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutDataRequest
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutResponse
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspacePayoutRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceGigerPayoutRepositoryImpl @Inject constructor(
    private val remoteDataStore: PayoutRemoteDataStore
) : TLWorkspacePayoutRepository {

    override suspend fun getGigerPayoutData(
        request: GetGigerPayoutDataRequest
    ): GetGigerPayoutResponse {
        return remoteDataStore.getGigerPayoutData(request)
    }


}