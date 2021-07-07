package com.gigforce.common_ui.metaDataHelper

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ImageMetaData(

        @get:PropertyName("size")
        @set:PropertyName("size")
        var size: ImageSize = ImageSize(),

        @get:PropertyName("aspectRatio")
        @set:PropertyName("aspectRatio")
        var aspectRatio: Float? = 0.0f,

        @get:PropertyName("length")
        @set:PropertyName("length")
        var length: Long = -1,

        @get:PropertyName("name")
        @set:PropertyName("name")
        var name: String = "",

        @get:PropertyName("mimeType")
        @set:PropertyName("mimeType")
        var mimeType: String? = null,

        @get:PropertyName("id")
        @set:PropertyName("id")
        var extension: String? = null,

        @get:Exclude
        @set:Exclude
        var thumbnail: Bitmap? = null
) {

    @Exclude
    fun isImageDimensionsNotAvailableOrImageIsSquare(): Boolean {
        return size.height == 0 || size.height == size.width || aspectRatio!! > 0.9f || aspectRatio!! < 1.1f
    }

    @Exclude
    fun isHeightBigger(): Boolean {
        return size.height > size.width
    }

    @Exclude
    fun isAspectRatioTooExtreme(): Boolean {
        val aspectRat = aspectRatio ?: return false
        return aspectRat <= 0.5f || aspectRat >= 1.5f
    }
}

data class ImageSize(

        @get:PropertyName("width")
        @set:PropertyName("width")
        var width: Int = 0,

        @get:PropertyName("height")
        @set:PropertyName("height")
        var height: Int = 0,
)
