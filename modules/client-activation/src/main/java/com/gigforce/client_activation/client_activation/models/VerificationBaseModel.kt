package com.gigforce.client_activation.client_activation.models

import androidx.annotation.Keep
import org.jetbrains.annotations.Contract

@Keep
data class VerificationBaseModel(
        var pan_card: PanCardDataModel? = null,
        var aadhar_card: AadharCardDataModel? = null,
        var bank_details: BankDetailsDataModel? = null,
        var driving_license: DrivingLicenseDataModel? = null,
        var selfie_video: SelfieVideoDataModel? = null,
        var sync_status: Boolean = false,
        var contract: Contract? = null
)
