package com.gigforce.app.tl_work_space.upcoming_gigers

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.domain.models.tl_workspace.*
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceUpcomingGigersRepository
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
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
    private var rawUpcomingGigerList: List<UpcomingGigersApiModel> = emptyList()

    /**
     * Processed Data
     */
    private var upcomingGigersShownOnView: List<UpcomingGigersListData> = emptyList()
    private var searchText: String? = null

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
                alreadyShowingGigersOnView = rawUpcomingGigerList.isNotEmpty()
            )
        }


        try {


            rawUpcomingGigerList = repository.getUpcomingGigers()
            processDataReceivedFromServerAndUpdateOnView()
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

    private  fun processDataReceivedFromServerAndUpdateOnView() {
        upcomingGigersShownOnView = UpcomingGigersListProcessor.processRawUpcomingListForView(
            rawUpcomingGigerList,
            searchText
        )

        setState {
            UpcomingGigersViewContract.UpcomingGigersUiState.ShowOrUpdateSectionListOnView(
                upcomingGigersShownOnView
            )
        }
    }

    override fun handleEvent(event: UpcomingGigersViewContract.UpcomingGigersUiEvents) {
        when (event) {
            is UpcomingGigersViewContract.UpcomingGigersUiEvents.CallGigerClicked -> callGiger(
                event.giger
            )
            is UpcomingGigersViewContract.UpcomingGigersUiEvents.GigerClicked -> gigerItemClicked(
                event.giger
            )
            UpcomingGigersViewContract.UpcomingGigersUiEvents.RefreshUpcomingGigersClicked -> refreshGigersData()
            is UpcomingGigersViewContract.UpcomingGigersUiEvents.FilterApplied.SearchFilterApplied -> searchFilterApplied(
                event.searchText
            )
        }
    }

    private fun searchFilterApplied(
        searchText: String?
    ) {
        this.searchText = searchText

        if (currentState is UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers) {
            return
        }
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


}