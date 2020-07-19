package com.gigforce.app.utils

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
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