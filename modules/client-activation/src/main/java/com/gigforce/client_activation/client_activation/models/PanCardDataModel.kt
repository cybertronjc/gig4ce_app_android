package com.gigforce.client_activation.client_activation.models

import androidx.annotation.Keep
import com.gigforce.core.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class PanCardDataModel(
    val userHasPanCard: Boolean? = null,
    val panCardImagePath: String?= null,
    val verified: Boolean = false,
    val panCardNo : String? = null,
    val state :Int = -1,
    val verifiedString : String? = null
) : BaseFirestoreDataModel(TABLE_NAME){

    companion object {
        const val TABLE_NAME = "pan_card"
        const val KEY_NAME_PASSBOOK_IMAGE_PATH = "panCardImagePath"
        const val KEY_NAME_USER_HAS_PAN_CARD = "userHasPanCard"
        const val KEY_NAME_VERIFIED = "verified"
        const val KEY_PAN_NO = "panCardNo"
    }

}