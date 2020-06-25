package com.gigforce.app.modules.gigerVerfication.selfieVideo

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class SelfieVideoDataModel(
    val videoPath: String
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "selfie_video"
        const val KEY_NAME_VIDEO_PATH = "videoPath"
        const val KEY_NAME_VERIFIED = "verified"
    }
}
