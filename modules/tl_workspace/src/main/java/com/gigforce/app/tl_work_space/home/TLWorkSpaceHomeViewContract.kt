package com.gigforce.app.tl_work_space.home

import android.view.View
import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import java.time.LocalDate


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

        object RefreshWorkSpaceDataClicked : TLWorkSpaceHomeUiEvents()

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

        sealed class SectionType1Event : TLWorkSpaceHomeUiEvents() {

            data class InnerCardClicked(
                val innerClickedCard: TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData
            ) : SectionType1Event()
        }

        sealed class SectionType2Event : TLWorkSpaceHomeUiEvents() {

            data class InnerCardClicked(
                val innerClickedCard: TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData
            ) : SectionType1Event()
        }

        sealed class UpcomingGigersSectionEvent : TLWorkSpaceHomeUiEvents() {

            object SeeAllUpcomingGigersClicked : UpcomingGigersSectionEvent()

            data class GigerClicked(
                val giger: TLWorkspaceRecyclerItemData.UpcomingGigerInnerItemData
            ) : UpcomingGigersSectionEvent()
        }


    }

    sealed class TLWorkSpaceHomeViewUiEffects : UiEffect {

        sealed class NavigationEvents : TLWorkSpaceHomeViewUiEffects() {

            data class OpenUpcomingGigersScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenCompliancePendingScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenPayoutScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenActivityTrackerScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenRetentionScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenJoininingScreen(
                val title : String,
                val filter : TLWorkSpaceDateFilterOption?
            ) : NavigationEvents()

            data class OpenGigerDetailsBottomSheet(
                val gigerId : String
            ) : NavigationEvents()
        }

        data class ShowFilterDialog(
            val anchorView: View,
            val sectionId: String,
            val dateFilters: List<TLWorkSpaceDateFilterOption>
        ) : TLWorkSpaceHomeViewUiEffects()

        data class ShowSnackBar(
            val message: String
        ) : TLWorkSpaceHomeViewUiEffects()

        data class OpenDateSelectDialog(
            val sectionId: String,
            val filterId: String,
            val showRange: Boolean,
            val minDate: LocalDate?,
            val maxDate: LocalDate?,
            val selectedDate: LocalDate
        ) : TLWorkSpaceHomeViewUiEffects()


    }