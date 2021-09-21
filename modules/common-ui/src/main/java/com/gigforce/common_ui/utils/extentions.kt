package com.gigforce.common_ui.utils

import android.content.res.Resources
import android.util.DisplayMetrics

val Int.dp2Px: Int
    get() {
        return Math.round(this * (Resources.getSystem().displayMetrics.density / DisplayMetrics.DENSITY_DEFAULT))
    }