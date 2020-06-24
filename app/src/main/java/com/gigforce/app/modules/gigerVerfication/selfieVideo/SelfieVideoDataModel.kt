package com.gigforce.app.modules.gigerVerfication.selfieVideo

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationDatabaseConstants.SELFIE_VIDEO_PARENT_NAME

@Keep
data class SelfieVideoDataModel(
    val videoPath: String
) : BaseFirestoreDataModel(SELFIE_VIDEO_PARENT_NAME)
