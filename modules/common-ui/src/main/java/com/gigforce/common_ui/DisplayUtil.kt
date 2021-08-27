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


    private var density = 1f

    fun toDp(pixels: Float, context: Context): Int {
        if (density == 1f) {
            checkDisplaySize(context)
        }
        return if (pixels == 0f) {
            0
        } else Math.ceil((density * pixels).toDouble()).toInt()
    }


    private fun checkDisplaySize(context: Context) {
        try {
            density = context.resources.displayMetrics.density
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}