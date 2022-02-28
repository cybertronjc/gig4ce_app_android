package com.gigforce.wallet.payouts.payout_list

import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.wallet.models.PayoutListPresentationItemData

object PayoutListDataProcessor {

    fun processPayoutListAndFilters(
        payouts: List<Payout>,
        collapsedDates: List<String>,
        payoutListViewModel: PayoutListViewModel
    ): List<PayoutListPresentationItemData> {

        val payoutMonthYearToPayoutGroup = payouts.groupBy {
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
                                expanded = false,
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
                viewModel = viewModel
            )
        }
    }

    fun expandPayoutsOfMonthYear(
        monthYear: String,
        payouts: List<Payout>,
        payoutListShownOnScreen: MutableList<PayoutListPresentationItemData>,
        payoutListViewModel: PayoutListViewModel
    ) {
        val indexOfTappedMonthYearHeader = payoutListShownOnScreen.indexOfFirst {
            it is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData && it.date == monthYear
        }

        if (indexOfTappedMonthYearHeader != -1) {
            (payoutListShownOnScreen[indexOfTappedMonthYearHeader] as PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData).expanded =
                true

            val payoutToInsertFromIndex = indexOfTappedMonthYearHeader + 1
            val payoutsToInsert = payouts.filter {
                it.getPaymentCycleEndDateMonthYear() == monthYear
            }

            payoutListShownOnScreen.addAll(
                payoutToInsertFromIndex,
                mapPayoutsToPayoutItemView(payoutsToInsert, payoutListViewModel)
            )
        }
    }

    fun collapsePayoutsOfMonthYear(
        monthYear: String,
        payoutListShownOnScreen: MutableList<PayoutListPresentationItemData>
    ) {

        val indexOfTappedMonthYearHeader = payoutListShownOnScreen.indexOfFirst {
            it is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData && it.date == monthYear
        }

        val payoutToRemoveStartIndex = indexOfTappedMonthYearHeader + 1
        var payoutToRemoveEndIndex = -1

        for (i in payoutToRemoveStartIndex until payoutListShownOnScreen.size) {

            if (payoutListShownOnScreen[i] is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData) {
                payoutToRemoveEndIndex = i
                break
            }
        }
        if (payoutToRemoveEndIndex == -1) {
            payoutToRemoveEndIndex = payoutListShownOnScreen.lastIndex
        }

        payoutListShownOnScreen.subList(payoutToRemoveStartIndex, payoutToRemoveEndIndex).clear()
    }
}