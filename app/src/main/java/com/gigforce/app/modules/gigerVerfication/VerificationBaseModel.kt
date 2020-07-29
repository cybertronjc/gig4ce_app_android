package com.gigforce.app.modules.gigerVerfication

import androidx.annotation.Keep
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.SelfieVideoDataModel

@Keep
data class VerificationBaseModel(
    var pan_card: PanCardDataModel? = null,
    var aadhar_card: AadharCardDataModel? = null,
    var bank_details: BankDetailsDataModel? = null,
    var driving_license: DrivingLicenseDataModel? = null,
    var selfie_video: SelfieVideoDataModel? = null,
    var sync_status : Boolean = false
)
