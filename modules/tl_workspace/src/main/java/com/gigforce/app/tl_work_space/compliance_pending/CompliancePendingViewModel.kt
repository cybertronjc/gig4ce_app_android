package com.gigforce.app.tl_work_space.compliance_pending

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.domain.models.tl_workspace.compliance.GigersWithPendingComplainceDataItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceComplianceRepository
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData
import com.gigforce.app.tl_work_space.custom_tab.CustomTabClickListener
import com.gigforce.app.tl_work_space.custom_tab.CustomTabData
import com.gigforce.app.tl_work_space.custom_tab.CustomTabDataType1
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
        CompliancePendingViewUiEffects>(initialState = CompliancePendingFragmentUiState.ScreenInitialisedOrRestored),
    CustomTabClickListener {

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
    private var dateDateFilterMaster: List<TLWorkSpaceDateFilterOption> = emptyList()
    private var statusMaster: List<ComplianceStatusData> = emptyList()

    /**
     * Processed Data
     */
    private var gigersComplianceShownOnView: List<CompliancePendingScreenData> = emptyList()

    /**
     *  Current Filters
     */
    private var selectedTab: ComplianceStatusData? = null
    private lateinit var currentlySelectedDateDateFilter: TLWorkSpaceDateFilterOption
    private var searchText: String? = null
    private var collapsedBusiness: ArrayDeque<String> by dequeLimiter(3)


    fun refreshComplianceData(
        dateDateFilter: TLWorkSpaceDateFilterOption?
    ) = viewModelScope.launch {

        if (currentState is CompliancePendingFragmentUiState.LoadingData) {
            logger.d(
                TAG,
                "ignoring refreshComplianceData call, already loading data , no-op"
            )
            return@launch
        }

        setState {
            CompliancePendingFragmentUiState.LoadingData(
                alreadyShowingGigersOnView = rawComplianceGigersList.isNotEmpty()
            )
        }

        try {
            val showSnackBar = rawComplianceGigersList.isNotEmpty()
            val complianceResponse = repository.getComplianceData(
                filter = dateDateFilter?.mapToApiModel()
            )

            dateDateFilterMaster = complianceResponse.filters?.map {
                it.mapToPresentationFilter()
            } ?: emptyList()

            statusMaster = complianceResponse.pendingTypeMaster?.map {
                ComplianceStatusData.fromAPiModel(
                    statusMasterWithCountItem = it,
                    viewModel = this@CompliancePendingViewModel
                )
            } ?: emptyList()
            setDefaultSelectedTabIfNotSet()

            rawComplianceGigersList =
                complianceResponse.gigersWithPendingComplainceData ?: emptyList()

            this@CompliancePendingViewModel.currentlySelectedDateDateFilter =
                dateDateFilter ?: getDefaultDateFilter()
            processRawComplianceDataAndUpdateOnView(showSnackBar)
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    CompliancePendingFragmentUiState.ErrorWhileLoadingData(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {
                logger.e(
                    TAG,
                    "while refreshing compliance data",
                    e
                )
                setState {
                    CompliancePendingFragmentUiState.ErrorWhileLoadingData(
                        "Unable to load data"
                    )
                }
            }
        }
    }

    private fun setDefaultSelectedTabIfNotSet() {
        if (selectedTab != null)
            return

        selectedTab = statusMaster.firstOrNull() ?: return
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
        val statusData = updatedStatusToGigerWithComplianceMap.first
        setState {
            CompliancePendingFragmentUiState.ShowOrUpdateData(
                dateDateFilterSelected = currentlySelectedDateDateFilter,
                complianceData = gigersComplianceShownOnView,
                tabData = statusData
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
                dateDateFilter = currentlySelectedDateDateFilter
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
                filterEvent.dateFilter
            )
            CompliancePendingFragmentViewEvents.FilterApplied.OpenDateFilterDialog -> openDateFilterDialog()
            is CompliancePendingFragmentViewEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                filterEvent.searchText
            )
            is CompliancePendingFragmentViewEvents.FilterApplied.TabSelected -> {}
        }
    }

    private fun openDateFilterDialog() {

        val dateFilterList = dateDateFilterMaster.onEach {
            it.selected = it.filterId == currentlySelectedDateDateFilter.filterId
        }

        setEffect {
            CompliancePendingViewUiEffects.ShowDateFilterBottomSheet(
                dateFilters = dateFilterList
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


    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText
        if (currentState is CompliancePendingFragmentUiState.LoadingData) {
            return
        }

        processRawComplianceDataAndUpdateOnView(
            false
        )
    }

    private fun getDefaultDateFilter(): TLWorkSpaceDateFilterOption {
        return dateDateFilterMaster.find {
            it.default
        } ?: throw IllegalStateException("no default filter found")
    }

    private fun getStatusFromId(
        statusId: String
    ): ComplianceStatusData = statusMaster.find {
        it.id == statusId
    } ?: throw IllegalStateException("status master is empty, there is no filter with $statusId")


    override fun handleCustomTabClick(
        tabClickedType1: CustomTabData
    ) {
        this.selectedTab = getStatusFromId(tabClickedType1.tabId)
        if (currentState is CompliancePendingFragmentUiState.LoadingData) {
            return
        }

        processRawComplianceDataAndUpdateOnView(
            false
        )
    }


}
