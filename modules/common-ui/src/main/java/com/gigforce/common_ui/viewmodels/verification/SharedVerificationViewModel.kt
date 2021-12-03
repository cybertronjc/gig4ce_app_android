package com.gigforce.common_ui.viewmodels.verification

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SharedVerificationViewModelEvent {

    object AadhaarCardInfoSubmitted : SharedVerificationViewModelEvent()

    object BankDetailsInfoSubmitted: SharedVerificationViewModelEvent()

    object DrivingLicenseInfoSubmitted : SharedVerificationViewModelEvent()

    object PanCardInfoSubmitted : SharedVerificationViewModelEvent()

}

class SharedVerificationViewModel : ViewModel() {

    private val _submissionEvents = MutableSharedFlow<SharedVerificationViewModelEvent>()
    val submissionEvents = _submissionEvents.asSharedFlow()

    fun aadhaarInfoSubmitted() = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted
    )

    fun bankDetailsSubmitted() = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.BankDetailsInfoSubmitted
    )

    fun drivingLicenseInfoSubmitted() = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.DrivingLicenseInfoSubmitted
    )

    fun panInfoSubmitted() = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.PanCardInfoSubmitted
    )

}