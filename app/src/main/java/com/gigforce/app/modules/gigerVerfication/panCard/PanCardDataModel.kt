package com.gigforce.app.modules.gigerVerfication.panCard

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class PanCardDataModel(
    val panCardImagePath: String,
    val verified: Boolean
) : BaseFirestoreDataModel(TABLE_NAME){

    companion object {
        const val TABLE_NAME = "pan_card"
        const val KEY_NAME_PASSBOOK_IMAGE_PATH = "panCardImagePath"
        const val KEY_NAME_VERIFIED = "verified"
    }

}