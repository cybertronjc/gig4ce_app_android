package com.gigforce.giger_gigs.attendance_tl.attendance_list

import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.models.GigAttendanceData
import java.time.LocalDate
import kotlin.random.Random

class GigerAttendanceUnderManagerViewContract {

    sealed class State {

        object ScreenLoaded : State()

        data class LoadingAttendanceList(
            val message: String?
        ) : State()

        data class ShowOrUpdateAttendanceListOnView(
            val date : LocalDate,
            val attendanceSwipeControlsEnabled: Boolean,
            val enablePresentSwipeAction: Boolean,
            val enableDeclineSwipeAction: Boolean,
            val attendanceItemData: List<AttendanceRecyclerItemData>,
            val showUpdateToast: Boolean,
            val tabsDataCounts : List<AttendanceStatusAndCountItemData>?
        ) : State()

        data class ErrorInLoadingOrUpdatingAttendanceList(
            val error: String
        ) : State()
    }

    sealed class UiEvent {

        data class AttendanceItemClicked(
            val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        ) : UiEvent()

        data class AttendanceItemResolveClicked(
            val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        ) : UiEvent()

        data class BusinessHeaderClicked(
            val header: AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData
        ) : UiEvent()

        sealed class FiltersApplied : UiEvent(){

            data class TabChanged(
                val tab : String
            ) : FiltersApplied()

            data class DateChanged(
                val date : LocalDate
            ) : FiltersApplied()

            data class SearchTextChanged(
                val searchText : String
            ) : FiltersApplied()
        }

        object RefreshAttendanceClicked : UiEvent()

        data class UserRightSwipedForMarkingPresent(
            val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        ) : UiEvent()

        data class UserLeftSwipedForMarkingAbsent(
            val attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        ) : UiEvent()
    }

    sealed class UiEffect {

        data class ShowErrorUnableToMarkAttendanceForUser(
            val error: String
        ) : UiEffect()

        data class ShowGigerDetailsScreen(
            val gigId : String,
            val gigAttendanceData : GigAttendanceData
        ) : UiEffect()

        data class ShowResolveAttendanceConflictScreen(
            val gigId : String,
            val gigAttendanceData : GigAttendanceData
        ) : UiEffect()

        data class OpenMarkGigerActiveConfirmation(
            val gigId: String,
            val hasGigerMarkedHimselfInActive : Boolean
        ) : UiEffect()

        data class OpenMarkInactiveConfirmationDialog(
            val gigId: String
        ): UiEffect()

        data class OpenMarkInactiveSelectReasonDialog(
            val gigId: String
        ): UiEffect()
    }
}