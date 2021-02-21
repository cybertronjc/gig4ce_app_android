package com.gigforce.app.modules.gigerVerfication.selfieVideo

import androidx.annotation.Keep
import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class SelfieVideoDataModel(
    val videoPath: String = "",
    val verified: Boolean = false
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "selfie_video"
        const val KEY_NAME_VIDEO_PATH = "videoPath"
        const val KEY_NAME_VERIFIED = "verified"
    }
}
