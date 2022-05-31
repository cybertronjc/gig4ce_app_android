package com.gigforce.app.navigation.tl_workspace

import android.os.Bundle
import androidx.core.os.bundleOf
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class TLWorkSpaceNavigation @Inject constructor(
    private val navigation: INavigation
) {
    companion object {

        const val NAV_DESTINATION_TL_WORKSPACE_HOME = "tl_workspace/home"
        const val NAV_DESTINATION_UPCOMING_GIGERS = "tl_workspace/upcoming_gigers"
        const val NAV_DESTINATION_PENDING_COMPLIANCE = "tl_workspace/pending_compliance"
        const val NAV_DESTINATION_RETENTION = "tl_workspace/retention"
        const val NAV_DESTINATION_ACTIVITY_TRACKER = "gig/gigerAttendanceUnderManagerFragment"
        const val NAV_DESTINATION_SELECTION_LIST = "LeadMgmt/joiningListFragment"


        const val NAV_DESTINATION_DATE_FILTER_BOTTOM_SHEET = "tl_workspace/date_filter_bottom_sheet"


        //Intent extras
        const val INTENT_EXTRA_DATE_FILTER_OPTIONS = "date_filter_options"
        const val INTENT_EXTRA_SELECTED_DATE_FILTER = "selected_date_filter"
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_GIGER_ID = "giger_id"
        const val INTENT_EXTRA_PAYOUT_ID = "payout_id"


        //Fragment ListenerKey
        const val FRAGMENT_RESULT_KEY_DATE_FILTER = "key_date_filter"
    }

    object FragmentResultHandler {

        fun getDateFilterResult(bundle: Bundle): TLWorkSpaceFilterOption? = bundle.getParcelable(
            INTENT_EXTRA_SELECTED_DATE_FILTER
        )
    }

    fun navigateToTLWorkSpaceHomeScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToUpcomingGigersScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_UPCOMING_GIGERS,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToPendingComplianceScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_PENDING_COMPLIANCE,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToRetentionScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_RETENTION,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openFilterBottomSheet(
        filterOptions: List<TLWorkSpaceFilterOption>
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_DATE_FILTER_BOTTOM_SHEET,
            bundleOf(
                INTENT_EXTRA_DATE_FILTER_OPTIONS to ArrayList(filterOptions)
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheetForPayout(
        gigerId: String,
        payoutId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_EXTRA_GIGER_ID to gigerId,
                INTENT_EXTRA_PAYOUT_ID to payoutId,
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheet(
        gigerId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_EXTRA_GIGER_ID to gigerId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheetForAttendance(
        gigerId: String,
        gigId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_EXTRA_GIGER_ID to gigerId,
                INTENT_EXTRA_GIG_ID to gigId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheetForRetention(
        gigerId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_EXTRA_GIGER_ID to gigerId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToJoiningListScreen(
        title : String
    ){
        navigation.navigateTo(
            NAV_DESTINATION_SELECTION_LIST,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToPayoutListScreen(
        title : String
    ){

    }

    fun navigateToActivityTrackerListScreen(
        title : String
    ){
        navigation.navigateTo(
            NAV_DESTINATION_ACTIVITY_TRACKER,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

}