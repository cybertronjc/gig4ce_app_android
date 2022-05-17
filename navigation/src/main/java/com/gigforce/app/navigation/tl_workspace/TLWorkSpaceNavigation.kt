package com.gigforce.app.navigation.tl_workspace

import android.os.Bundle
import androidx.core.os.bundleOf
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject
import javax.inject.Singleton

class TLWorkSpaceNavigation @Inject constructor(
    private val navigation: INavigation
) {
    companion object {

        const val NAV_DESTINATION_TL_WORKSPACE_HOME = "tl_workspace/home"
        const val NAV_DESTINATION_UPCOMING_GIGERS = "tl_workspace/upcoming_gigers"
        const val NAV_DESTINATION_PENDING_COMPLIANCE = "tl_workspace/pending_compliance"


        //Intent extras
        const val INTENT_EXTRA_DATE_FILTER_OPTIONS = "date_filter_options"
        const val INTENT_EXTRA_SELECTED_DATE_FILTER = "selected_date_filter"

        //Fragment ListenerKey
        const val FRAGMENT_RESULT_KEY_DATE_FILTER = "key_date_filter"
    }

    object FragmentResultHandler {

        fun getDateFilterResult(bundle: Bundle): TLWorkSpaceFilterOption? = bundle.getParcelable(
            INTENT_EXTRA_SELECTED_DATE_FILTER
        )
    }

    fun navigateToTLWorkSpaceHomeScreen() {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            null,
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToUpcomingGigersScreen() {
        navigation.navigateTo(
            NAV_DESTINATION_UPCOMING_GIGERS,
            null,
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToPendingComplianceScreen() {
        navigation.navigateTo(
            NAV_DESTINATION_PENDING_COMPLIANCE,
            null,
            NavigationOptions.getNavOptions()
        )
    }

    fun openFilterBottomSheet(
        filterOptions: List<TLWorkSpaceFilterOption>
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_EXTRA_DATE_FILTER_OPTIONS to ArrayList(filterOptions)
            ),
            NavigationOptions.getNavOptions()
        )
    }
}