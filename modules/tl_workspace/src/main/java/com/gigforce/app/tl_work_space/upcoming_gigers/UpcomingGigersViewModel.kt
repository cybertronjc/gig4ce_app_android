package com.gigforce.app.tl_work_space.upcoming_gigers

import android.view.View
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.*
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceUpcomingGigersRepository
import com.gigforce.app.tl_work_space.home.mapper.ApiModelToPresentationModelMapper
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UpcomingGigersViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val repository: TLWorkspaceUpcomingGigersRepository
) : BaseViewModel<
        UpcomingGigersViewContract.UpcomingGigersUiEvents,
        UpcomingGigersViewContract.UpcomingGigersUiState,
        UpcomingGigersViewContract.UpcomingGigersViewUiEffects>
    (
    initialState = UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers(
        alreadyShowingGigersOnView = false
    )
) {

    companion object {
        private const val TAG = "UpcomingGigersViewModel"
    }

    /**
     * Raw Data, from Server
     */
    private var tlWorkSpaceDataRaw: List<TLWorkSpaceSectionApiModel> = emptyList()

    /**
     * Processed Data
     */
    private var sectionsShownOnView: List<TLWorkspaceRecyclerItemData> = emptyList()
    private var sectionToSelectedFilterMap: MutableMap<
            TLWorkspaceHomeSection, //Section
            TLWorkSpaceFilterOption? //Currently Selected filter
            > = mutableMapOf()

    init {
        refreshGigersData()
    }

    private fun refreshGigersData() = viewModelScope.launch {

        if (currentState is UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers) {
            logger.d(TAG, "ignoring refreshGigersData call, already loading data , no-op")
            return@launch
        }

        setState {
            UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers(
                alreadyShowingGigersOnView = tlWorkSpaceDataRaw.isNotEmpty()
            )
        }


        try {


//            val rawSectionDataFromServer = tlWorkSpaceHomeScreenRepository.getWorkspaceSectionsData(
//                GetTLWorkspaceRequest(
//                    defaultRequest = false,
//                    requestedData = sectionToRefreshWithFiltersInfo
//                )
//            )
//            processDataReceivedFromServerAndUpdateOnView(
//                sectionToRefreshWithFiltersInfo,
//                rawSectionDataFromServer
//            )
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    UpcomingGigersViewContract.UpcomingGigersUiState.ErrorWhileLoadingScreenContent(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    UpcomingGigersViewContract.UpcomingGigersUiState.ErrorWhileLoadingScreenContent(
                        "Unable to load data"
                    )
                }
            }
        }
    }

    private fun processDataReceivedFromServerAndUpdateOnView(
        filtersUsed: List<RequestedDataItem>?,
        rawSectionDataFromServer: List<TLWorkSpaceSectionApiModel>
    ) {
//        this.tlWorkSpaceDataRaw = rawSectionDataFromServer
//
//        sectionsShownOnView = ApiModelToPresentationModelMapper.mapToPresentationList(
//            sectionToSelectedFilterMap,
//            tlWorkSpaceDataRaw,
//            this
//        )

        setState {
            UpcomingGigersViewContract.UpcomingGigersUiState.ShowOrUpdateSectionListOnView(
                sectionsShownOnView
            )
        }
    }

    override fun handleEvent(event: UpcomingGigersViewContract.UpcomingGigersUiEvents) {
        TODO("Not yet implemented")
    }

//    override fun handleEvent(event: TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents) {
//        when (event) {
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.OpenFilter -> handleOpenFilterClicked(
//                event.sectionOpenFilterClickedFrom,
//                event.anchorView
//            )
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.FilterSelected -> handleFilterApplied(
//                event.section,
//                event.filterId
//            )
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter -> handleCustomDateFilter(
//                event.sectionId,
//                event.filterId,
//                event.date1,
//                event.date2
//            )
//            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.RefreshWorkSpaceDataClicked -> refreshGigersData()
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.SectionType1Event.InnerCardClicked -> TODO()
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.SectionType2Event.InnerCardClicked -> TODO()
//            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.GigerClicked -> TODO()
//            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.SeeAllUpcomingGigersClicked -> TODO()
//        }
//    }

    private fun handleCustomDateFilter(
        sectionId: String,
        filterId: String,
        date1: LocalDate,
        date2: LocalDate?
    ) {

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
//        val sectionWhereOpenFilterWasTapped = tlWorkSpaceDataRaw.find {
//            filterSectionId == it.sectionId
//        } ?: return
//
//        val filters = sectionWhereOpenFilterWasTapped.filters?.map {
//            it.mapToPresentationFilter()
//        } ?: return
//
//        if (filters.isEmpty()) {
//            logger.w(
//                TAG,
//                "showFilterScreen() filters got from $filterSectionId has no items"
//            )
//            return
//        }
//
//        filters.onEach {
//            it.selected = it.filterId == sectionToSelectedFilterMap.get(
//                TLWorkspaceHomeSection.fromId(it.filterId)
//            )?.filterId
//        }
//
//        setEffect {
//            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.ShowFilterDialog(
//                anchorView,
//                filterSectionId,
//                filters
//            )
//        }
    }

    private fun handleFilterApplied(
        section: TLWorkspaceHomeSection,
        filterId: String
    ) = viewModelScope.launch {

//       val updatedFilterData =  tlWorkSpaceHomeScreenRepository.getSingleWorkSpaceSectionData(
//            RequestedDataItem(
//                filter = filterApplied.mapToApiModel(),
//                sectionId = section.getSectionId()
//            )
//        )
    }
}