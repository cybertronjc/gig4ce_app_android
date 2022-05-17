package com.gigforce.app.tl_work_space.upcoming_gigers

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData

sealed class UpcomingGigersViewContract {

    sealed class UpcomingGigersUiState : UiState {

        object ScreenInitialisedOrRestored : UpcomingGigersUiState()

        data class LoadingGigers(
            val alreadyShowingGigersOnView: Boolean
        ) : UpcomingGigersUiState()

        data class ErrorWhileLoadingScreenContent(
            val error: String
        ) : UpcomingGigersUiState()

        data class ShowOrUpdateSectionListOnView(
            val upcomingGigers: List<UpcomingGigersListData>,
        ) : UpcomingGigersUiState()
    }

    sealed class UpcomingGigersUiEvents : UiEvent {

        object RefreshUpcomingGigersClicked : UpcomingGigersUiEvents()

        data class GigerClicked(
            val giger: UpcomingGigersListData.UpcomingGigerItemData
        ) : UpcomingGigersUiEvents()

        data class CallGigerClicked(
            val giger: UpcomingGigersListData.UpcomingGigerItemData
        ) : UpcomingGigersUiEvents()

        sealed class FilterApplied : UpcomingGigersUiEvents() {

            data class SearchFilterApplied(
                val searchText: String?
            ) : UpcomingGigersUiEvents()

        }
    }

    sealed class UpcomingGigersViewUiEffects : UiEffect {

        data class DialogPhoneNumber(
            val phoneNumber: String
        ) : UpcomingGigersViewUiEffects()

        data class OpenGigerDetailsBottomSheet(
            val gigerDetails: UpcomingGigersListData.UpcomingGigerItemData
        ) : UpcomingGigersViewUiEffects()

        data class ShowSnackBar(
            val message: String
        ) : UpcomingGigersViewUiEffects()
    }
}
