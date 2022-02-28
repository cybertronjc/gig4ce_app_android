package com.gigforce.common_ui.useCases.payouts

import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.PayoutRetrofitService
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.logger.GigforceLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPayoutDetailsUseCase @Inject constructor(
    private val logger: GigforceLogger,
    private val payoutRetrofitService: PayoutRetrofitService
) {

    suspend fun getPayouts(
        payoutId: String
    ): Payout {

        return payoutRetrofitService
            .getPayoutDetails(payoutId)
            .bodyOrThrow()
    }
}