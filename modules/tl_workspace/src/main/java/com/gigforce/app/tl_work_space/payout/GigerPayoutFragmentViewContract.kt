package com.gigforce.app.tl_work_space.payout

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutStatusData

sealed class GigerPayoutFragmentUiState : UiState{

    object ScreenInitialisedOrRestored : GigerPayoutFragmentUiState()

    data class LoadingGigerPayoutData(
        val alreadyShowingGigersOnView: Boolean
    ) : GigerPayoutFragmentUiState()

    data class ErrorWhileLoadingGigerPayoutData(
        val error: String
    ) : GigerPayoutFragmentUiState()

    data class ShowOrUpdateGigerPayoutData(
        val dateFilterSelected: TLWorkSpaceFilterOption?,
        val gigerPayoutData: List<GigerPayoutScreenData>,
        val updatedTabMaster: List<GigerPayoutStatusData>,
    ) : GigerPayoutFragmentUiState()

}

sealed class GigerPayoutFragmentViewEvents : UiEvent {

    object RefreshGigerPayoutDataClicked : GigerPayoutFragmentViewEvents()

    data class GigerClicked(
        val giger: GigerPayoutScreenData.GigerItemData
    ) : GigerPayoutFragmentViewEvents()

    data class CallGigerClicked(
        val giger: GigerPayoutScreenData.GigerItemData
    ) : GigerPayoutFragmentViewEvents()

    data class BusinessClicked(
        val businessData: GigerPayoutScreenData.BusinessItemData
    ) : GigerPayoutFragmentViewEvents()

    sealed class FilterApplied : GigerPayoutFragmentViewEvents() {

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

sealed class GigerPayoutFragmentViewUiEffects : UiEffect {

    data class DialogPhoneNumber(
        val phoneNumber: String
    ) : GigerPayoutFragmentViewUiEffects()

    data class OpenGigerDetailsBottomSheet(
        val gigerDetails: GigerPayoutScreenData.GigerItemData
    ) : GigerPayoutFragmentViewUiEffects()

    data class ShowSnackBar(
        val message: String
    ) : GigerPayoutFragmentViewUiEffects()

    data class ShowDateFilterBottomSheet(
        val filters: List<TLWorkSpaceFilterOption>
    ) : GigerPayoutFragmentViewUiEffects()
}
