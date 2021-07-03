package com.gigforce.common_ui

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics

object DisplayUtil {


    fun getScreenWidthInPx(context: Context): Int {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }



    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}