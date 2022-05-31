package com.gigforce.app.tl_work_space.retentions

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionTabData
import kotlin.random.Random


sealed class RetentionFragmentUiState : UiState {

    object ScreenInitialisedOrRestored : RetentionFragmentUiState()

    data class LoadingRetentionData(
        val alreadyShowingGigersOnView: Boolean
    ) : RetentionFragmentUiState()

    data class ErrorWhileLoadingRetentionData(
        val error: String
    ) : RetentionFragmentUiState()

    data class ShowOrUpdateRetentionData(
        val dateFilterSelected: TLWorkSpaceFilterOption?,
        val retentionData: List<RetentionScreenData>,
        val updatedTabMaster: List<RetentionTabData>,
    ) : RetentionFragmentUiState(){

        override fun equals(other: Any?): Boolean {
            return false
        }

        override fun hashCode(): Int {
            return Random.nextInt()
        }
    }

    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }
}

sealed class RetentionFragmentViewEvents : UiEvent {

    object RefreshRetentionDataClicked : RetentionFragmentViewEvents()

    data class GigerClicked(
        val giger: RetentionScreenData.GigerItemData
    ) : RetentionFragmentViewEvents()

    data class BusinessClicked(
        val business: RetentionScreenData.BusinessItemData
    ) : RetentionFragmentViewEvents()

    data class CallGigerClicked(
        val giger: RetentionScreenData.GigerItemData
    ) : RetentionFragmentViewEvents()

    object OpenDateFilterIconClicked : RetentionFragmentViewEvents()

    sealed class FilterApplied : RetentionFragmentViewEvents() {

        data class TabSelected(
            val tabId: String
        ) : FilterApplied()

        data class SearchFilterApplied(
            val searchText: String?
        ) : FilterApplied()

        data class DateFilterApplied(
            val filter: TLWorkSpaceFilterOption
        ) : FilterApplied()
    }
}

sealed class RetentionFragmentViewUiEffects : UiEffect {

    data class DialogPhoneNumber(
        val phoneNumber: String
    ) : RetentionFragmentViewUiEffects()

    data class OpenGigerDetailsBottomSheet(
        val gigerDetails: RetentionScreenData.GigerItemData
    ) : RetentionFragmentViewUiEffects()

    data class ShowSnackBar(
        val message: String
    ) : RetentionFragmentViewUiEffects()

    data class ShowDateFilterBottomSheet(
        val filters: List<TLWorkSpaceFilterOption>
    ) : RetentionFragmentViewUiEffects()
}
