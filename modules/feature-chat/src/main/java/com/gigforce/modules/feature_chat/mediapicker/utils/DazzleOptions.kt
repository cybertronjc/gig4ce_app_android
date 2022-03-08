package com.gigforce.modules.feature_chat.mediapicker.utils

import java.io.Serializable

class DazzleOptions : Serializable {

    var maxCount = 10
    var allowFrontCamera = true
    var excludeVideos = false
    var maxVideoDuration = 30
    var preSelectedMediaList = ArrayList<String>()
    var cropEnabled = false

    companion object {
        @JvmStatic
        fun init(): DazzleOptions {
            return DazzleOptions()
        }
    }
}