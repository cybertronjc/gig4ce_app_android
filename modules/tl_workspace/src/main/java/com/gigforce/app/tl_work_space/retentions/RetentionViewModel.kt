package com.gigforce.app.tl_work_space.retentions

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.domain.models.tl_workspace.retention.GetRetentionDataRequest
import com.gigforce.app.domain.models.tl_workspace.retention.GigersRetentionListItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceRetentionRepository
import com.gigforce.app.tl_work_space.retentions.models.RetentionStatusData
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
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
    private var rawUpcomingGigerList: List<GigersRetentionListItem> = emptyList()

    /**
     *  Master Data
     */
    private var filterMaster: List<TLWorkSpaceFilterOption> = emptyList()
    private var statusMaster: List<RetentionStatusData> = emptyList()

    /**
     * Processed Data
     */


    private var upcomingGigersShownOnView: List<UpcomingGigersListData> = emptyList()

    private lateinit var selectedTabId: String

    private var searchText: String? = null
    private var currentlySelectedDateFilter: TLWorkSpaceFilterOption? = null

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
                alreadyShowingGigersOnView = rawUpcomingGigerList.isNotEmpty()
            )
        }

        try {
            val showSnackBar = rawUpcomingGigerList.isNotEmpty()
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

            rawUpcomingGigerList = retentionResponse.gigersRetentionList  ?: emptyList()

            this@RetentionViewModel.currentlySelectedDateFilter = dateFilter
            processRawUpcmoningGigersAndUpdateOnView(showSnackBar)
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

    private fun processRawUpcmoningGigersAndUpdateOnView(
        showDataUpdatedSnackbar: Boolean
    ) {
        upcomingGigersShownOnView = RetentionDataProcessor.processRawRetentionDataForListForView(
            rawUpcomingGigerList,
            searchText,
            getDateFilterOptionFromId(selectedTabId),
            this
        )

        setState {
            RetentionFragmentViewContract.RetentionFragmentUiState.ShowOrUpdateRetentionData(
                upcomingGigersShownOnView
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
            RetentionFragmentViewContract.RetentionFragmentViewEvents.RefreshRetentionDataClicked -> refreshGigersData()
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

    }

    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText

        if (currentState is UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers) {
            return
        }
        processRawUpcmoningGigersAndUpdateOnView(
            false
        )
    }

    private fun gigerItemClicked(
        giger: UpcomingGigersListData.UpcomingGigerItemData
    ) {
        setEffect {
            UpcomingGigersViewContract.UpcomingGigersViewUiEffects.OpenGigerDetailsBottomSheet(
                giger
            )
        }
    }

    private fun callGiger(
        giger: UpcomingGigersListData.UpcomingGigerItemData
    ) {
        if (giger.phoneNumber.isNullOrBlank()) {
            return
        }

        setEffect {
            UpcomingGigersViewContract.UpcomingGigersViewUiEffects.DialogPhoneNumber(
                giger.phoneNumber
            )
        }
    }

    private fun getDateFilterOptionFromId(
        filterId: String
    ): TLWorkSpaceFilterOption {

    }


}