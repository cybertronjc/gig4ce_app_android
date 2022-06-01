package com.gigforce.app.tl_work_space.payout

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.payout.GetGigerPayoutDataRequest
import com.gigforce.app.domain.models.tl_workspace.payout.GigerPayoutListItem
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspacePayoutRepository
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutStatusData
import com.gigforce.app.tl_work_space.retentions.RetentionFragmentViewEvents
import com.gigforce.app.tl_work_space.retentions.RetentionFragmentViewUiEffects
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.core.deque.dequeLimiter
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GigerPayoutViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val repository: TLWorkspacePayoutRepository
) : BaseViewModel<
        GigerPayoutFragmentViewEvents,
        GigerPayoutFragmentUiState,
        GigerPayoutFragmentViewUiEffects>
    (
    initialState = GigerPayoutFragmentUiState.ScreenInitialisedOrRestored
) {
    companion object {
        private const val TAG = "GigerPayoutViewModel"
    }
    /**
     * Raw Data, from Server
     */
    private var rawGigerPayoutGigersList: List<GigerPayoutListItem> = emptyList()

    /**
     * Master Data
     */
    private var filterMaster: List<TLWorkSpaceFilterOption> = emptyList()
    private var statusMaster: List<GigerPayoutStatusData> = emptyList()

    /**
     * Processed Data
     */
    private var gigersGigerPayoutShownOnView: List<GigerPayoutScreenData> = emptyList()

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

        if (currentState is GigerPayoutFragmentUiState.LoadingGigerPayoutData) {
            logger.d(TAG, "ignoring refreshGigersData call, already loading data , no-op")
            return@launch
        }

        setState {
            GigerPayoutFragmentUiState.LoadingGigerPayoutData(
                alreadyShowingGigersOnView = rawGigerPayoutGigersList.isNotEmpty()
            )
        }

        try {
            val showSnackBar = rawGigerPayoutGigersList.isNotEmpty()
            val gigerPayoutResponse = repository.getGigerPayoutData(
                GetGigerPayoutDataRequest(
                    filter = dateFilter?.mapToApiModel()
                )
            )

            filterMaster = gigerPayoutResponse.filters?.map {
                it.mapToPresentationFilter()
            } ?: emptyList()

            statusMaster = gigerPayoutResponse.pendingTypeMaster?.map {
                GigerPayoutStatusData.fromAPiModel(
                    statusMasterWithCountItem = it,
                    viewModel = this@GigerPayoutViewModel
                )
            } ?: emptyList()

            rawGigerPayoutGigersList = gigerPayoutResponse.gigersWithPayoutData ?: emptyList()

            setDefaultSeletectedIfNotSet()
            setDefaultDateFilter(dateFilter)

            processRawGigerPayoutDataAndUpdateOnView(showSnackBar)
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    GigerPayoutFragmentUiState.ErrorWhileLoadingGigerPayoutData(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    GigerPayoutFragmentUiState.ErrorWhileLoadingGigerPayoutData(
                        "Unable to load data"
                    )
                }
            }
        }
    }

    private fun setDefaultSeletectedIfNotSet() {
        if (selectedTabId != null)
            return

        selectedTabId = statusMaster.firstOrNull()?.id
    }

    private fun setDefaultDateFilter(dateFilter: TLWorkSpaceFilterOption?) {
        this.currentlySelectedDateFilter = dateFilter ?: getDefaultDateFilter()
    }

    private fun getSelectedTab(): GigerPayoutStatusData? {
        return statusMaster.find {
            selectedTabId == it.id
        }
    }

    private fun getDefaultDateFilter(): TLWorkSpaceFilterOption {
        return filterMaster.find {
            it.default
        } ?: throw IllegalStateException("no default filter found")
    }

    private fun processRawGigerPayoutDataAndUpdateOnView(showSnackBar: Boolean) {

        val updatedStatusToGigerWithComplianceMap = GigerPayoutDataProcessor.processRawGigerPayoutDataForListForView(
            rawGigerGigerPayoutList = rawGigerPayoutGigersList,
            searchText = searchText,
            tabMaster = statusMaster,
            collapsedBusinessIds = collapsedBusiness,
            selectedTab = getSelectedTab(),
            retentionViewModel = this
        )
        gigersGigerPayoutShownOnView = updatedStatusToGigerWithComplianceMap.second
        val updatedTabMaster = updatedStatusToGigerWithComplianceMap.first

        setState {
            GigerPayoutFragmentUiState.ShowOrUpdateGigerPayoutData(
                dateFilterSelected = currentlySelectedDateFilter,
                gigerPayoutData = gigersGigerPayoutShownOnView,
                updatedTabMaster = updatedTabMaster
            )
        }

        if (showSnackBar) {

            setEffect {
                GigerPayoutFragmentViewUiEffects.ShowSnackBar(
                    "Giger Payout data updated"
                )
            }
        }
    }

    override fun handleEvent(event: GigerPayoutFragmentViewEvents) {
        when(event) {

            is GigerPayoutFragmentViewEvents.BusinessClicked -> businessHeaderClicker(event.businessData)

            is GigerPayoutFragmentViewEvents.GigerClicked  -> gigerClicked(
                event.giger
            )
            is GigerPayoutFragmentViewEvents.FilterApplied -> handleFilter(
                event
            )
        }
    }

    private fun handleFilter(event: GigerPayoutFragmentViewEvents.FilterApplied) {
        when (event) {
            is GigerPayoutFragmentViewEvents.FilterApplied.DateFilterApplied -> refreshGigersData(
                event.filter
            )
            is GigerPayoutFragmentViewEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                event.searchText
            )
            is GigerPayoutFragmentViewEvents.FilterApplied.TabSelected -> tabSelected(
                event.tabId
            )
        }
    }

    private fun businessHeaderClicker(businessData: GigerPayoutScreenData.BusinessItemData) = viewModelScope.launch{

        if (collapsedBusiness.contains(businessData.businessName)){
            collapsedBusiness.remove(businessData.businessName)
        } else {
            collapsedBusiness.add(businessData.businessName)
        }

        processRawGigerPayoutDataAndUpdateOnView(true)

    }

    private fun tabSelected(
        tabId: String
    ) {
        this.selectedTabId = tabId
        if (currentState is GigerPayoutFragmentUiState.LoadingGigerPayoutData) {
            return
        }

        processRawGigerPayoutDataAndUpdateOnView(
            false
        )
    }

    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText
        if (currentState is GigerPayoutFragmentUiState.LoadingGigerPayoutData) {
            return
        }

        processRawGigerPayoutDataAndUpdateOnView(
            false
        )
    }

    private fun gigerClicked(
        giger: GigerPayoutScreenData.GigerItemData
    ) {
        setEffect {
            GigerPayoutFragmentViewUiEffects.OpenGigerDetailsBottomSheet(
                giger
            )
        }
    }
}