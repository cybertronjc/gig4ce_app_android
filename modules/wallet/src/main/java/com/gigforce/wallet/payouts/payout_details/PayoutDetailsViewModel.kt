package com.gigforce.wallet.payouts.payout_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.useCases.payouts.GetPayoutDetailsUseCase
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PayoutDetailsViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val getPayoutDetailsUseCase: GetPayoutDetailsUseCase,
    private val buildConfigVM: IBuildConfigVM
) : ViewModel() {

    companion object {
        const val TAG = "PayoutDetailsViewModel"
    }

    private val _viewState = MutableStateFlow<PayoutDetailsContract.State>(
        PayoutDetailsContract.State.LoadingPayoutDetails(null)
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<PayoutDetailsContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    // Data
    private lateinit var payoutId: String
    private var payout: Payout? = null

    fun handleEvent(
        event: PayoutDetailsContract.UiEvent
    ) = when (event) {
        PayoutDetailsContract.UiEvent.CallHelpLineClicked -> callHelpLineNumber()
        PayoutDetailsContract.UiEvent.DownloadPayoutPDFClicked -> checkAndDownloadPayoutDocument()
    }

    private fun checkAndDownloadPayoutDocument() = viewModelScope.launch {
        val payoutUrl = payout?.payoutDocumentUrl ?: return@launch
        val businessName = payout?.businessName ?: "-"

        _viewEffects.emit(
            PayoutDetailsContract.UiEffect.StartPayoutDocumentDownload(
                url = payoutUrl,
                businessName = businessName
            )
        )
    }

    private fun callHelpLineNumber() = viewModelScope.launch {
        val helpLineNumber = payout?.helpLineNumber ?: return@launch
        _viewEffects.emit(PayoutDetailsContract.UiEffect.CallHelpLineNo(helpLineNumber))
    }

    fun setPayoutReceivedFromPreviousScreen(
        payoutId: String
    ) = viewModelScope.launch {

        this@PayoutDetailsViewModel.payoutId = payoutId
        fetchPayoutDetails(payoutId)
    }

    private fun fetchPayoutDetails(
        payoutId: String
    ) = viewModelScope.launch {

        _viewState.emit(PayoutDetailsContract.State.LoadingPayoutDetails(null))
        try {

            payout = getPayoutDetailsUseCase.getPayoutDetails(
                payoutId
            )

            _viewState.emit(
                PayoutDetailsContract.State.ShowPayoutDetails(
                    payout!!
                )
            )
        } catch (e: Exception) {

            if (e is IOException) {
                _viewState.emit(
                    PayoutDetailsContract.State.ErrorInLoadingPayoutDetails(
                        e.message ?: "Unable to fetch payout details"
                    )
                )
            } else {
                _viewState.emit(
                    PayoutDetailsContract.State.ErrorInLoadingPayoutDetails(
                        "Unable to fetch payout details"
                    )
                )
            }
        }
    }


}