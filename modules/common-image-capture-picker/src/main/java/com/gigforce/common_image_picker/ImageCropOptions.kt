package com.gigforce.common_image_picker

import android.net.Uri

class ImageCropOptions private constructor(builder: Builder) {

    val shouldOpenImageCropper: Boolean = builder.shouldOpenImageCropper
    val shouldDetectForFace: Boolean = builder.shouldDetectForFace
    val outputFileUri : Uri? = builder.outputFileUri
    val freeCropEnabled : Boolean = builder.freeCropEnabled

    class Builder() {

        var shouldOpenImageCropper: Boolean = false
        var shouldDetectForFace: Boolean = false
        var outputFileUri : Uri? = null
        var freeCropEnabled : Boolean = false

        fun build(): ImageCropOptions {
            return ImageCropOptions(this)
        }

        fun shouldOpenImageCrop(openImageCropper: Boolean) : Builder{
            shouldOpenImageCropper = openImageCropper
            return this
        }

        fun setShouldEnableFaceDetector(faceDetector : Boolean) : Builder{
            shouldDetectForFace = faceDetector
            return this
        }

        fun setOutputFileUri(outputFileUri : Uri) : Builder{
            this.outputFileUri = outputFileUri
            return this
        }

        fun shouldEnableFreeCrop(enableFreeCrop : Boolean) : Builder {
            this.freeCropEnabled = enableFreeCrop
            return this
        }
    }
}