package com.gigforce.wallet.payouts.payout_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.useCases.payouts.GetPayoutsUseCase
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.wallet.models.PayoutListPresentationItemData
import com.gigforce.wallet.payouts.payout_list.filter.DateFilterForFilterScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PayoutListViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val getPayoutsUseCase: GetPayoutsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        const val TAG = "PayoutListViewModel"

        const val INTENT_EXTRA_SELECTED_DATE_FILTER = "selected_date_filter"
    }

    private val _viewState = MutableStateFlow<PayoutListViewContract.State>(
        PayoutListViewContract.State.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<PayoutListViewContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    // filters
    private var collapsedDates: MutableList<String> = mutableListOf()
    private var activeDateFilter = PayoutDateFilters.LAST_SIX_MONTHS

    /**
     * Payout list from server as it is
     */
    private var payoutListRaw: List<Payout> = emptyList()
    private var payoutListShownOnScreen: MutableList<PayoutListPresentationItemData> =
        mutableListOf()

    init {
        restoreFiltersAndOtherData()
        fetchPayouts(PayoutDateFilters.LAST_SIX_MONTHS)
    }

    private fun restoreFiltersAndOtherData() {
        val selectedDateFilter =
            savedStateHandle.get<String?>(INTENT_EXTRA_SELECTED_DATE_FILTER) ?: return
        //todo complte thsi
    }

    private fun fetchPayouts(
        dateFilter: PayoutDateFilter
    ) = viewModelScope.launch {
        if (_viewState.value is PayoutListViewContract.State.LoadingPayoutList) {
            logger.d(TAG, "already a loading process in progress, no-op")
            return@launch
        }

        _viewState.emit(PayoutListViewContract.State.LoadingPayoutList(null))
        try {
            activeDateFilter = dateFilter
            payoutListRaw = getPayoutsUseCase.getPayouts(
                activeDateFilter.startEndDatePair
            )
            payoutListShownOnScreen = PayoutListDataProcessor.processPayoutListAndFilters(
                payouts = payoutListRaw,
                collapsedDates = collapsedDates,
                payoutListViewModel = this@PayoutListViewModel
            ).toMutableList()

            _viewState.emit(
                PayoutListViewContract.State.ShowOrUpdatePayoutListOnView(
                    true,
                    payoutListShownOnScreen
                )
            )
        } catch (e: Exception) {
            if (e is IOException) {
                _viewState.emit(
                    PayoutListViewContract.State.ErrorInLoadingOrUpdatingPayoutList(
                        e.message ?: "Unable to fetch payouts"
                    )
                )
            } else {
                _viewState.emit(
                    PayoutListViewContract.State.ErrorInLoadingOrUpdatingPayoutList(
                        "Unable to fetch payouts"
                    )
                )
            }
        }
    }

    fun handleEvent(
        event: PayoutListViewContract.UiEvent
    ) = when (event) {
        is PayoutListViewContract.UiEvent.MonthYearHeaderClicked -> monthYearHeaderClicked(event.header)
        is PayoutListViewContract.UiEvent.PayoutItemClicked -> payoutItemClicked(event.payoutItem)
        PayoutListViewContract.UiEvent.RefreshPayoutListClicked -> fetchPayouts(activeDateFilter)
        is PayoutListViewContract.UiEvent.FiltersApplied -> fetchPayouts(event.dateFilter)
        PayoutListViewContract.UiEvent.OpenFiltersScreen -> prepareFiltersDatesAndOpenPayoutFilterScreen()
    }

    private fun prepareFiltersDatesAndOpenPayoutFilterScreen() = viewModelScope.launch {

        val filters = arrayListOf<DateFilterForFilterScreen>().apply {
            add(
                DateFilterForFilterScreen(
                    date = PayoutDateFilters.LAST_SIX_MONTHS,
                    selected = PayoutDateFilters.LAST_SIX_MONTHS.id == activeDateFilter.id
                )
            )
            add(
                DateFilterForFilterScreen(
                    date = PayoutDateFilters.LAST_ONE_YEAR,
                    selected = PayoutDateFilters.LAST_ONE_YEAR.id == activeDateFilter.id
                )
            )
            add(
                DateFilterForFilterScreen(
                    date = PayoutDateFilters.LAST_FIVE_YEARS,
                    selected = PayoutDateFilters.LAST_FIVE_YEARS.id == activeDateFilter.id
                )
            )
        }

        _viewEffects.emit(PayoutListViewContract.UiEffect.OpenPayoutFiltersScreen(filters))
    }

    private fun payoutItemClicked(
        payoutItem: PayoutListPresentationItemData.PayoutItemRecyclerItemData
    ) = viewModelScope.launch {

        _viewEffects.emit(PayoutListViewContract.UiEffect.OpenPayoutDetailScreen(payoutItem.id))
    }

    private fun monthYearHeaderClicked(
        header: PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData
    ) = viewModelScope.launch {

        if (collapsedDates.contains(header.date)) {
            collapsedDates.remove(header.date)
        } else {
            collapsedDates.add(header.date)
        }

        payoutListShownOnScreen = PayoutListDataProcessor.processPayoutListAndFilters(
            payouts = payoutListRaw,
            collapsedDates = collapsedDates,
            payoutListViewModel = this@PayoutListViewModel
        ).toMutableList()

        _viewState.emit(
            PayoutListViewContract.State.ShowOrUpdatePayoutListOnView(
                false,
                payoutListShownOnScreen
            )
        )
    }

}