package com.gigforce.giger_gigs.attendance_tl.resolve_attendance_conflict

sealed class ResolveAttendanceConflictViewContract {

    sealed class UiState {

        object ScreenLoaded : UiState()

        data class ResolvingConflict(
            val optionSelected : Boolean
        ) : UiState()

        object ConflictResolvedSuccessfully: UiState()

        data class ErrorWhileResolvingConflict(
            val error : String
        ) : UiState()
    }
}