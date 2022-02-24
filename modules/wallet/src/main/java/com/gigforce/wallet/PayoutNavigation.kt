package com.gigforce.wallet

import androidx.core.os.bundleOf
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject


class PayoutNavigation @Inject constructor(
    private val navigation: INavigation
) {

    companion object {
        const val NAV_DESTINATION_PAYOUT_LIST = "Payout/PayoutListFragment" }

    fun openPayoutList(
        payOutIdToOpenOnLoading: String? = null
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_PAYOUT_LIST,
            bundleOf(PayoutConstants.INTENT_EXTRA_PAYOUT_ID to payOutIdToOpenOnLoading),
            navOptions = NavigationOptions.getNavOptions()
        )
    }
}