package com.gigforce.app.tl_work_space.user_info_bottomsheet

import android.os.Bundle
import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import java.time.LocalDate

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
        val actionButton: UserInfoBottomSheetData.UserInfoActionButtonData
    ) : GigerInformationDetailsBottomSheetFragmentViewEvents()

}

sealed class GigerInformationDetailsBottomSheetFragmentViewEffects : UiEffect {


    /**
     * Buttons below user picture clicked
     */
    data class CallPhoneNumber(
        val phoneNumber: String
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()

    data class DropGiger(
        val jobProfileId: String,
        val gigerId: String
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()

    data class DownloadPayslip(
        val businessName: String,
        val payslipUrl: String
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()

    data class OpenChangeClientIdBottomSheet(
        val existingClientId: String,
        val gigerId: String,
        val gigerMobile: String,
        val gigerName: String,
        val jobProfileId: String,
        val jobProfileName: String,
        val businessId: String
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()


    data class OpenMonthlyAttendanceScreen(
        val gigDate: LocalDate,
        val gigTitle: String,
        val companyLogo: String?,
        val companyName: String,
        val jobProfileId: String,
        val gigerId: String?
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()



    data class OpenChangeTeamLeaderScreen(
        val gigerId: String,
        val gigerName: String,
        val teamLeaderUid: String,
        val jobProfileId: String
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()

    data class NavigateToScreen(
        val route: String,
        val payload: Bundle?
    ) : GigerInformationDetailsBottomSheetFragmentViewEffects()




}