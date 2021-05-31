package com.gigforce.core.utils

interface SelectImageSourceBottomSheetActionListener {
    fun onImageSourceSelected(source: ImageSource)
}

enum class ImageSource {
    CAMERA,
    GALLERY
}