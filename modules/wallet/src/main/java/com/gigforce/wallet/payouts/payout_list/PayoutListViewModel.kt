package com.gigforce.wallet.payouts.payout_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.useCases.payouts.GetPayoutsUseCase
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.wallet.models.PayoutListPresentationItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class PayoutListViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val getPayoutsUseCase: GetPayoutsUseCase
) : ViewModel() {
    companion object {
        const val TAG = "PayoutListViewModel"
    }

    private val _viewState = MutableStateFlow<PayoutListViewContract.State>(
        PayoutListViewContract.State.LoadingPayoutList(null)
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<PayoutListViewContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    // filters
    private var expandedDates: List<String> = emptyList()
    private var activeDateFilters: Pair<LocalDate, LocalDate> = PayoutListFilters.LAST_SIX_MONTHS

    /**
     * Payout list from server as it is
     */
    private var payoutListRaw: List<Payout> = emptyList()
    private var payoutListShownOnScreen: List<PayoutListPresentationItemData> = emptyList()

    init {
        fetchPayouts(PayoutListFilters.LAST_SIX_MONTHS)
    }

    private fun fetchPayouts(
        dateFilter: Pair<LocalDate, LocalDate>
    ) = viewModelScope.launch {
        if (_viewState.value is PayoutListViewContract.State.LoadingPayoutList) {
            logger.d(TAG, "already a loading process in progress, no-op")
            return@launch
        }

        _viewState.emit(PayoutListViewContract.State.LoadingPayoutList(null))
        try {
            activeDateFilters = dateFilter
            payoutListRaw = getPayoutsUseCase.getPayouts(activeDateFilters)
            payoutListShownOnScreen = PayoutListDataProcessor.processPayoutListAndFilters(
                payouts = payoutListRaw,
                expandedDates = expandedDates
            )

            _viewState.emit(
                PayoutListViewContract.State.ShowOrUpdatePayoutListOnView(
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
    }

    private fun payoutItemClicked(
        payoutItem: PayoutListPresentationItemData.PayoutItemRecyclerItemData
    ) {


    }

    private fun monthYearHeaderClicked(
        header: PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData
    ) {

    }

}