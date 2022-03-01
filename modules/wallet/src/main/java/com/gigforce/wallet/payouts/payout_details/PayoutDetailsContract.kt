package com.gigforce.wallet.payouts.payout_details

import com.gigforce.common_ui.viewmodels.payouts.Payout

class PayoutDetailsContract {

    sealed class State {

        data class LoadingPayoutDetails(
            val message: String?
        ) : State()

        data class ShowPayoutDetails(
            val payout: Payout
        ) : State()

        data class ErrorInLoadingPayoutDetails(
            val error: String
        ) : State()
    }

    sealed class UiEvent {

        object CallHelpLineClicked : UiEvent()

        object DownloadPayoutPDFClicked : UiEvent()
    }

    sealed class UiEffect {

        data class CallHelpLineNo(
            val phoneNumber: String
        ) : UiEffect()

        data class StartPayoutDocumentDownload(
            val businessName : String,
            val url : String
        ) : UiEffect()
    }
}