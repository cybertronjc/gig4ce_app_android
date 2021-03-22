package com.gigforce.common_ui.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.gigforce.common_ui.R


class GrayLine(context: Context,attributeSet: AttributeSet?) : View(context,attributeSet){
    init {
        minimumHeight = getPixelValue(2)
        setBackgroundColor(ContextCompat.getColor(context, R.color.grey))
    }

    fun getPixelValue(sizeInDp:Int): Int {
        val scale = resources.displayMetrics.density
        return ((sizeInDp * scale + 0.5f).toInt())
    }
}