package com.gigforce.app.tl_work_space.home

import android.view.View
import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import java.time.LocalDate

sealed class TLWorkSpaceHomeViewContract {

    sealed class TLWorkSpaceHomeUiState : UiState {

        data class LoadingHomeScreenContent(
            val anyPreviousDataShownOnScreen: Boolean
        ) : TLWorkSpaceHomeUiState()

        data class ErrorWhileLoadingScreenContent(
            val error: String
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

        data class FilterSelected(
            val section: TLWorkspaceHomeSection,
            val filterId: String
        ) : TLWorkSpaceHomeUiEvents()

        data class DateSelectedInCustomDateFilter(
            val sectionId: String,
            val filterId: String,
            val date1: LocalDate,
            val date2: LocalDate? // will be non-null in case of range
        ) : TLWorkSpaceHomeUiEvents()
    }

    sealed class TLWorkSpaceHomeViewUiEffects : UiEffect {

        data class ShowFilterDialog(
            val anchorView: View,
            val sectionId: String,
            val filters: List<TLWorkSpaceFilterOption>
        ) : TLWorkSpaceHomeViewUiEffects()

        data class ShowSnackBar(
            val message: String
        ) : TLWorkSpaceHomeViewUiEffects()

        data class OpenDateSelectedDialog(
            val sectionId: String,
            val filterId : String,
            val showRange: Boolean,
            val minDate: LocalDate,
            val maxDate: LocalDate,
            val selectedDate: LocalDate
        ) : TLWorkSpaceHomeViewUiEffects()
    }
}
