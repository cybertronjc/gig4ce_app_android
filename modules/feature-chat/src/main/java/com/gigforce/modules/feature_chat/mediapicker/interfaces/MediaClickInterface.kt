package com.gigforce.modules.feature_chat.mediapicker.interfaces

import com.gigforce.modules.feature_chat.mediapicker.gallery.MediaModel


interface MediaClickInterface {
    fun onMediaClick(media: MediaModel)
    fun onMediaLongClick(media: MediaModel, intentFrom: String)
}