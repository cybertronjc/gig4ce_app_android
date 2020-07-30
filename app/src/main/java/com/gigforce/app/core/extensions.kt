package com.gigforce.app.core

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


val Int.dp: Int
    get() {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

fun ViewPager2.reduceDragSensitivity(factor: Int = 4) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * factor)       // "8" was obtained experimentally
}

fun Fragment.setDarkStatusBarTheme(isDark: Boolean = true) {
    val window: Window = this.requireActivity().window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    if (isDark) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity!!.resources.getColor(R.color.colorAccent)
        window.decorView.systemUiVisibility = 0
    }
}

fun NavController.popAllBackStates() {
    var hasBackStack = true;
    while (hasBackStack) {
        hasBackStack = this.popBackStack()
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

val LocalDateTime.toDate: Date
    @RequiresApi(Build.VERSION_CODES.O)
    get() {
        return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
    }

fun Spinner.selectItemWithText(text: String) {

    if (this.count == 0)
        return

    for (i in 0..this.adapter.count) {
        val item = this.adapter.getItem(i)

        item ?: continue
        val stringDisplayed = item.toString().toLowerCase().trim()
        val targetString = text.toLowerCase().trim()

        if (stringDisplayed.equals(targetString)) {
            this.setSelection(i)
            return
        }
    }
}