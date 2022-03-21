package com.gigforce.common_ui.ext

import android.view.View
import com.gigforce.common_ui.utils.PushDownAnim

fun View.pushOnclickListener(listener: View.OnClickListener) {
    PushDownAnim.setPushDownAnimTo(this).setOnClickListener(listener)
}