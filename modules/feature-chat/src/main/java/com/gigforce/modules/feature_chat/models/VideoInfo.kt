package com.gigforce.modules.feature_chat.models

import android.graphics.Bitmap

data class VideoInfo(
    val name : String = "",
    val duration : Long,
    val size : Long,
    val thumbnail : Bitmap? = null
)