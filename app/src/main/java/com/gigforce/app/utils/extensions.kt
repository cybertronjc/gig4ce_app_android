package com.gigforce.app.utils

import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

val Int.dp: Int
    get() {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

fun ViewPager2.reduceDragSensitivity(factor:Int = 4) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop*factor)       // "8" was obtained experimentally
}