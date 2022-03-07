package com.gigforce.wallet.payouts.payout_list

import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.wallet.models.PayoutListPresentationItemData

object PayoutListDataProcessor {

    fun processPayoutListAndFilters(
        payouts: List<Payout>,
        collapsedDates: List<String>,
        payoutListViewModel: PayoutListViewModel
    ): List<PayoutListPresentationItemData> {

        val payoutMonthYearToPayoutGroup = payouts
            .sortedByDescending { it.getPaymentCycleEndDateMonth() }
            .groupBy {
                it.getPaymentCycleEndDateMonthYear()
            }

        return mutableListOf<PayoutListPresentationItemData>()
            .apply {

                payoutMonthYearToPayoutGroup.forEach { (monthYearHeader, payouts) ->

                    if (collapsedDates.contains(monthYearHeader)) {

                        add(
                            PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData(
                                date = monthYearHeader,
                                expanded = false,
                                viewModel = payoutListViewModel
                            )
                        )
                    } else {

                        add(
                            PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData(
                                date = monthYearHeader,
                                expanded = true,
                                viewModel = payoutListViewModel
                            )
                        )

                        addAll(mapPayoutsToPayoutItemView(payouts, payoutListViewModel))
                    }
                }
            }
    }

    private fun mapPayoutsToPayoutItemView(
        payouts: List<Payout>,
        viewModel: PayoutListViewModel
    ): Collection<PayoutListPresentationItemData.PayoutItemRecyclerItemData> {

        return payouts.map {
            PayoutListPresentationItemData.PayoutItemRecyclerItemData(
                id = it.id ?: "-1",
                icon = it.businessIcon,
                companyName = it.businessName,
                amount = it.amount,
                status = it.status ?: "#FFBF00",
                statusColorCode = it.statusColorCode ?: "#",
                paymentDate = it.paidOnDate,
                viewModel = viewModel,
                category = it.category
            )
        }
    }

}