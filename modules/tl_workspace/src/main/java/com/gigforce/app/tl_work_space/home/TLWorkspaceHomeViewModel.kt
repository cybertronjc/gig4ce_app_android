package com.gigforce.app.tl_work_space.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.*
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.app.tl_work_space.home.mapper.ApiModelToPresentationModelMapper
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.extensions.replace
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
        TLWorkSpaceHomeUiEvents,
        TLWorkSpaceHomeUiState,
        TLWorkSpaceHomeViewUiEffects>
    (
    initialState = TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
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
    private var sectionToSelectedDateFilterMap: MutableMap<
            TLWorkspaceHomeSection, //Section
            TLWorkSpaceDateFilterOption? //Currently Selected filter
            > = mutableMapOf()

    init {
        startObservingDefaultWorkSpaceData()
    }

    private fun startObservingDefaultWorkSpaceData() = viewModelScope.launch {

        tlWorkSpaceHomeScreenRepository
            .getWorkspaceSectionAsFlow()
            .collect {
                logger.d(TAG, "new state received : $it")

                when (it) {
                    is Lce.Content -> {

                        val shouldShowDataUpdatedToast = tlWorkSpaceDataRaw.isNotEmpty()
                        tlWorkSpaceDataRaw = it.content

                        processDataReceivedFromServerAndUpdateOnView(
                            null,
                            shouldShowDataUpdatedToast
                        )
                    }
                    is Lce.Error -> showErrorState(it.error)
                    Lce.Loading -> showLoadingOnView()
                }
            }
    }

    private fun showErrorState(error: String) {
        setState {
            TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                error = error
            )
        }
    }

    private fun showLoadingOnView() {
        logger.d(TAG, "emitting loading state...")

        setState {
            TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
                anyPreviousDataShownOnScreen = tlWorkSpaceDataRaw.isNotEmpty()
            )
        }
    }

    fun refreshWorkSpaceData() = viewModelScope.launch {

        if (currentState is TLWorkSpaceHomeUiState.LoadingHomeScreenContent) {
            logger.d(TAG, "ignoring refreshWorkSpaceData call, already loading data , no-op")
            return@launch
        }

        var anyFilterOtherThanDefaultAppliedToAnySection = false
        sectionToSelectedDateFilterMap.forEach { (_, filterCurrentlyApplied) ->

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
            setState {
                TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
                    tlWorkSpaceDataRaw.isNotEmpty()
                )
            }

            tlWorkSpaceHomeScreenRepository.refreshCachedWorkspaceSectionData()
        }
    }

    private fun refreshWorkSpaceDataWithFiltersApplied() = viewModelScope.launch {
        val sectionToRefreshWithFiltersInfo = sectionToSelectedDateFilterMap.map {
            RequestedDataItem(
                filter = it.value?.mapToApiModel(),
                sectionId = it.key.getSectionId()
            )
        }

        setState {
            TLWorkSpaceHomeUiState.LoadingHomeScreenContent(
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
            tlWorkSpaceDataRaw = rawSectionDataFromServer

            processDataReceivedFromServerAndUpdateOnView(
                sectionToRefreshWithFiltersInfo,
                true
            )
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        e.message ?: "Unable to fetch load data"
                    )
                }
            } else {

                setState {
                    TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        "Unable to fetch load data"
                    )
                }
            }
        }
    }

    private fun processDataReceivedFromServerAndUpdateOnView(
        filtersUsed: List<RequestedDataItem>?,
        showDataUpdatedToast: Boolean
    ) {
        logger.d(TAG, "processing data received from server...")

        val initialFetch = sectionToSelectedDateFilterMap.isEmpty()
        if (initialFetch) {
            //we need to update one section only
            addFiltersToDefaultSelectedFilter()
        } else {
            updateFiltersInSelectedFilters(
                filtersUsed
            )
        }

        prepareUiModelsAndEmit()
        if (showDataUpdatedToast) {
            setEffect {
                TLWorkSpaceHomeViewUiEffects.ShowSnackBar("Workspace updated")
            }
        }
    }

    private fun prepareUiModelsAndEmit() {
        sectionsShownOnView = ApiModelToPresentationModelMapper.mapToPresentationList(
            sectionToSelectedDateFilterMap,
            tlWorkSpaceDataRaw,
            this
        )

        logger.d(TAG, "emiting new state...")
        setState {
            TLWorkSpaceHomeUiState.ShowOrUpdateSectionListOnView(
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
            sectionToSelectedDateFilterMap.put(
                section,
                it.filter!!.mapToPresentationFilter().apply {
                    this.selected = true
                }
            )
        }
    }

    private fun addFiltersToDefaultSelectedFilter() {
        if (sectionToSelectedDateFilterMap.isEmpty()) {
            /**
             * Preparing a map of section id to default selected filter
             */
            this.tlWorkSpaceDataRaw.forEach {
                sectionToSelectedDateFilterMap.put(
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

    override fun handleEvent(event: TLWorkSpaceHomeUiEvents) {
        when (event) {
            is TLWorkSpaceHomeUiEvents.OpenFilter -> handleOpenFilterClicked(
                event.sectionOpenFilterClickedFrom,
                event.anchorView
            )
            is TLWorkSpaceHomeUiEvents.FilterSelected -> handleFilterApplied(
                event.section,
                event.filterId
            )
            is TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter -> handleCustomDateFilter(
                event.sectionId,
                event.filterId,
                event.date1,
                event.date2
            )
            TLWorkSpaceHomeUiEvents.RefreshWorkSpaceDataClicked -> refreshWorkSpaceData()
            is TLWorkSpaceHomeUiEvents.SectionType1Event.InnerCardClicked -> handleType1InnerCardClick(
                event.innerClickedCard
            )
            is TLWorkSpaceHomeUiEvents.SectionType2Event.InnerCardClicked -> handleType2InnerCardClicked(
                event.innerClickedCard
            )
            is TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.GigerClicked -> openGigerDetailBottomSheet(
                event.giger.gigerId
            )
            TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.SeeAllUpcomingGigersClicked -> openUpcomingGigersScreen()
        }
    }

    private fun handleType1InnerCardClick(
        cardClicked: TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData
    ) {
        when (TLWorkspaceHomeSection.fromId(cardClicked.sectionId)) {
            TLWorkspaceHomeSection.ACTIVITY_TRACKER -> openActivityTrackerScreen()
            TLWorkspaceHomeSection.UPCOMING_GIGERS -> openUpcomingGigersScreen()
            TLWorkspaceHomeSection.PAYOUT -> openPayoutScreen()
            TLWorkspaceHomeSection.COMPLIANCE_PENDING -> openCompliancePendingScreen()
            TLWorkspaceHomeSection.RETENTION -> openRetentionScreen()
            TLWorkspaceHomeSection.SELECTIONS -> openSelectionListScreen()
        }
    }

    private fun handleType2InnerCardClicked(
        cardClicked: TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData
    ) {
        when (TLWorkspaceHomeSection.fromId(cardClicked.sectionId)) {
            TLWorkspaceHomeSection.ACTIVITY_TRACKER -> openActivityTrackerScreen()
            TLWorkspaceHomeSection.UPCOMING_GIGERS -> openUpcomingGigersScreen()
            TLWorkspaceHomeSection.PAYOUT -> openPayoutScreen()
            TLWorkspaceHomeSection.COMPLIANCE_PENDING -> openCompliancePendingScreen()
            TLWorkspaceHomeSection.RETENTION -> openRetentionScreen()
            TLWorkspaceHomeSection.SELECTIONS -> openSelectionListScreen()
        }
    }

    private fun openGigerDetailBottomSheet(gigerId: String) {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenGigerDetailsBottomSheet(
                gigerId
            )
        }
    }


    private fun getTitleForSection(
        section: TLWorkspaceHomeSection
    ): String {
        return tlWorkSpaceDataRaw.find {
            section.getSectionId() == it.sectionId
        }?.title ?: getDefaultTitleForScreen(section)
    }

    private fun getDefaultTitleForScreen(section: TLWorkspaceHomeSection): String {
        return when (section) {
            TLWorkspaceHomeSection.ACTIVITY_TRACKER -> "Activity Tracker"
            TLWorkspaceHomeSection.UPCOMING_GIGERS -> "Upcoming Gigers"
            TLWorkspaceHomeSection.PAYOUT -> "Payout"
            TLWorkspaceHomeSection.COMPLIANCE_PENDING -> "Compliance Pending"
            TLWorkspaceHomeSection.RETENTION -> "Retention"
            TLWorkspaceHomeSection.SELECTIONS -> "Selections"
            else -> ""
        }
    }


    private fun handleOpenFilterClicked(
        sectionOpenFilterClickedFrom: TLWorkspaceHomeSection,
        anchorView: View
    ) {
        val sectionId = sectionOpenFilterClickedFrom.getSectionId()
        val doesSectionHaveFilters = tlWorkSpaceDataRaw.find {
            sectionOpenFilterClickedFrom.getSectionId() == it.sectionId
        }?.filters?.count() != 0

        if (!doesSectionHaveFilters) {
            logger.w(
                TAG,
                "handleOpenFilterClicked() - called from section $sectionId, section doesn't have filters"
            )
            return
        }

        showFilterScreen(
            sectionId,
            anchorView
        )
    }

    private fun showFilterScreen(
        sectionId: String,
        anchorView: View
    ) {
        val sectionWhereOpenFilterWasTapped = tlWorkSpaceDataRaw.find {
            sectionId == it.sectionId
        } ?: return

        val filters = sectionWhereOpenFilterWasTapped.filters?.map {
            it.mapToPresentationFilter()
        } ?: return

        if (filters.isEmpty()) {
            logger.w(
                TAG,
                "showFilterScreen() filters got from $sectionId has no items"
            )
            return
        }

        filters.onEach {
            it.selected = it.filterId == sectionToSelectedDateFilterMap.get(
                TLWorkspaceHomeSection.fromId(sectionId)
            )?.filterId
        }

        setEffect {
            TLWorkSpaceHomeViewUiEffects.ShowFilterDialog(
                anchorView,
                sectionId,
                filters
            )
        }
    }

    private fun handleFilterApplied(
        section: TLWorkspaceHomeSection,
        filterId: String
    ) = viewModelScope.launch {

        val selectedFilter = getFilterFrom(
            section,
            filterId
        ) ?: return@launch

        if (selectedFilter.customDateOrRangeFilter) {

            setEffect {
                TLWorkSpaceHomeViewUiEffects.OpenDateSelectDialog(
                    sectionId = section.getSectionId(),
                    filterId = selectedFilter.filterId,
                    showRange = selectedFilter.selectRangeInFilter,
                    minDate = selectedFilter.minimumDateAvailableForSelection,
                    maxDate = selectedFilter.maximumDateAvailableForSelection,
                    selectedDate = selectedFilter.defaultSelectedDate ?: LocalDate.now()
                )
            }

            return@launch
        }

        updatedSingleSection(
            section,
            selectedFilter
        )
    }

    private fun handleCustomDateFilter(
        sectionId: String,
        filterId: String,
        date1: LocalDate,
        date2: LocalDate?
    ) = viewModelScope.launch {
        val section = TLWorkspaceHomeSection.fromId(sectionId)

        val selectedFilter = getFilterFrom(
            section,
            filterId
        ) ?: return@launch

        selectedFilter.startDate = date1
        selectedFilter.endDate = date2

        updatedSingleSection(
            section,
            selectedFilter
        )
    }

    private suspend fun updatedSingleSection(
        section: TLWorkspaceHomeSection,
        tlWorkSpaceDateFilterOption: TLWorkSpaceDateFilterOption?
    ) {

        setState {
            TLWorkSpaceHomeUiState.LoadingHomeScreenContent(true)
        }

        try {
            val singleSectionData = tlWorkSpaceHomeScreenRepository.getSingleWorkSpaceSectionData(
                RequestedDataItem(
                    filter = tlWorkSpaceDateFilterOption?.mapToApiModel(),
                    sectionId = section.getSectionId()
                )
            )

            tlWorkSpaceDataRaw.replace(
                singleSectionData
            ) {
                section.getSectionId() == it.sectionId
            }

            sectionToSelectedDateFilterMap.put(
                section,
                tlWorkSpaceDateFilterOption
            )

            processDataReceivedFromServerAndUpdateOnView(
                filtersUsed = listOf(
                    RequestedDataItem(
                        filter = tlWorkSpaceDateFilterOption?.mapToApiModel(),
                        sectionId = section.getSectionId()
                    )
                ),
                showDataUpdatedToast = true
            )
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        e.message ?: "Unable to fetch load data"
                    )
                }
            } else {

                setState {
                    TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent(
                        "Unable to fetch load data"
                    )
                }
            }
        }

    }


    private fun getFilterFrom(
        section: TLWorkspaceHomeSection,
        filterId: String
    ): TLWorkSpaceDateFilterOption? {
        return tlWorkSpaceDataRaw.find {
            section.getSectionId() == it.sectionId
        }?.filters?.find {
            filterId == it.filterId
        }?.mapToPresentationFilter()

    }

    private fun openSelectionListScreen() {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenJoininingScreen(
                getTitleForSection(TLWorkspaceHomeSection.SELECTIONS)
            )
        }
    }

    private fun openRetentionScreen() {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenRetentionScreen(
                getTitleForSection(TLWorkspaceHomeSection.RETENTION)
            )
        }
    }

    private fun openCompliancePendingScreen() {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenCompliancePendingScreen(
                getTitleForSection(TLWorkspaceHomeSection.COMPLIANCE_PENDING)
            )
        }
    }

    private fun openActivityTrackerScreen() {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenActivityTrackerScreen(
                getTitleForSection(TLWorkspaceHomeSection.ACTIVITY_TRACKER)
            )
        }
    }

    private fun openPayoutScreen() {
        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenPayoutScreen(
                getTitleForSection(TLWorkspaceHomeSection.PAYOUT)
            )
        }
    }

    private fun openUpcomingGigersScreen() {

        setEffect {
            TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenUpcomingGigersScreen(
                getTitleForSection(TLWorkspaceHomeSection.UPCOMING_GIGERS)
            )
        }
    }
}