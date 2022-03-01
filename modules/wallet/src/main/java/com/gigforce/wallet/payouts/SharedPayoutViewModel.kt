package com.gigforce.wallet.payouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.wallet.payouts.payout_list.PayoutDateFilter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SharedPayoutViewModelEvents{

    object OpenFilterClicked : SharedPayoutViewModelEvents()

    data class FilterSelected(
        val filter : PayoutDateFilter
    ) : SharedPayoutViewModelEvents()
}

class SharedPayoutViewModel : ViewModel() {

    private val _sharedEvents = MutableSharedFlow<SharedPayoutViewModelEvents>()
    val sharedEvents = _sharedEvents.asSharedFlow()

    fun openPayoutFilter() = viewModelScope.launch{
        _sharedEvents.emit(SharedPayoutViewModelEvents.OpenFilterClicked)
    }

    fun filterSelected(
        dateFilter : PayoutDateFilter
    ) = viewModelScope.launch {
        _sharedEvents.emit(SharedPayoutViewModelEvents.FilterSelected(dateFilter))
    }
}