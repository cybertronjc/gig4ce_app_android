package com.gigforce.app.tl_work_space.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.*
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.app.tl_work_space.home.mapper.ApiModelToPresentationModelMapper
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TLWorkspaceHomeViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val tlWorkSpaceHomeScreenRepository: TLWorkSpaceHomeScreenRepository
) : BaseViewModel<
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents,
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState,
        TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects>
    (
    initialState = TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
        anyPreviousDataShownOnScreen = false
    )
) {

    companion object {
        private const val TAG = "TLWorkspaceHomeViewModel"
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
        startObservingDefaultWorkSpaceData()
    }

    private fun startObservingDefaultWorkSpaceData() = viewModelScope.launch {

        tlWorkSpaceHomeScreenRepository
            .getWorkspaceSectionAsFlow()
            .collect {

                when (it) {
                    is Lce.Content -> processDataReceivedFromServerAndUpdateOnView(
                        null,
                        it.content
                    )
                    is Lce.Error -> showErrorState(it.error)
                    Lce.Loading -> showLoadingOnView()
                }
            }
    }

    private fun showErrorState(error: String) {
        setState {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                error = error
            )
        }
    }

    private fun showLoadingOnView() {
        setState {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
                anyPreviousDataShownOnScreen = tlWorkSpaceDataRaw.isNotEmpty()
            )
        }
    }

    fun refreshWorkSpaceData() = viewModelScope.launch {

        if (currentState is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent) {
            logger.d(TAG, "ignoring refreshWorkSpaceData call, already loading data , no-op")
            return@launch
        }

        var anyFilterOtherThanDefaultAppliedToAnySection = false
        sectionToSelectedFilterMap.forEach { (_, filterCurrentlyApplied) ->

            // Checking If there any filter (other than default one) is applied to any section
            if (filterCurrentlyApplied != null && !filterCurrentlyApplied.default) {
                anyFilterOtherThanDefaultAppliedToAnySection = true
            }
        }

        if (anyFilterOtherThanDefaultAppliedToAnySection) {
            //At least one not default filter has been applied to
            //one of the section, so we have to fetch latest data once, without updating the cache
            refreshWorkSpaceDataWithFiltersApplied()
        } else {
            // No Filter (excluding default filter) has beeen applied
            // refreshing data with updating cache
            tlWorkSpaceHomeScreenRepository.refreshCachedWorkspaceSectionData()
        }
    }

    private fun refreshWorkSpaceDataWithFiltersApplied() = viewModelScope.launch {
        val sectionToRefreshWithFiltersInfo = sectionToSelectedFilterMap.map {
            RequestedDataItem(
                filter = it.value?.mapToApiModel(),
                sectionId = it.key.getSectionId()
            )
        }

        setState {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
                tlWorkSpaceDataRaw.isNotEmpty()
            )
        }

        try {
            val rawSectionDataFromServer = tlWorkSpaceHomeScreenRepository.getWorkspaceSectionsData(
                GetTLWorkspaceRequest(
                    defaultRequest = false,
                    requestedData = sectionToRefreshWithFiltersInfo
                )
            )
            processDataReceivedFromServerAndUpdateOnView(
                sectionToRefreshWithFiltersInfo,
                rawSectionDataFromServer
            )
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        e.message ?: "Unable to fetch load data"
                    )
                }
            } else {

                setState {
                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        "Unable to fetch load data"
                    )
                }
            }
        }
    }

    private fun processDataReceivedFromServerAndUpdateOnView(
        filtersUsed: List<RequestedDataItem>?,
        rawSectionDataFromServer: List<TLWorkSpaceSectionApiModel>
    ) {
        this.tlWorkSpaceDataRaw = rawSectionDataFromServer

        val initialFetch = sectionToSelectedFilterMap.isEmpty()
        if (initialFetch) {
            //we need to update one section only
            addFiltersToDefaultSelectedFilter()
        } else {
            updateFiltersInSelectedFilters(
                filtersUsed
            )
        }

        prepareUiModelsAndEmit()
    }

    private fun prepareUiModelsAndEmit() {
        sectionsShownOnView = ApiModelToPresentationModelMapper.mapToPresentationList(
            sectionToSelectedFilterMap,
            tlWorkSpaceDataRaw,
            this
        )

        setState {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ShowOrUpdateSectionListOnView(
                sectionsShownOnView
            )
        }
    }

    private fun updateFiltersInSelectedFilters(filtersUsed: List<RequestedDataItem>?) {
        if (filtersUsed.isNullOrEmpty()) return

        filtersUsed.filter {
            it.filter != null && it.sectionId != null
        }.forEach {
            val section = TLWorkspaceHomeSection.fromId(it.sectionId!!)
            sectionToSelectedFilterMap.put(
                section,
                it.filter!!.mapToPresentationFilter().apply {
                    this.selected = true
                }
            )
        }
    }

    private fun addFiltersToDefaultSelectedFilter() {
        if (sectionToSelectedFilterMap.isEmpty()) {
            /**
             * Preparing a map of section id to default selected filter
             */
            this.tlWorkSpaceDataRaw.forEach {
                sectionToSelectedFilterMap.put(
                    TLWorkspaceHomeSection.fromId(it.sectionId!!),
                    it.filters?.find {
                        it.default!!
                    }?.mapToPresentationFilter().apply {
                        this?.selected = true
                    }
                )
            }
        }
    }

    override fun handleEvent(event: TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents) {
        when (event) {
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.OpenFilter -> handleOpenFilterClicked(
                event.sectionOpenFilterClickedFrom,
                event.anchorView
            )
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.FilterSelected -> handleFilterApplied(
                event.section,
                event.filterId
            )
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter -> handleCustomDateFilter(
                event.sectionId,
                event.filterId,
                event.date1,
                event.date2
            )
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.RefreshWorkSpaceDataClicked -> refreshWorkSpaceData()
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.SectionType1Event.InnerCardClicked -> TODO()
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.SectionType2Event.InnerCardClicked -> TODO()
            is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.GigerClicked -> TODO()
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.SeeAllUpcomingGigersClicked -> openUpcomingGigersScreen()
        }
    }

    private fun openUpcomingGigersScreen() {

        setEffect {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenUpcomingGigersScreen
        }
    }

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
        val sectionWhereOpenFilterWasTapped = tlWorkSpaceDataRaw.find {
            filterSectionId == it.sectionId
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
            )?.filterId
        }

        setEffect {
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.ShowFilterDialog(
                anchorView,
                filterSectionId,
                filters
            )
        }
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