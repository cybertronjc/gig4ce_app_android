package com.gigforce.modules.feature_chat.mediapicker.gallery

import android.net.Uri
import androidx.annotation.Keep

@Keep
data class MediaModel(var mMediaUri: Uri?, var mMediaType: Int, var mMediaDate: String){
    var isSelected = false
}