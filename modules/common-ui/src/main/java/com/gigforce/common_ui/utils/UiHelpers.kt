package com.gigforce.common_ui.utils

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.text.Html
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

fun getScreenWidth(ctx: FragmentActivity): Size {
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
    popUp.inflate(menuId)
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

fun getScreenShot(view: View): Bitmap {
    val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitmap)
    val bgDrawable = view.background
    if (bgDrawable != null) bgDrawable.draw(canvas)
    else canvas.drawColor(Color.WHITE)
    view.draw(canvas)
    return returnedBitmap
}

fun getCircularProgressDrawable(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 20f
    circularProgressDrawable.start()
    return circularProgressDrawable
}

fun getBitmapFromView(view: View, height: Int, width: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    val canvas = Canvas(bitmap);
    val bgDrawable = view.background;
    if (bgDrawable != null)
        bgDrawable.draw(canvas);
    else
        canvas.drawColor(Color.WHITE);
    view.draw(canvas);
    return bitmap;
}

fun TextView.setDrawableColor(color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}


fun addAsteriskHint(normalTextColor: String, color: String, vararg ets: EditText) {
    for (et in ets) {
        val hint = et.hint.toString()
        et.hint =
                Html.fromHtml("<font color=$normalTextColor>$hint </font><font color=$color> *</font>")
    }
}

fun showViews(visibility: Boolean, vararg views: View) {
    views.forEach {
        it.isVisible = visibility
    }
}

