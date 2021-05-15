package com.gigforce.core.extensions

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Timestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

val Int.dp: Int
    get() {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

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

    for (i in 0 until this.adapter.count) {
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

fun Date?.toFirebaseTimeStamp(): Timestamp? {

    return if (this == null)
        null
    else
        Timestamp(this)
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Timestamp.toLocalDateTime(): LocalDateTime {

    return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Timestamp.toLocalDate(): LocalDate {
    return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Timestamp.toDisplayText(): String {
    val date = this.toDate()
    return if(DateUtils.isToday(date.time)) SimpleDateFormat("hh:mm a").format(date) else SimpleDateFormat("dd MMM, hh:mm a").format(date)
}

fun Date.toDisplayText(): String {
    return if(DateUtils.isToday(this.time)) SimpleDateFormat("hh:mm a").format(this) else SimpleDateFormat("dd MMM, hh:mm a").format(this)
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

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")


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

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}

fun EditText.onTextChanged(onTextChange: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChange.invoke(s.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

public fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> {
    return BatchingSequence(this, n)
}

private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) : Sequence<List<T>> {
    override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
        val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
        override fun computeNext() {
            if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
            else done()
        }
    }
}