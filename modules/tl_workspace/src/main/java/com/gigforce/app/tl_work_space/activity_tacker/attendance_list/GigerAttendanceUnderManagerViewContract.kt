package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.app.tl_work_space.activity_tacker.attendance_details.GigerAttendanceDetailsViewContract
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceTabData
import java.time.LocalDate


sealed class GigerAttendanceUnderManagerViewState  : UiState{

    object ScreenLoaded : GigerAttendanceUnderManagerViewState()

    data class LoadingAttendanceList(
        val message: String?
    ) : GigerAttendanceUnderManagerViewState()

    data class ShowOrUpdateAttendanceListOnView(
        val date: LocalDate,
        val attendanceSwipeControlsEnabled: Boolean,
        val enablePresentSwipeAction: Boolean,
        val enableDeclineSwipeAction: Boolean,
        val attendanceItemData: List<AttendanceRecyclerItemData>,
        val showUpdateToast: Boolean,
        val tabsDataCounts: List<AttendanceTabData>?
    ) : GigerAttendanceUnderManagerViewState()

    data class ErrorInLoadingOrUpdatingAttendanceList(
        val error: String
    ) : GigerAttendanceUnderManagerViewState()
}

sealed class GigerAttendanceUnderManagerViewEvents : UiEvent {

    data class AttendanceItemClicked(
        val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) : GigerAttendanceUnderManagerViewEvents()

    data class AttendanceItemResolveClicked(
        val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) : GigerAttendanceUnderManagerViewEvents()

    data class BusinessHeaderClicked(
        val header: AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData
    ) : GigerAttendanceUnderManagerViewEvents()

    sealed class FiltersApplied : GigerAttendanceUnderManagerViewEvents() {

        data class DateChanged(
            val date: LocalDate
        ) : FiltersApplied()

        data class SearchTextChanged(
            val searchText: String
        ) : FiltersApplied()
    }

    object RefreshAttendanceClicked : GigerAttendanceUnderManagerViewEvents()

    data class UserRightSwipedForMarkingPresent(
        val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) : GigerAttendanceUnderManagerViewEvents()

    data class UserLeftSwipedForMarkingAbsent(
        val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) : GigerAttendanceUnderManagerViewEvents()
}

sealed class GigerAttendanceUnderManagerViewEffect : UiEffect {

    data class ShowErrorUnableToMarkAttendanceForUser(
        val error: String
    ) : GigerAttendanceUnderManagerViewEffect()

    data class ShowGigerDetailsScreen(
        val gigId: String,
        val gigAttendanceData: GigAttendanceData
    ) : GigerAttendanceUnderManagerViewEffect()

    data class ShowResolveAttendanceConflictScreen(
        val gigId: String,
        val gigAttendanceData: GigAttendanceData
    ) : GigerAttendanceUnderManagerViewEffect()

    data class OpenMarkGigerActiveConfirmation(
        val gigId: String,
        val hasGigerMarkedHimselfInActive: Boolean
    ) : GigerAttendanceUnderManagerViewEffect()

    data class OpenMarkInactiveConfirmationDialog(
        val gigId: String
    ) : GigerAttendanceUnderManagerViewEffect()

    data class OpenMarkInactiveSelectReasonDialog(
        val gigId: String,
        val popConfirmationDialog: Boolean = false
    ) : GigerAttendanceUnderManagerViewEffect()
}