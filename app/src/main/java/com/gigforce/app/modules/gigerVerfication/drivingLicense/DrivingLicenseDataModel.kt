package com.gigforce.app.modules.gigerVerfication.drivingLicense

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class DrivingLicenseDataModel(
    val userHasDL : Boolean? = null,
    val frontImage: String? = null,
    val backImage: String? = null,
    val verified: Boolean = false,
    val dlState : String? = null,
    val dlNo : String? = null
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "driving_license"
        const val KEY_USER_HAS_DL = "userHasDL"
        const val KEY_NAME_FRONT_IMAGE = "frontImage"
        const val KEY_NAME_BACK_IMAGE = "backImage"
        const val KEY_NAME_VERIFIED = "verified"
        const val KEY_DL_NO = "dlNo"
        const val KEY_DL_STATE = "dlState"
    }
}
