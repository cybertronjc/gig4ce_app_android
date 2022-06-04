package com.gigforce.app.navigation.tl_workspace.attendance

import androidx.core.os.bundleOf
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class ActivityTrackerNavigation @Inject constructor(
    private val navigation: INavigation
) {

    companion object {
        const val NAV_DESTINATION_MARK_ACTIVE_CONFIRMATION_DIALOG =
            "gig/mark_giger_active_confirmation_screen"
        const val NAV_DESTINATION_MARK_INACTIVE_CONFIRMATION_DIALOG =
            "gig/mark_giger_inactive_confirmation_screen"
        const val NAV_DESTINATION_SELECT_INACTIVE_REASON_DIALOG =
            "gig/select_giger_inactive_reason_screen"
        const val NAV_DESTINATION_RESOLVE_ATTENDANCE_CONFIRMATION_DIALOG =
            "gig/resolve_giger_attendance_conflict_screen"

        const val NAV_DESTINATION_ATTENDANCE_DETAILS = "gig/attendance_details"
        const val NAV_DESTINATION_ATTENDANCE = "gig/attendance_details"

        //Monthly Attendance Contstants

    }

    fun openActiveConfirmationDialog(
        gigId: String,
        hasGigerMarkedHimselfInactive: Boolean
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_MARK_ACTIVE_CONFIRMATION_DIALOG,
            bundleOf(
                GigAttendanceConstants.INTENT_EXTRA_GIG_ID to gigId,
                GigAttendanceConstants.INTENT_HAS_GIGER_MARKED_HIMSELF_INACTIVE to hasGigerMarkedHimselfInactive
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openMarkInactiveConfirmationDialog(
        gigId: String,
        hasGigerMarkedHimselfActive: Boolean
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_MARK_INACTIVE_CONFIRMATION_DIALOG,
            bundleOf(
                GigAttendanceConstants.INTENT_EXTRA_GIG_ID to gigId,
                GigAttendanceConstants.INTENT_HAS_GIGER_MARKED_HIMSELF_ACTIVE to hasGigerMarkedHimselfActive
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openMarkInactiveReasonDialog(
        gigId: String
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_SELECT_INACTIVE_REASON_DIALOG,
            bundleOf(
                GigAttendanceConstants.INTENT_EXTRA_GIG_ID to gigId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openResolveAttendanceConflictDialog(
        gigId: String,
        attendanceDetails: GigAttendanceData
    ) {
        navigation.navigateTo(
            NAV_DESTINATION_RESOLVE_ATTENDANCE_CONFIRMATION_DIALOG,
            bundleOf(
                GigAttendanceConstants.INTENT_EXTRA_GIG_ID to gigId,
                GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS to attendanceDetails
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openGigDetailsScreen(
        gigId: String,
        attendanceDetails: GigAttendanceData
    ) = navigation.navigateTo(
        NAV_DESTINATION_ATTENDANCE_DETAILS,
        bundleOf(
            GigAttendanceConstants.INTENT_EXTRA_GIG_ID to gigId,
            GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS to attendanceDetails
        ),
        NavigationOptions.getNavOptions()
    )
}