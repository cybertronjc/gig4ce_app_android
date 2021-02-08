package com.gigforce.common_image_picker

import android.net.Uri

class ImageCropOptions private constructor(builder: Builder) {

    var shouldOpenImageCropper: Boolean = builder.shouldOpenImageCropper
    var shouldDetectForFace: Boolean = builder.shouldDetectForFace
    var outputFileUri : Uri? = builder.outputFileUri

    class Builder() {

        var shouldOpenImageCropper: Boolean = false
        var shouldDetectForFace: Boolean = false
        var outputFileUri : Uri? = null

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
    }
}