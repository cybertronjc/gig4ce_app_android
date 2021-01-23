package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.gigforce.common_ui.R

class StandardActionLipstickCardComponent(context: Context, attrs: AttributeSet?) :StandardActionCardComponent(context,attrs) {
    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.lipstick))
        titleColor = ContextCompat.getColor(context, R.color.white)
        subtitleColor = ContextCompat.getColor(context, R.color.white)
    }

}