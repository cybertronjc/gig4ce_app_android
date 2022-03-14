package com.gigforce.giger_gigs.attendance_tl.attendance_details

import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class GigerAttendanceDetailsViewContract {

    sealed class State {

        data class LoadingAttendanceDetails(
            val message: String?
        ) : State()

        data class ShowAttendanceDetails(
            val attendanceDetails: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        ) : State()

        data class ErrorInLoadingAttendanceDetails(
            val error: String
        ) : State()
    }

    sealed class UiEvent {

        object ResolveButtonClicked : UiEvent()

        object InactiveButtonClicked : UiEvent()

        object ActiveButtonClicked : UiEvent()
    }

    sealed class UiEffect {

        data class OpenResolveAttendanceScreen(
            val gigId: String
        ) : UiEffect()

        data class OpenMarkGigerActiveScreen(
            val gigId : String
        ) : UiEffect()

        data class OpenMarkGigerInActiveScreen(
            val gigId : String
        ) : UiEffect()
    }
}