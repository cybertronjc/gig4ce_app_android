package com.gigforce.wallet.payouts.payout_list

import com.gigforce.wallet.models.PayoutListPresentationItemData

class PayoutListViewContract {

    sealed class State {

        data class LoadingPayoutList(
            val message: String?
        ) : State()

        data class ShowOrUpdatePayoutListOnView(
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
    }

    sealed class UiEffect {

        data class OpenPayoutDetailScreen(
            val payoutId : String
        ) : UiEffect()
    }
}