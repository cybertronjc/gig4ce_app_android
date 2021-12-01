package com.gigforce.common_ui.viewmodels.verification

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SharedVerificationViewModelEvent {

    data class AadhaarCardInfoSubmitted(
        val aadhaarInfo: AadhaarDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class BankDetailsInfoSubmitted(
        val bankDetails : BankAccountDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class DrivingLicenseInfoSubmitted(
        val drivingLicenseDetails : DrivingLicenseDetailsDataModel
    ) : SharedVerificationViewModelEvent()

    data class PanCardInfoSubmitted(
        val panCardDetails : PanDetailsDataModel
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
        bankName : String,
        ifsc : String,
        bankAccountNumber : String
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.BankDetailsInfoSubmitted(
            BankAccountDetailsDataModel(
                bankName = bankName,
                ifsc = ifsc,
                bankAccountNumber = bankAccountNumber
            )
        )
    )

    fun drivingLicenseInfoSubmitted(
        name : String?,
        drivingLicenseNo : String,
        issueDate : String,
        expiryDate : String,
        dateOfBirth : String
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.DrivingLicenseInfoSubmitted(
            DrivingLicenseDetailsDataModel(
                name = name,
                drivingLicenseNo = drivingLicenseNo,
                issueDate = issueDate,
                expiryDate = expiryDate,
                dateOfBirth = dateOfBirth
            )
        )
    )

    fun panInfoSubmitted(
        panCardImagePath : String,
        panCardNo : String
    ) = _submissionEvents.tryEmit(
        SharedVerificationViewModelEvent.PanCardInfoSubmitted(
            PanDetailsDataModel(
                panCardImagePath = panCardImagePath,
                panCardNo = panCardNo
            )
        )
    )

}