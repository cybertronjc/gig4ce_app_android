package com.gigforce.common_ui.useCases.payouts

import com.gigforce.common_ui.datamodels.payouts.Filters
import com.gigforce.common_ui.datamodels.payouts.GetPayoutFilters
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.PayoutRetrofitService
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.logger.GigforceLogger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPayoutsUseCase @Inject constructor(
    private val logger: GigforceLogger,
    private val payoutRetrofitService: PayoutRetrofitService
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

    suspend fun getPayouts(
        filterDateFromTo: Pair<LocalDate, LocalDate>
    )  : List<Payout> {
        val payoutFilters = GetPayoutFilters(
            filters = Filters(
                endDate = dateFormatter.format(filterDateFromTo.second),
                startDate = dateFormatter.format(filterDateFromTo.first)
            )
        )

        return payoutRetrofitService
            .getPayouts(payoutFilters)
            .bodyOrThrow()
    }
}