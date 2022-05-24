package com.gigforce.app.tl_work_space.user_info_bottomsheet

import com.gigforce.app.android_common_utils.base.viewModel.UiState

sealed class GigerInformationDetailsBottomSheetFragmentViewState : UiState {

    object LoadingGigerInformation : GigerInformationDetailsBottomSheetFragmentViewState()

    data class ShowGigerInformation(
        val s: String
    ) : GigerInformationDetailsBottomSheetFragmentViewState()

    data class ErrorWhileFetchingGigerInformation(
        val errorMessage: String
    ) : GigerInformationDetailsBottomSheetFragmentViewState()
}

sealed class GigerInformationDetailsBottomSheetFragmentViewEvents : UiState {


    /**
     * Buttons below user picture clicked
     */
    data class TopActionButtonClicked(
        val he : String
    ) : GigerInformationDetailsBottomSheetFragmentViewEvents()
}