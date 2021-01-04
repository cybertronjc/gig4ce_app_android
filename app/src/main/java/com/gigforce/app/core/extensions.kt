package com.gigforce.app.core

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.format.DateUtils
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Timestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
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

fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Timestamp.toLocalDateTime(): LocalDateTime {

    return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Timestamp.toLocalDate(): LocalDate {
    return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Timestamp.toDisplayText(): String {
    val date = this.toDate()
    return if (DateUtils.isToday(date.time)) SimpleDateFormat("hh:mm a").format(date) else SimpleDateFormat(
        "dd MMM, hh:mm a"
    ).format(date)
}

fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val k = it.key
        val v = it.value
        when (v) {
            is IBinder -> putBinder(k, v)
            is Bundle -> putBundle(k, v)
            is Byte -> putByte(k, v)
            is ByteArray -> putByteArray(k, v)
            is Char -> putChar(k, v)
            is CharArray -> putCharArray(k, v)
            is CharSequence -> putCharSequence(k, v)
            is Float -> putFloat(k, v)
            is FloatArray -> putFloatArray(k, v)
            is Parcelable -> putParcelable(k, v)
            is Serializable -> putSerializable(k, v)
            is Short -> putShort(k, v)
            is ShortArray -> putShortArray(k, v)

//      is Size -> putSize(k, v) //api 21
//      is SizeF -> putSizeF(k, v) //api 21

            else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
//      is Array<*> -> TODO()
//      is List<*> -> TODO()
        }
    }
}

fun ChipGroup.selectChipWithText(vararg text: String) {
    if (this.childCount == 0)
        return

    text.forEach {
        if (it.isNotBlank()) {

            for (i in 0 until this.childCount) {
                val chip = this.getChildAt(i) as Chip

                if (chip.text.toString().trim().equals(
                        other = it,
                        ignoreCase = true
                    )
                ) {
                    chip.isChecked = true
                }
            }
        }
    }
}

fun ChipGroup.selectChipsWithText(text: List<String>) {
    selectChipWithText(*text.toTypedArray())
}