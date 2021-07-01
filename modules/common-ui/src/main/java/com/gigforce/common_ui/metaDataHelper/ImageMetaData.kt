package com.gigforce.common_ui.metaDataHelper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size

data class ImageMetaData(
        val size: Size,
        val aspectRatio : Float?,
        val length: Long = -1,
        val name :String,
        val mimeType: String?,
        val extension : String?,
        val thumbnail : Bitmap?
)
