package com.gigforce.wallet.payouts.payout_list

import com.gigforce.core.recyclerView.CoreDiffUtilCallback
import com.gigforce.wallet.models.PayoutListPresentationItemData

class PayoutDiffUtilCallback : CoreDiffUtilCallback<PayoutListPresentationItemData>() {

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return if (oldItem.type != newItem.type) {
            false
        } else {
            if (oldItem is PayoutListPresentationItemData.PayoutItemRecyclerItemData &&
                newItem is PayoutListPresentationItemData.PayoutItemRecyclerItemData
            ) {
                oldItem.id == newItem.id
            } else if (oldItem is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData &&
                newItem is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData
            ) {
                oldItem.date == newItem.date
            } else {
                false
            }
        }
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return if (oldItem.type != newItem.type) {
            false
        } else {
            if (oldItem is PayoutListPresentationItemData.PayoutItemRecyclerItemData &&
                newItem is PayoutListPresentationItemData.PayoutItemRecyclerItemData
            ) {
                oldItem.id == newItem.id
                        && oldItem.amount == newItem.amount
                        && oldItem.companyName == newItem.companyName
                        && oldItem.status == newItem.status

            } else if (oldItem is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData &&
                newItem is PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData
            ) {
                oldItem.date == newItem.date && oldItem.expanded == newItem.expanded
            } else {
                false
            }
        }
    }
}