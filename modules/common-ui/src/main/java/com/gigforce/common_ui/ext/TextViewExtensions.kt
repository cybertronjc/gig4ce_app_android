package com.gigforce.common_ui.ext

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView

fun TextView.setTextWithMandatorySymbol(text: String) {
    if (text.isNotBlank()) {

        val spannableText =
            if (text.endsWith("*"))
                SpannableString(text)
            else
                SpannableString("$text *")

        spannableText.setSpan(
            ForegroundColorSpan(Color.RED),
            spannableText.length - 1,
            spannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        this.text = spannableText
    }
}

fun TextView.addMandatorySymbolToTextEnd() {
        val spannableText =
            if (text.endsWith("*"))
                SpannableString(text)
            else
                SpannableString("$text *")

        spannableText.setSpan(
            ForegroundColorSpan(Color.RED),
            spannableText.length - 1,
            spannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        this.text = spannableText
}

fun EditText.setHintWithMandatorySymbol(text: String) {
    if (text.isNotBlank()) {

        val spannableText =
            if (text.endsWith("*"))
                SpannableString(text)
            else
                SpannableString("$text *")

        spannableText.setSpan(
            ForegroundColorSpan(Color.RED),
            spannableText.length - 1,
            spannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        this.hint = spannableText
    }
}

fun TextView.setTextWithDefaultvalue(text: String?, defaultValue: String) {

    if (text != null) {
        this.text = text
    } else {
        this.text = defaultValue
    }
}