package com.gigforce.app.tl_work_space.compliance_pending

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.compliance.GigersWithPendingComplainceDataItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceComplianceRepository
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData
import com.gigforce.core.deque.dequeLimiter
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CompliancePendingViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val repository: TLWorkspaceComplianceRepository
) : BaseViewModel<
        CompliancePendingFragmentViewEvents,
        CompliancePendingFragmentUiState,
        CompliancePendingViewUiEffects>(initialState = CompliancePendingFragmentUiState.ScreenInitialisedOrRestored) {

    companion object {
        private const val TAG = "CompliancePendingViewModel"
    }

    /**
     * Raw Data, from Server
     */
    private var rawComplianceGigersList: List<GigersWithPendingComplainceDataItem> = emptyList()

    /**
     * Master Data
     */
    private var dateFilterMaster: List<TLWorkSpaceFilterOption> = emptyList()
    private var statusMaster: List<ComplianceStatusData> = emptyList()

    /**
     * Processed Data
     */
    private var gigersComplianceShownOnView: List<CompliancePendingScreenData> = emptyList()

    /**
     *  Current Filters
     */
    private lateinit var selectedTab: ComplianceStatusData
    private lateinit var currentlySelectedDateFilter: TLWorkSpaceFilterOption
    private var searchText: String? = null
    private var collapsedBusiness: ArrayDeque<String> by dequeLimiter(3)

    init {
        refreshComplianceData(null)
    }

    private fun refreshComplianceData(
        dateFilter: TLWorkSpaceFilterOption?
    ) = viewModelScope.launch {

        if (currentState is CompliancePendingFragmentUiState.LoadingComplianceData) {
            logger.d(
                TAG,
                "ignoring refreshComplianceData call, already loading data , no-op"
            )
            return@launch
        }

        setState {
            CompliancePendingFragmentUiState.LoadingComplianceData(
                alreadyShowingGigersOnView = rawComplianceGigersList.isNotEmpty()
            )
        }

        try {
            val showSnackBar = rawComplianceGigersList.isNotEmpty()
            val complianceResponse = repository.getComplianceData(
                filter = dateFilter?.mapToApiModel()
            )

            dateFilterMaster = complianceResponse.filters?.map {
                it.mapToPresentationFilter()
            } ?: emptyList()

            statusMaster = complianceResponse.pendingTypeMaster?.map {
                ComplianceStatusData.fromAPiModel(
                    statusMasterWithCountItem = it,
                    viewModel = this@CompliancePendingViewModel
                )
            } ?: emptyList()

            rawComplianceGigersList =
                complianceResponse.gigersWithPendingComplainceData ?: emptyList()

            this@CompliancePendingViewModel.currentlySelectedDateFilter =
                dateFilter ?: getDefaultDateFilter()
            processRawComplianceDataAndUpdateOnView(showSnackBar)
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    CompliancePendingFragmentUiState.ErrorWhileLoadingRetentionData(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    CompliancePendingFragmentUiState.ErrorWhileLoadingRetentionData(
                        "Unable to load data"
                    )
                }
            }
        }
    }


    private fun processRawComplianceDataAndUpdateOnView(
        showDataUpdatedSnackbar: Boolean
    ) {

        val updatedStatusToGigerWithComplianceMap =
            ComplianceDataProcessor.processRawComplianceDataForListForView(
                rawComplianceList = rawComplianceGigersList,
                statusMaster = statusMaster,
                searchText = searchText,
                collapsedBusinessId = collapsedBusiness.toList(),
                selectedStatus = selectedTab,
                viewModel = this
            )


        gigersComplianceShownOnView = updatedStatusToGigerWithComplianceMap.second
        setState {
            CompliancePendingFragmentUiState.ShowOrUpdateComplainceData(
                dateFilterSelected = currentlySelectedDateFilter,
                retentionData = gigersComplianceShownOnView
            )
        }

        if (showDataUpdatedSnackbar) {

            setEffect {
                CompliancePendingViewUiEffects.ShowSnackBar(
                    "Compliance data updated"
                )
            }
        }
    }

    override fun handleEvent(
        event: CompliancePendingFragmentViewEvents
    ) {
        when (event) {
            is CompliancePendingFragmentViewEvents.BusinessItemClicked -> handleBusinessItemClicked(
                event.businessId
            )
            is CompliancePendingFragmentViewEvents.CallGigerClicked -> callGiger(
                event.giger
            )
            is CompliancePendingFragmentViewEvents.FilterApplied -> handleFilter(event)
            is CompliancePendingFragmentViewEvents.GigerClicked -> gigerItemClicked(
                event.giger
            )
            CompliancePendingFragmentViewEvents.RefreshDataClicked -> refreshComplianceData(
                dateFilter = currentlySelectedDateFilter
            )
        }
    }

    private fun handleBusinessItemClicked(
        businessId: String
    ) {
        if (collapsedBusiness.contains(businessId)) {
            collapsedBusiness.remove(businessId)
        } else {
            collapsedBusiness.add(businessId)
        }

        processRawComplianceDataAndUpdateOnView(
            false
        )
    }

    private fun handleFilter(
        filterEvent: CompliancePendingFragmentViewEvents.FilterApplied
    ) {
        when (filterEvent) {
            is CompliancePendingFragmentViewEvents.FilterApplied.DateFilterApplied -> refreshComplianceData(
                filterEvent.filter
            )
            CompliancePendingFragmentViewEvents.FilterApplied.OpenDateFilterDialog -> openDateFilterDialog()
            is CompliancePendingFragmentViewEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                filterEvent.searchText
            )
            is CompliancePendingFragmentViewEvents.FilterApplied.TabSelected -> tabSelected(
                filterEvent.tabId
            )
        }
    }

    private fun openDateFilterDialog() {

        val dateFilterList = dateFilterMaster.onEach {
            it.selected = it.filterId == currentlySelectedDateFilter.filterId
        }

        setEffect {
            CompliancePendingViewUiEffects.ShowDateFilterBottomSheet(
                filters = dateFilterList
            )
        }
    }

    private fun callGiger(
        giger: CompliancePendingScreenData.GigerItemData
    ) {
        if (giger.phoneNumber.isNullOrBlank()) {
            return
        }

        setEffect {
            CompliancePendingViewUiEffects.DialogPhoneNumber(
                giger.phoneNumber
            )
        }
    }

    private fun gigerItemClicked(
        giger: CompliancePendingScreenData.GigerItemData
    ) {
        setEffect {
            CompliancePendingViewUiEffects.OpenGigerDetailsBottomSheet(
                giger
            )
        }
    }

    private fun tabSelected(
        tabId: String
    ) {
        this.selectedTab = getStatusFromId(tabId)
        if (currentState is CompliancePendingFragmentUiState.LoadingComplianceData) {
            return
        }

        processRawComplianceDataAndUpdateOnView(
            false
        )
    }


    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText
        if (currentState is CompliancePendingFragmentUiState.LoadingComplianceData) {
            return
        }

        processRawComplianceDataAndUpdateOnView(
            false
        )
    }

    private fun getDefaultDateFilter(): TLWorkSpaceFilterOption {
        return dateFilterMaster.find {
            it.default
        } ?: throw IllegalStateException("no default filter found")
    }

    private fun getStatusFromId(
        statusId: String
    ): ComplianceStatusData = statusMaster.find {
        it.id == statusId
    } ?: throw IllegalStateException("status master is empty, there is no filter with $statusId")


}
