package com.gigforce.app.modules.gigerVerfication.aadharCard

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class AadharCardDataModel(
    val userHasAadharCard : Boolean,
    val frontImage: String? = null,
    val backImage: String? = null,
    val verified: Boolean = false
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "aadhar_card"
        const val KEY_NAME_USER_HAS_AADHAR = "userHasAadharCard"
        const val KEY_NAME_FRONT_IMAGE = "frontImage"
        const val KEY_NAME_BACK_IMAGE = "backImage"
        const val KEY_NAME_VERIFIED = "verified"
    }
}
