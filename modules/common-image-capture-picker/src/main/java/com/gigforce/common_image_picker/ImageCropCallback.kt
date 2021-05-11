package com.gigforce.common_image_picker

import android.net.Uri

interface ImageCropCallback {

    fun errorWhileCapturingOrPickingImage(e : Exception)

    fun imageResult(uri : Uri)
}