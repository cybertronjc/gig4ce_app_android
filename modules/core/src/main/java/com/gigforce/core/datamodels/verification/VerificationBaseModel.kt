package com.gigforce.core.datamodels.verification

import androidx.annotation.Keep

@Keep
data class VerificationBaseModel(
        var pan_card: PanCardDataModel? = null,
        var aadhar_card: AadharCardDataModel? = null,
        var bank_details: BankDetailsDataModel? = null,
        var driving_license: DrivingLicenseDataModel? = null,
        var selfie_video: SelfieVideoDataModel? = null,
        var sync_status: Boolean = false,
        var contract: Contract? = null,
        var aadhaar_card_questionnaire: AadhaarDetailsDataModel? = null,
        var covid_vaccine_details : CovidVaccineDetailsDataModel?=null,
        var signature: SignatureDataModel? = null
)
