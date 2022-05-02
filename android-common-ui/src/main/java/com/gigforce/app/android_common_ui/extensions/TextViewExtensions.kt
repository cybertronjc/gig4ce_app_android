package com.gigforce.app.android_common_ui.extensions

import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

// Button extends TextView
fun TextView.leftDrawable(
    @DrawableRes id: Int = 0,
    @DimenRes sizeRes: Int
) {
    val drawable = ContextCompat.getDrawable(context, id)
    val size = resources.getDimensionPixelSize(sizeRes)
    drawable?.setBounds(0, 0, size, size)
    this.setCompoundDrawables(drawable, null, null, null)
}