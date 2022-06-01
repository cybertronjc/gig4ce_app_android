package com.gigforce.app.tl_work_space.compliance_pending

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData
import kotlin.random.Random


sealed class CompliancePendingFragmentUiState : UiState {

    object ScreenInitialisedOrRestored : CompliancePendingFragmentUiState()

    data class LoadingData(
        val alreadyShowingGigersOnView: Boolean
    ) : CompliancePendingFragmentUiState()

    data class ErrorWhileLoadingData(
        val error: String
    ) : CompliancePendingFragmentUiState()

    data class ShowOrUpdateData(
        val dateDateFilterSelected: TLWorkSpaceDateFilterOption,
        val complianceData: List<CompliancePendingScreenData>,
        val tabData: List<ComplianceStatusData>
    ) : CompliancePendingFragmentUiState() {

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

sealed class CompliancePendingFragmentViewEvents : UiEvent {

    object RefreshDataClicked : CompliancePendingFragmentViewEvents()

    data class BusinessItemClicked(
        val businessId: String
    ) : CompliancePendingFragmentViewEvents()

    data class GigerClicked(
        val giger: CompliancePendingScreenData.GigerItemData
    ) : CompliancePendingFragmentViewEvents()

    data class CallGigerClicked(
        val giger: CompliancePendingScreenData.GigerItemData
    ) : CompliancePendingFragmentViewEvents()

    sealed class FilterApplied : CompliancePendingFragmentViewEvents() {

        object OpenDateFilterDialog : FilterApplied()

        data class TabSelected(
            val tabId: String
        ) : FilterApplied()

        data class SearchFilterApplied(
            val searchText: String?
        ) : FilterApplied()

        data class DateFilterApplied(
            val dateFilter: TLWorkSpaceDateFilterOption
        ) : FilterApplied()
    }
}

sealed class CompliancePendingViewUiEffects : UiEffect {

    data class DialogPhoneNumber(
        val phoneNumber: String
    ) : CompliancePendingViewUiEffects()

    data class OpenGigerDetailsBottomSheet(
        val gigerDetails: CompliancePendingScreenData.GigerItemData
    ) : CompliancePendingViewUiEffects()

    data class ShowSnackBar(
        val message: String
    ) : CompliancePendingViewUiEffects()

    data class ShowDateFilterBottomSheet(
        val dateFilters: List<TLWorkSpaceDateFilterOption>
    ) : CompliancePendingViewUiEffects()
}
