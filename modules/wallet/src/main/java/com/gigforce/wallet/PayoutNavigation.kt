package com.gigforce.wallet

import androidx.core.os.bundleOf
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import com.gigforce.wallet.payouts.payout_list.filter.DateFilterForFilterScreen
import com.gigforce.wallet.payouts.payout_list.filter.PayoutListFilterFragment
import javax.inject.Inject


class PayoutNavigation @Inject constructor(
    private val navigation: INavigation
) {

    companion object {
        const val NAV_DESTINATION_PAYOUT_LIST = "Payout/PayoutListFragment"
        const val NAV_DESTINATION_PAYOUT_DETAILS = "Payout/PayoutDetailsBottomSheet"
        const val NAV_DESTINATION_PAYOUT_LIST_FILTERS = "Payout/PayoutListFilterFragment"
    }

    fun openPayoutList(
        payOutIdToOpenOnLoading: String? = null
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_PAYOUT_LIST,
            bundleOf(PayoutConstants.INTENT_EXTRA_PAYOUT_ID to payOutIdToOpenOnLoading),
            navOptions = NavigationOptions.getNavOptions()
        )
    }

    fun openPayoutDetailsScreen(
        payoutId : String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_PAYOUT_DETAILS,
            bundleOf(PayoutConstants.INTENT_EXTRA_PAYOUT_ID to payoutId),
            navOptions = NavigationOptions.getNavOptions()
        )
    }

    fun openPayoutListFilterScreen(
        dateFilters  : ArrayList<DateFilterForFilterScreen>
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_PAYOUT_LIST_FILTERS,
            bundleOf(PayoutListFilterFragment.INTENT_EXTRA_DATE_FILTERS to dateFilters),
            navOptions = NavigationOptions.getNavOptions()
        )
    }

}