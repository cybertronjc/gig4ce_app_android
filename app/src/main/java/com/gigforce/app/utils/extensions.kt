package com.gigforce.app.utils

import android.content.res.Resources
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R


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

fun Fragment.setDarkStatusBarTheme(isDark:Boolean = true) {
    val window: Window = this.activity!!.window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    if(isDark) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(activity!!.resources.getColor(R.color.colorAccent))
        window.decorView.systemUiVisibility = 0
    }
}

fun NavController.popAllBackStates(){
    var hasBackStack = true;
    while (hasBackStack) {
        hasBackStack = this.popBackStack()
    }
}