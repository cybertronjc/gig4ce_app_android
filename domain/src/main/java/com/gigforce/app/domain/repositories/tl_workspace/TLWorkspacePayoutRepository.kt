package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutDataRequest
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutResponse

interface TLWorkspacePayoutRepository {

    suspend fun getGigerPayoutData(
        request: GetGigerPayoutDataRequest
    ) : GetGigerPayoutResponse
}