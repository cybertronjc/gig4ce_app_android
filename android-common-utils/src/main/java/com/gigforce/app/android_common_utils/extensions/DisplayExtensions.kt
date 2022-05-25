package com.gigforce.app.android_common_utils.extensions

import android.content.res.Resources
import android.util.DisplayMetrics

val Int.dpToPx: Int
    get() {
        return Math.round(this * (Resources.getSystem().displayMetrics.density / DisplayMetrics.DENSITY_DEFAULT))
    }