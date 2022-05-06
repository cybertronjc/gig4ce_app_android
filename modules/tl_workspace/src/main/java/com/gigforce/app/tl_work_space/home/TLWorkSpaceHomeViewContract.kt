package com.gigforce.app.tl_work_space.home

import android.view.View
import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData

sealed class TLWorkSpaceHomeViewContract {
    sealed class TLWorkSpaceHomeUiState : UiState {
        object LoadingHomeScreenContent : TLWorkSpaceHomeUiState()

        data class ErrorWhileLoadingScreenContent(
            val error : String
        ) : TLWorkSpaceHomeUiState()

        data class ShowOrUpdateSectionListOnView(
            val sectionData: List<TLWorkspaceRecyclerItemData>,
        ) : TLWorkSpaceHomeUiState()
    }

    sealed class TLWorkSpaceHomeUiEvents : UiEvent {

        data class OpenFilter(
            val sectionOpenFilterClickedFrom: TLWorkspaceHomeSection,
            val anchorView: View
        ) : TLWorkSpaceHomeUiEvents()

        data class FilterApplied(
            val section: TLWorkspaceHomeSection,
            val filterApplied : TLWorkSpaceFilterOption
        ) :  TLWorkSpaceHomeUiEvents()
    }

    sealed class TLWorkSpaceHomeViewUiEffects : UiEffect{

        data class ShowFilterDialog(
            val anchorView : View,
            val sectionId : String,
            val filters : List<TLWorkSpaceFilterOption>
        ) : TLWorkSpaceHomeViewUiEffects()


    }
}
