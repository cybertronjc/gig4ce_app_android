package com.gigforce.app.tl_work_space.activity_tacker.mark_inactive_attendance

import com.gigforce.common_ui.viewdatamodels.gig.DeclineReason

sealed class SelectMarkInactiveReasonsViewContract {

    sealed class UiState {

        //Loading Data states
        object LoadingDeclineOptions : UiState()

        data class ShowDeclineOptions(
            val options : List<DeclineReason>
        ): UiState()

        data class ErrorWhileLoadingDeclineOptions(
            val  error : String
        ) : UiState()


        object MarkingDecline : UiState()

        object DeclineMarkedSuccessfully: UiState()

        data class ErrorWhileMarkingDecline(
            val error : String
        ) : UiState()
    }
}