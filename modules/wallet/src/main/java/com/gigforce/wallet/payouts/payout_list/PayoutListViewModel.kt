package com.gigforce.wallet.payouts.payout_list

import androidx.lifecycle.ViewModel
import com.gigforce.wallet.models.PayoutListPresentationItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PayoutListViewModel @Inject constructor(

): ViewModel() {

    private val _viewState = MutableStateFlow<PayoutListViewContract.State>(PayoutListViewContract.State.LoadingPayoutList(null))
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<PayoutListViewContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    fun handleEvent(
        event : PayoutListViewContract.UiEvent
    ) = when(event){
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