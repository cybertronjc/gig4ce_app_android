package com.gigforce.app.tl_work_space.activity_tacker.mark_active_attendance_confirm_check

sealed class MarkActiveViewContract {

    sealed class UiState {

        object ScreenLoaded : UiState()

        object MarkingPresent : UiState()

        object PresentMarkedSuccessfully: UiState()

        data class ErrorWhileMarkingPresent(
            val error : String
        ) : UiState()
    }
}