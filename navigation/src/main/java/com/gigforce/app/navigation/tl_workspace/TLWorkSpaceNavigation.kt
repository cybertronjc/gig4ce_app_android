package com.gigforce.app.navigation.tl_workspace

import android.os.Bundle
import androidx.core.os.bundleOf
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
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
        const val NAV_DESTINATION_SELECTION_FORM = "tl_workspace/selection_form"
        const val NAV_DESTINATION_LOGIN_SUMMARY = "tl_workspace/login_summary"
        const val NAV_DESTINATION_GIGER_PAYOUT = "tl_workspace/payout"
        const val NAV_DESTINATION_ACTIVITY_TRACKER = "gig/gigerAttendanceUnderManagerFragment"
        const val NAV_DESTINATION_SELECTION_LIST = "LeadMgmt/joiningListFragment"
        const val NAV_HELP_SCREEN = "HelpSectionFragment"

        const val NAV_DESTINATION_DATE_FILTER_BOTTOM_SHEET = "tl_workspace/date_filter_bottom_sheet"
        const val NAV_DESTINATION_GIGER_INFO_BOTTOM_SHEET = "tl_workspace/giger_info_bottomsheet"

        //Intent extras
        const val INTENT_EXTRA_DATE_FILTER_OPTIONS = "date_filter_options"
        const val INTENT_EXTRA_SELECTED_DATE_FILTER = "selected_date_filter"
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_GIGER_ID = "giger_id"
        const val INTENT_EXTRA_PAYOUT_ID = "payout_id"
        const val INTENT_EXTRA_BUSINESS_ID = "business_id"
        const val INTENT_EXTRA_JOB_PROFILE_ID = "job_profile_id"
        const val INTENT_OPEN_USER_DETAILS_OF = "open_details_of"

        const val COMPLIANCE = "compliance"
        const val RETENTION = "retention"
        const val PAYOUT = "payout"

        //Fragment ListenerKey
        const val FRAGMENT_RESULT_KEY_DATE_FILTER = "key_date_filter"
    }

    object FragmentResultHandler {

        fun getDateFilterResult(bundle: Bundle): TLWorkSpaceDateFilterOption? =
            bundle.getParcelable(
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
        dateFilterOptions: List<TLWorkSpaceDateFilterOption>
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_DATE_FILTER_BOTTOM_SHEET,
            bundleOf(
                INTENT_EXTRA_DATE_FILTER_OPTIONS to ArrayList(dateFilterOptions)
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

    fun openUpcomingGigerInfoBottomSheet(
        gigerId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            bundleOf(
                INTENT_OPEN_USER_DETAILS_OF to COMPLIANCE,
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
        gigerId: String,
        jobProfileId: String,
        businessId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_GIGER_INFO_BOTTOM_SHEET,
            bundleOf(
                INTENT_OPEN_USER_DETAILS_OF to RETENTION,
                INTENT_EXTRA_GIGER_ID to gigerId,
                INTENT_EXTRA_JOB_PROFILE_ID to jobProfileId,
                INTENT_EXTRA_BUSINESS_ID to businessId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheetForCompliance(
        gigerId: String,
        jobProfileId: String,
        businessId: String,
        eJoiningId: String?
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_GIGER_INFO_BOTTOM_SHEET,
            bundleOf(
                INTENT_OPEN_USER_DETAILS_OF to COMPLIANCE,
                INTENT_EXTRA_GIGER_ID to "18qOXM6TUOgGcx70XwMNFxvVxKm2",
                INTENT_EXTRA_JOB_PROFILE_ID to "7ccMIdMtaamjtIgP85yE",
                INTENT_EXTRA_BUSINESS_ID to "Yzd2b16WVRFtfnJWTpHN",
                INTENT_EXTRA_PAYOUT_ID to "627f5345b4b64ad717e7e92d"
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigerInfoBottomSheetForPayout(
        gigerId: String,
        jobProfileId: String,
        businessId: String,
        eJoiningId: String,
        payoutId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_GIGER_INFO_BOTTOM_SHEET,
            bundleOf(
                INTENT_OPEN_USER_DETAILS_OF to PAYOUT,
                INTENT_EXTRA_GIGER_ID to gigerId,
                INTENT_EXTRA_JOB_PROFILE_ID to jobProfileId,
                INTENT_EXTRA_BUSINESS_ID to businessId,
                INTENT_EXTRA_PAYOUT_ID to payoutId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToJoiningListScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_SELECTION_LIST,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }


    fun navigateToPayoutListScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_GIGER_PAYOUT,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToActivityTrackerListScreen(
        title: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_ACTIVITY_TRACKER,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to title
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun navigateToHelpScreen() {
        navigation.navigateTo(
            NAV_HELP_SCREEN,
            bundleOf(
                BaseFragment2.INTENT_EXTRA_TOOLBAR_TITLE to "Help"
            ),
            NavigationOptions.getNavOptions()
        )
    }

}