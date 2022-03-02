package com.gigforce.wallet

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.wallet.payouts.payout_list.components.MonthYearHeaderRecyclerItemView
import com.gigforce.wallet.payouts.payout_list.components.PayoutItemRecyclerItemView

object PayoutCoreRecyclerViewBindings : IViewTypeLoader {

    /**
     * Payout List - **[PayoutListFragment]**
     */
    const val MonthYearHeaderRecyclerItemViewType = 1298012
    const val PayoutItemRecyclerItemViewType = 1239344

    override fun getView(
        context: Context,
        viewType: Int
    ): View? = when (viewType) {
        MonthYearHeaderRecyclerItemViewType -> MonthYearHeaderRecyclerItemView(context, null)
        PayoutItemRecyclerItemViewType -> PayoutItemRecyclerItemView(context, null)
        else -> null
    }

}