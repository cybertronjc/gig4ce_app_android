package com.gigforce.giger_gigs.attendance_tl.mark_active_attendance_confirm_check

sealed class MarkInactiveReasonsViewContract {

    sealed class UiState {

        object ScreenLoaded : UiState()

        object MarkingPresent : UiState()

        object PresentMarkedSuccessfully: UiState()

        data class ErrorWhileMarkingPresent(
            val error : String
        ) : UiState()
    }
}