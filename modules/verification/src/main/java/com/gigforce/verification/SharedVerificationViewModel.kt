package com.gigforce.verification

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.PanCardDataModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SharedVerificationViewModelEvent {

    data class AadhaarCardInfoSubmitted(
        val aadhaarInfo: AadhaarDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class BankDetailsInfoSubmitted(
        val aadhaarInfo: AadhaarDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class DrivingLicenseInfoSubmitted(
        val aadhaarInfo: AadhaarDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class PanCardInfoSubmitted(
        val aadhaarInfo: AadhaarDetailsDataModel
    ) : SharedVerificationViewModelEvent()
}

class SharedVerificationViewModel : ViewModel() {

    private val _submissionEvents = MutableSharedFlow<SharedVerificationViewModelEvent>()
    val submissionEvents = _submissionEvents.asSharedFlow()

    fun aadhaarInfoSubmitted(
        aadhaarInfo: AadhaarDetailsDataModel
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted(aadhaarInfo)
    )

    fun bankDetailsSubmitted(
        aadhaarInfo: AadhaarDetailsDataModel
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted(aadhaarInfo)
    )

    fun aadhaarInfoSubmitted(
        aadhaarInfo: AadhaarDetailsDataModel
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted(aadhaarInfo)
    )

    fun panInfoSubmitted(
        aadhaarInfo: PanCardDataModel
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted(aadhaarInfo)
    )

}