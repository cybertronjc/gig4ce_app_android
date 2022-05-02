package com.gigforce.app.android_common_utils.extensions

import android.widget.TextSwitcher
import android.widget.TextView

fun TextSwitcher.getTextView(): TextView {
    return this.currentView as TextView
}

fun TextSwitcher.getText(): String {
    return this.getTextView().text.toString()
}

fun TextSwitcher.setTextSkipAnimationIfTextIsTheSame(
    text: String
) {
    val previousText = getText()
    if (previousText == text) {
        this.setCurrentText(text)
    } else {
        this.setText(text)
    }
}