package com.gigforce.giger_gigs.attendance_tl.select_decline_reasons

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
    }
}