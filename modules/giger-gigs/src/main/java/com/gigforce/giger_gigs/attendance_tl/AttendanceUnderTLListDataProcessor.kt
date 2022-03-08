package com.gigforce.giger_gigs.attendance_tl

import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModel
import com.gigforce.wallet.models.PayoutListPresentationItemData

object AttendanceUnderTLListDataProcessor {

    fun processPayoutListAndFilters(
        attendance: List<Payout>,
        collapsedDates: List<String>,
        gigerAttendanceUnderManager: GigerAttendanceUnderManagerViewModel
    ): List<AttendanceRecyclerItemData> {

        val payoutMonthYearToPayoutGroup = attendance
            .sortedByDescending { it.getPaymentCycleEndDateMonth() }
            .groupBy {
                it.getPaymentCycleEndDateMonthYear()
            }

        return mutableListOf<AttendanceRecyclerItemData>()
            .apply {

                payoutMonthYearToPayoutGroup.forEach { (monthYearHeader, payouts) ->

                    if (collapsedDates.contains(monthYearHeader)) {

                        add(
                            AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData(
                                date = monthYearHeader,
                                expanded = false,
                                viewModel = payoutListViewModel
                            )
                        )
                    } else {

                        add(
                            AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData(
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
        viewModel: GigerAttendanceUnderManagerViewModel
    ): Collection<AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData> {

        return payouts.map {
            AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData(
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