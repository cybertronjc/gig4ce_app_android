package com.gigforce.app.tl_work_space.retentions

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GigersRetentionListItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceRetentionRepository
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionStatusData
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
        RetentionFragmentViewContract.RetentionFragmentViewEvents,
        RetentionFragmentViewContract.RetentionFragmentUiState,
        RetentionFragmentViewContract.RetentionFragmentViewUiEffects>
    (
    initialState = RetentionFragmentViewContract.RetentionFragmentUiState.ScreenInitialisedOrRestored
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
    private var statusMaster: List<RetentionStatusData> = emptyList()

    /**
     * Processed Data
     */
    private var gigersRetentionShownOnView: List<RetentionScreenData> = emptyList()

    /**
     *  Current Filters
     */
    private lateinit var selectedTabId: String
    private lateinit var currentlySelectedDateFilter: TLWorkSpaceFilterOption
    private var searchText: String? = null


    init {
        refreshGigersData(null)
    }

    private fun refreshGigersData(
        dateFilter: TLWorkSpaceFilterOption?
    ) = viewModelScope.launch {

        if (currentState is RetentionFragmentViewContract.RetentionFragmentUiState.LoadingRetentionData) {
            logger.d(TAG, "ignoring refreshGigersData call, already loading data , no-op")
            return@launch
        }

        setState {
            RetentionFragmentViewContract.RetentionFragmentUiState.LoadingRetentionData(
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
                RetentionStatusData.fromAPiModel(
                    statusMasterWithCountItem = it,
                    viewModel = this@RetentionViewModel
                )
            } ?: emptyList()

            rawRetentionGigersList = retentionResponse.gigersRetentionList ?: emptyList()

            this@RetentionViewModel.currentlySelectedDateFilter =
                dateFilter ?: getDefaultDateFilter()
            processRawRetentionDataAndUpdateOnView(showSnackBar)
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    RetentionFragmentViewContract.RetentionFragmentUiState.ErrorWhileLoadingRetentionData(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    RetentionFragmentViewContract.RetentionFragmentUiState.ErrorWhileLoadingRetentionData(
                        "Unable to load data"
                    )
                }
            }
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
        gigersRetentionShownOnView = RetentionDataProcessor.processRawRetentionDataForListForView(
            rawUpcomingGigerList = rawRetentionGigersList,
            searchText = searchText,
            dateFilterOptionFromId = getDateFilterOptionFromId(selectedTabId),
            retentionViewModel = this
        )

        setState {
            RetentionFragmentViewContract.RetentionFragmentUiState.ShowOrUpdateRetentionData(
                dateFilterSelected = currentlySelectedDateFilter,
                retentionData = gigersRetentionShownOnView
            )
        }

        if (showDataUpdatedSnackbar) {

            setEffect {
                RetentionFragmentViewContract.RetentionFragmentViewUiEffects.ShowSnackBar(
                    "Retention data updated"
                )
            }
        }
    }

    override fun handleEvent(event: RetentionFragmentViewContract.RetentionFragmentViewEvents) {
        when (event) {
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.CallGigerClicked -> callGiger(
                event.giger
            )
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.GigerClicked -> gigerItemClicked(
                event.giger
            )
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.FilterApplied -> handleFilter(
                event
            )
            RetentionFragmentViewContract.RetentionFragmentViewEvents.RefreshRetentionDataClicked -> refreshGigersData(
                currentlySelectedDateFilter
            )
        }
    }

    private fun handleFilter(event: RetentionFragmentViewContract.RetentionFragmentViewEvents.FilterApplied) {
        when (event) {
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.FilterApplied.DateFilterApplied -> refreshGigersData(
                event.filter
            )
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                event.searchText
            )
            is RetentionFragmentViewContract.RetentionFragmentViewEvents.FilterApplied.TabSelected -> tabSelected(
                event.tabId
            )
        }
    }

    private fun tabSelected(
        tabId: String
    ) {
        this.selectedTabId = tabId
        if (currentState is RetentionFragmentViewContract.RetentionFragmentUiState.LoadingRetentionData) {
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
        if (currentState is RetentionFragmentViewContract.RetentionFragmentUiState.LoadingRetentionData) {
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
            RetentionFragmentViewContract.RetentionFragmentViewUiEffects.OpenGigerDetailsBottomSheet(
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
            RetentionFragmentViewContract.RetentionFragmentViewUiEffects.DialogPhoneNumber(
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