package com.gigforce.app.tl_work_space.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TLWorkspaceHomeViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val tlWorkSpaceHomeScreenRepository: TLWorkSpaceHomeScreenRepository
) : BaseViewModel<
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents,
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState,
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects>
    (initialState = TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent) {

    companion object {
        private const val TAG = "TLWorkspaceHomeViewModel"
    }

    /**
     * Raw Data
     */
    private var tlWorkSpaceDataRaw: List<TLWorkSpaceSectionApiModel> = emptyList()

    /**
     * Processed Data
     */
    private var sectionToSelectedFilterMap: Map<
            TLWorkspaceHomeSection, //Section
            String? //Currently Selected filter's id
            > = mutableMapOf()

    init {
        getTLWorkSpaceData()
    }

    private fun getTLWorkSpaceData() = viewModelScope.launch {

        tlWorkSpaceHomeScreenRepository
            .getWorkspaceSectionAsFlow()
            .collect {


            }
    }

    override fun handleEvent(event: TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents) {
        when (event) {
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.OpenFilter -> handleOpenFilterClicked(
                event.sectionOpenFilterClickedFrom,
                event.anchorView
            )
            else -> {}
        }
    }

    private fun handleOpenFilterClicked(
        sectionOpenFilterClickedFrom: TLWorkspaceHomeSection,
        anchorView: View
    ) {
        val filterSectionId = sectionOpenFilterClickedFrom.getSectionId()
        val doesSectionHaveFilters = false

        if (!doesSectionHaveFilters) {
            logger.w(
                TAG,
                "handleOpenFilterClicked() - called from section $filterSectionId, section doesn't have filters"
            )
            return
        }

        showFilterScreen(
            filterSectionId,
            anchorView
        )
    }

    private fun showFilterScreen(
        filterSectionId: String,
        anchorView: View
    ) {
        val sectionWhereOpenFilterWasTapped = tlWorkSpaceDataRaw.find {
            filterSectionId == it.type
        } ?: return

        val filters = sectionWhereOpenFilterWasTapped.filters?.map {
            it.mapToPresentationFilter()
        } ?: return

        if (filters.isEmpty()) {
            logger.w(
                TAG,
                "showFilterScreen() filters got from $filterSectionId has no items"
            )
            return
        }

        filters.onEach {
            it.selected = it.filterId == sectionToSelectedFilterMap.get(
                TLWorkspaceHomeSection.fromId(it.filterId)
            )
        }

        setEffect {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.ShowFilterDialog(
                anchorView,
                filterSectionId,
                filters
            )
        }
    }
}