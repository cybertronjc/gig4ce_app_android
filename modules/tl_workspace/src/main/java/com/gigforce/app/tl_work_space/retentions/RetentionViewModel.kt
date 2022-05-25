package com.gigforce.app.tl_work_space.retentions

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GigersRetentionListItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceRetentionRepository
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionTabData
import com.gigforce.core.deque.dequeLimiter
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RetentionViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val repository: TLWorkspaceRetentionRepository
) : BaseViewModel<
        RetentionFragmentViewEvents,
        RetentionFragmentUiState,
        RetentionFragmentViewUiEffects>
    (
    initialState = RetentionFragmentUiState.ScreenInitialisedOrRestored
) {

    companion object {
        private const val TAG = "RetentionViewModel"
    }

    /**
     * Raw Data, from Server
     */
    private var rawRetentionGigersList: List<GigersRetentionListItem> = emptyList()

    /**
     * Master Data
     */
    private var filterMaster: List<TLWorkSpaceFilterOption> = emptyList()
    private var statusMaster: List<RetentionTabData> = emptyList()

    /**
     * Processed Data
     */
    private var gigersRetentionShownOnView: List<RetentionScreenData> = emptyList()

    /**
     *  Current Filters
     */
    private var selectedTabId: String? = null
    private var currentlySelectedDateFilter: TLWorkSpaceFilterOption? = null
    private var searchText: String? = null
    private var collapsedBusiness: ArrayDeque<String> by dequeLimiter(3)

    init {
        refreshGigersData(null)
    }

    private fun refreshGigersData(
        dateFilter: TLWorkSpaceFilterOption?
    ) = viewModelScope.launch {

        if (currentState is RetentionFragmentUiState.LoadingRetentionData) {
            logger.d(TAG, "ignoring refreshGigersData call, already loading data , no-op")
            return@launch
        }

        setState {
            RetentionFragmentUiState.LoadingRetentionData(
                alreadyShowingGigersOnView = rawRetentionGigersList.isNotEmpty()
            )
        }

        try {
            val showSnackBar = rawRetentionGigersList.isNotEmpty()
            val retentionResponse = repository.getRetentionData(
                GetRetentionDataRequest(
                    filter = dateFilter?.mapToApiModel()
                )
            )

            filterMaster = retentionResponse.filters?.map {
                it.mapToPresentationFilter()
            } ?: emptyList()

            statusMaster = retentionResponse.statusMasterWithCount?.map {
                RetentionTabData.fromAPiModel(
                    statusMasterWithCountItem = it,
                    viewModel = this@RetentionViewModel
                )
            } ?: emptyList()

            rawRetentionGigersList = retentionResponse.gigersRetentionList ?: emptyList()

            setDefaultSeletectedIfNotSet()
            setDefaultDateFilter(dateFilter)

            processRawRetentionDataAndUpdateOnView(showSnackBar)
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    RetentionFragmentUiState.ErrorWhileLoadingRetentionData(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    RetentionFragmentUiState.ErrorWhileLoadingRetentionData(
                        "Unable to load data"
                    )
                }
            }
        }
    }

    private fun setDefaultDateFilter(dateFilter: TLWorkSpaceFilterOption?) {
        this.currentlySelectedDateFilter = dateFilter ?: getDefaultDateFilter()
    }

    private fun setDefaultSeletectedIfNotSet() {
        if (selectedTabId != null)
            return

        selectedTabId = statusMaster.firstOrNull()?.id
    }

    private fun getSelectedTab(): RetentionTabData? {
        return statusMaster.find {
            selectedTabId == it.id
        }
    }

    private fun getDefaultDateFilter(): TLWorkSpaceFilterOption {
        return filterMaster.find {
            it.default
        } ?: throw IllegalStateException("no default filter found")
    }

    private fun processRawRetentionDataAndUpdateOnView(
        showDataUpdatedSnackbar: Boolean
    ) {
        val updatedStatusToGigerWithComplianceMap = RetentionDataProcessor.processRawRetentionDataForListForView(
                rawGigerRetentionList = rawRetentionGigersList,
                searchText = searchText,
                tabMaster = statusMaster,
                collapsedBusinessIds = collapsedBusiness,
                selectedTab = getSelectedTab(),
                retentionViewModel = this
        )
        gigersRetentionShownOnView = updatedStatusToGigerWithComplianceMap.second
        val updatedTabMaster = updatedStatusToGigerWithComplianceMap.first

        setState {
            RetentionFragmentUiState.ShowOrUpdateRetentionData(
                dateFilterSelected = currentlySelectedDateFilter,
                retentionData = gigersRetentionShownOnView,
                updatedTabMaster = updatedTabMaster
            )
        }

        if (showDataUpdatedSnackbar) {

            setEffect {
                RetentionFragmentViewUiEffects.ShowSnackBar(
                    "Retention data updated"
                )
            }
        }
    }

    override fun handleEvent(event: RetentionFragmentViewEvents) {
        when (event) {
            is RetentionFragmentViewEvents.CallGigerClicked -> callGiger(
                event.giger
            )
            is RetentionFragmentViewEvents.GigerClicked -> gigerItemClicked(
                event.giger
            )
            is RetentionFragmentViewEvents.FilterApplied -> handleFilter(
                event
            )
            RetentionFragmentViewEvents.RefreshRetentionDataClicked -> refreshGigersData(
                currentlySelectedDateFilter
            )
        }
    }

    private fun handleFilter(event: RetentionFragmentViewEvents.FilterApplied) {
        when (event) {
            is RetentionFragmentViewEvents.FilterApplied.DateFilterApplied -> refreshGigersData(
                event.filter
            )
            is RetentionFragmentViewEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                event.searchText
            )
            is RetentionFragmentViewEvents.FilterApplied.TabSelected -> tabSelected(
                event.tabId
            )
        }
    }

    private fun tabSelected(
        tabId: String
    ) {
        this.selectedTabId = tabId
        if (currentState is RetentionFragmentUiState.LoadingRetentionData) {
            return
        }

        processRawRetentionDataAndUpdateOnView(
            false
        )
    }

    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText
        if (currentState is RetentionFragmentUiState.LoadingRetentionData) {
            return
        }

        processRawRetentionDataAndUpdateOnView(
            false
        )
    }

    private fun gigerItemClicked(
        giger: RetentionScreenData.GigerItemData
    ) {
        setEffect {
            RetentionFragmentViewUiEffects.OpenGigerDetailsBottomSheet(
                giger
            )
        }
    }

    private fun callGiger(
        giger: RetentionScreenData.GigerItemData
    ) {
        if (giger.phoneNumber.isNullOrBlank()) {
            return
        }

        setEffect {
            RetentionFragmentViewUiEffects.DialogPhoneNumber(
                giger.phoneNumber
            )
        }
    }

    private fun getDateFilterOptionFromId(
        filterId: String
    ): TLWorkSpaceFilterOption = filterMaster.find {
        it.filterId == filterId
    } ?: throw IllegalStateException("status master is empty, there is no filter with $filterId")


}