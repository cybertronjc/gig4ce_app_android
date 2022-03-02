package com.gigforce.wallet.payouts.payout_list

import com.gigforce.wallet.models.PayoutListPresentationItemData
import com.google.android.material.snackbar.Snackbar

class PayoutListViewContract {

    sealed class State {

        object ScreenLoaded: State()

        data class LoadingPayoutList(
            val message: String?
        ) : State()

        data class ShowOrUpdatePayoutListOnView(
            val showUpdateSnackbar: Boolean,
            val payouts: List<PayoutListPresentationItemData>
        ) : State()

        data class ErrorInLoadingOrUpdatingPayoutList(
            val error: String
        ) : State()
    }

    sealed class UiEvent{

        data class PayoutItemClicked(
            val payoutItem : PayoutListPresentationItemData.PayoutItemRecyclerItemData
        ) : UiEvent()

        data class MonthYearHeaderClicked(
            val header : PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData
        ) : UiEvent()

        data class FiltersApplied(
            val dateFilter : PayoutDateFilter
        ) : UiEvent()

        object OpenFiltersScreen : UiEvent()

        object RefreshPayoutListClicked : UiEvent()
    }

    sealed class UiEffect {

        data class OpenPayoutDetailScreen(
            val payoutId : String
        ) : UiEffect()

        data class OpenPayoutFiltersScreen(
            val filters : ArrayList<DateFilterForFilterScreen>
        ) : UiEffect()
    }
}