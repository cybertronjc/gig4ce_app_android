package com.gigforce.wallet.models

import com.gigforce.core.SimpleDVM
import com.gigforce.wallet.PayoutCoreRecyclerViewBindings

open class PayoutListPresentationItemData(
    val type: Int
) : SimpleDVM(type) {

    data class MonthYearHeaderRecyclerItemData(
        val date: String,
        val expanded : Boolean
    ) : PayoutListPresentationItemData(
        PayoutCoreRecyclerViewBindings.MonthYearHeaderRecyclerItemViewType
    )

    data class PayoutItemRecyclerItemData(
        val id :String,
        val icon: String?,
        val companyName: String?,
        val amount: Double?,
        val status: String,
        val paymentDate: String?
    ): PayoutListPresentationItemData(
        PayoutCoreRecyclerViewBindings.PayoutItemRecyclerItemViewType
    )
}