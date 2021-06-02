package com.gigforce.core.datamodels.verification

import androidx.annotation.Keep
import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class AadharCardDataModel(
    val userHasAadharCard : Boolean? = null,
    val frontImage: String? = null,
    val backImage: String? = null,
    val verified: Boolean = false,
    val aadharCardNo : String? = null,
    val state :Int = -1,
    val verifiedString : String? = null
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "aadhar_card"
        const val KEY_NAME_USER_HAS_AADHAR = "userHasAadharCard"
        const val KEY_NAME_FRONT_IMAGE = "frontImage"
        const val KEY_NAME_BACK_IMAGE = "backImage"
        const val KEY_NAME_VERIFIED = "verified"
        const val KEY_AADHAR_CARD_NO = "aadharCardNo"
    }
}
