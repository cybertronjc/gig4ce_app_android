package com.gigforce.app.modules.profile.utils

import android.graphics.Canvas
import android.widget.ImageView

class VerticalParallaxTransformer : ViewTransformer() {

    override fun onAttached(view: ScrollTransformImageView) {
        view.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun apply(view: ScrollTransformImageView, canvas: Canvas, viewX : Int, viewY : Int) {
        if (view.scaleType == ImageView.ScaleType.CENTER_CROP) {
            val imageWidth = view.drawable.intrinsicWidth
            val imageHeight = view.drawable.intrinsicHeight

            val viewWidth = view.width - view.paddingLeft - view.paddingRight
            val viewHeight = view.height - view.paddingTop - view.paddingBottom

            val deviceHeight = view.resources.displayMetrics.heightPixels

            // If this view is off screen we wont do anything
            if (viewY < -viewHeight || viewY > deviceHeight) return

            if (imageWidth * viewHeight < viewWidth * imageHeight) {
                val scale = viewWidth.toFloat() / imageWidth.toFloat()
                val invisibleVerticalArea = imageHeight * scale - viewHeight

                val y = centeredY(viewY, viewHeight, deviceHeight)
                val translationScale = invisibleVerticalArea / (deviceHeight + viewHeight)
                canvas.translate(0f, y * translationScale)
            }
        }
    }
}