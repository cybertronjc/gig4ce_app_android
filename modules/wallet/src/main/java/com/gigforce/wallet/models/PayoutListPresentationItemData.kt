package com.gigforce.wallet.models

import com.gigforce.core.SimpleDVM
import com.gigforce.wallet.PayoutCoreRecyclerViewBindings
import com.gigforce.wallet.payouts.payout_list.PayoutListViewModel

open class PayoutListPresentationItemData(
    val type: Int
) : SimpleDVM(type) {

    data class MonthYearHeaderRecyclerItemData(
        val date: String,
        var expanded: Boolean,
        val viewModel: PayoutListViewModel
    ) : PayoutListPresentationItemData(
        PayoutCoreRecyclerViewBindings.MonthYearHeaderRecyclerItemViewType
    )

    data class PayoutItemRecyclerItemData(
        val id: String,
        val icon: String?,
        val companyName: String?,
        val category: String?,
        val amount: Double?,
        val status: String,
        val statusColorCode: String,
        val paymentDate: String?,
        val viewModel: PayoutListViewModel
    ) : PayoutListPresentationItemData(
        PayoutCoreRecyclerViewBindings.PayoutItemRecyclerItemViewType
    )
}