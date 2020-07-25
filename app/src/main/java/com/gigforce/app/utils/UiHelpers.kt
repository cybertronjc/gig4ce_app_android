package com.gigforce.app.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import com.gigforce.app.R


fun getScreenWidth(ctx: Activity): Size {
    val displayMetrics = DisplayMetrics()
    ctx.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)

}

fun openPopupMenu(
    view: View,
    menuId: Int,
    click: PopupMenu.OnMenuItemClickListener,
    activity: Activity?
) {
    val popUp = PopupMenu(activity?.applicationContext, view)
    popUp.setOnMenuItemClickListener(click)
    popUp.inflate(R.menu.menu_chat)
    popUp.show()
}


fun getViewHeight(view: View): Int {
    val wm =
        view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val deviceWidth: Int
    val size = Point()
    display.getSize(size)
    deviceWidth = size.x
    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
        deviceWidth,
        View.MeasureSpec.AT_MOST
    )
    val heightMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(widthMeasureSpec, heightMeasureSpec)
    return view.measuredHeight //        view.getMeasuredWidth();
}

fun getViewWidth(view: View): Int {
    val wm =
        view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val deviceWidth: Int
    val size = Point()
    display.getSize(size)
    deviceWidth = size.x
    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
        deviceWidth,
        View.MeasureSpec.AT_MOST
    )
    val heightMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(widthMeasureSpec, heightMeasureSpec)
    return view.measuredWidth //        view.getMeasuredWidth();
}