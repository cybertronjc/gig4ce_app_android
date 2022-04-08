package com.gigforce.giger_gigs.attendance_tl.attendance_details

import com.gigforce.common_ui.viewdatamodels.gig.GigAttendanceData
import com.gigforce.giger_gigs.GigMonthlyAttendanceFragment
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
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

        object MarkingAttendance : State()

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
            val gigId: String,
            val gigAttendanceData: GigAttendanceData
        ) : UiEffect()

        data class OpenMarkGigerActiveScreen(
            val gigId : String,
            val hasGigerMarkedHimselfInActive : Boolean
        ) : UiEffect()

        data class OpenMarkGigerInActiveConfirmationScreen(
            val gigId : String
        ) : UiEffect()

        data class OpenSelectGigerInActiveReasonScreen(
            val gigId : String
        ) : UiEffect()

        data class CallGiger(
            val phoneNumber : String
        ) : UiEffect()

        data class OpenDropGigerScreen(
            val gigId : String
        ) : UiEffect()

        data class OpenChangeTLScreen(
            val gigId : String,
            val gigerId : String?,
            val gigerName : String?,
            val teamLeaderUid : String
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