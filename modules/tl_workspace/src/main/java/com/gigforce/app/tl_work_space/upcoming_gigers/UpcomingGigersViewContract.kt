package com.gigforce.app.tl_work_space.upcoming_gigers

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigerItemData

sealed class UpcomingGigersViewContract {

    sealed class UpcomingGigersUiState : UiState {

        data class LoadingGigers(
            val alreadyShowingGigersOnView: Boolean
        ) : UpcomingGigersUiState()

        data class ErrorWhileLoadingScreenContent(
            val error: String
        ) : UpcomingGigersUiState()

        data class ShowOrUpdateSectionListOnView(
            val sectionData: List<UpcomingGigerItemData>,
        ) : UpcomingGigersUiState()
    }

    sealed class UpcomingGigersUiEvents : UiEvent {

        object RefreshUpcomingGigersClicked : UpcomingGigersUiEvents()

        data class GigerClicked(
            val giger: UpcomingGigerItemData
        ) : UpcomingGigersUiEvents()

        data class CallGigerClicked(
            val giger: UpcomingGigerItemData
        ) : UpcomingGigersUiEvents()

        sealed class FilterApplied() : UpcomingGigersUiEvents(){

            data class SearchFilterApplied(
                val searchText: String?
            ) : UpcomingGigersUiEvents()

            data class DateFilterApplied(
                val filter : TLWorkSpaceFilterOption
             ) : UpcomingGigersUiEvents()
        }
    }

    sealed class UpcomingGigersViewUiEffects : UiEffect {

        data class ShowFilterBottomSheet(
            val filters : List<TLWorkSpaceFilterOption>
        ) : UpcomingGigersViewUiEffects()

        data class ShowSnackBar(
            val message: String
        ) : UpcomingGigersViewUiEffects()
    }
}
