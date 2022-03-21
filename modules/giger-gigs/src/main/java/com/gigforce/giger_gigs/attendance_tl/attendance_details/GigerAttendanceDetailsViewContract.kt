package com.gigforce.giger_gigs.attendance_tl.attendance_details

import com.gigforce.giger_gigs.GigMonthlyAttendanceFragment
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.GigAttendanceData
import java.time.LocalDate

class GigerAttendanceDetailsViewContract {

    sealed class State {

        data class LoadingAttendanceDetails(
            val message: String?
        ) : State()

        data class ShowAttendanceDetails(
            val attendanceDetails: GigAttendanceData
        ) : State()

        data class ErrorInLoadingAttendanceDetails(
            val error: String
        ) : State()

        object ResolvingAttendance : State()

        object MarkingPresent : State()

        object MarkingAbsent : State()

        data class ErrorWhileResolvingOrMarkingAttendance(
            val error : String
        ) : State()
    }

    sealed class UiEvent {

        object CallGigerButtonClicked : UiEvent()

        object ChangeTLButtonClicked : UiEvent()

        object AttendanceHistoryClicked : UiEvent()

        object DropGigerClicked : UiEvent()

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

        data class CallGiger(
            val phoneNumber : String
        ) : UiEffect()

        data class OpenDropGigerScreen(
            val gigId : String
        ) : UiEffect()

        data class OpenChangeTLScreen(
            val gigId : String
        ) : UiEffect()

        data class OpenMonthlyAttendanceScreen(
            val gigOrderId : String,
            val date : LocalDate,
            val jobProfile : String?,
            val companyName : String?,
            val companyLogo : String?
        ) : UiEffect()

    }
}