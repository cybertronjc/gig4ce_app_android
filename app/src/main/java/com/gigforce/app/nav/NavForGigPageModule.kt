package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.app.navigation.tl_workspace.attendance.ActivityTrackerNavigation

class NavForGigPageModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName:String = "gig"
        baseImplementation.registerRoute("${moduleName}/mygig", R.id.gig_history_fragment)

        baseImplementation.registerRoute("${moduleName}/attendance", R.id.gigPage2Fragment)
        baseImplementation.registerRoute("${moduleName}/gigRegulariseAttendanceFragment",R.id.gigRegulariseAttendanceFragment)
        baseImplementation.registerRoute("${moduleName}/gigDetailsFragment",R.id.gigDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/gigerIdFragment",R.id.giger_id_fragment)
        baseImplementation.registerRoute("${moduleName}/gigMonthlyAttendanceFragment",R.id.gigMonthlyAttendanceFragment)
        baseImplementation.registerRoute("${moduleName}/gigerAttendanceUnderManagerFragment",R.id.gigerAttendanceUnderManagerFragment)
        baseImplementation.registerRoute("${moduleName}/tlLoginDetails",R.id.teamLeaderLoginDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/addNewLoginSummary",R.id.addNewLoginSummaryFragment)
        baseImplementation.registerRoute("${moduleName}/filterTeamLeaderListing",R.id.teamLeaderLoginFilterFragment)
        baseImplementation.registerRoute("${moduleName}/TravellingDetailInfoFragment",R.id.travellingDetailInfoFragment)

        baseImplementation.registerRoute(
            ActivityTrackerNavigation.NAV_DESTINATION_ATTENDANCE_DETAILS,
            R.id.gigerAttendanceDetailsFragment
        )
        baseImplementation.registerRoute(
            ActivityTrackerNavigation.NAV_DESTINATION_MARK_ACTIVE_CONFIRMATION_DIALOG,
            R.id.markActiveConfirmationBottomSheetFragment
        )
        baseImplementation.registerRoute(
            ActivityTrackerNavigation.NAV_DESTINATION_MARK_INACTIVE_CONFIRMATION_DIALOG,
            R.id.markInactiveConfirmationBottomSheetFragment
        )
        baseImplementation.registerRoute(
            ActivityTrackerNavigation.NAV_DESTINATION_SELECT_INACTIVE_REASON_DIALOG,
            R.id.selectMarkInactiveReasonsBottomSheetFragment
        )
        baseImplementation.registerRoute(
            ActivityTrackerNavigation.NAV_DESTINATION_RESOLVE_ATTENDANCE_CONFIRMATION_DIALOG,
            R.id.resolveAttendanceConflictConfirmationBottomSheetFragment
        )


//        baseImplementation.registerRoute("${moduleName}/filterTeamLeaderListing",R.id.teamLeaderLoginFilterFragment)
//        baseImplementation.registerRoute("${moduleName}/filterTeamLeaderListing",R.id.teamLeaderLoginFilterFragment)

    }
}