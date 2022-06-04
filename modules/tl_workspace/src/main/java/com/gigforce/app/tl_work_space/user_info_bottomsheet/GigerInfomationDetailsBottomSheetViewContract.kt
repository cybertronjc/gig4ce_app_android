package com.gigforce.app.tl_work_space.user_info_bottomsheet

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData

sealed class GigerInformationDetailsBottomSheetFragmentViewState : UiState {

    object LoadingGigerInformation : GigerInformationDetailsBottomSheetFragmentViewState()

    data class ShowGigerInformation(
        val viewItems: List<UserInfoBottomSheetData>
    ) : GigerInformationDetailsBottomSheetFragmentViewState()

    data class ErrorWhileFetchingGigerInformation(
        val errorMessage: String
    ) : GigerInformationDetailsBottomSheetFragmentViewState()
}

sealed class GigerInformationDetailsBottomSheetFragmentViewEvents : UiEvent {


    /**
     * Buttons below user picture clicked
     */
    data class ActionButtonClicked(
        val actionButton : UserInfoBottomSheetData.UserInfoActionButtonData
    ) : GigerInformationDetailsBottomSheetFragmentViewEvents()

}

sealed class GigerInformationDetailsBottomSheetFragmentViewEffects : UiEffect {


    /**
     * Buttons below user picture clicked
     */
    data class ActionButtonClicked(
        val actionButton : UserInfoBottomSheetData.UserInfoActionButtonData
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()

}