package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class LandingView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}