package com.gigforce.app.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import com.gigforce.app.R


class ThumbTextView : AppCompatTextView {
    private val lp = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )


    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    fun attachToSeekBar(seekBar: SeekBar) {
        val contentWidth = this.paint.measureText(text.toString())

        val width = (seekBar.width
                - seekBar.paddingLeft
                - seekBar.paddingRight)
        val thumbPos = (seekBar.paddingLeft
                + width
                * seekBar.progress
                / seekBar.max)
        val layoutParams = layoutParams as LinearLayout.LayoutParams
        val left = resources.getDimensionPixelSize(R.dimen.size_4)
        val top = resources.getDimensionPixelSize(R.dimen.size_9)
        layoutParams.setMargins(
            (thumbPos - (left + ((contentWidth - left) / 2))).toInt(),
            top,
            0,
            0
        )
        setLayoutParams(layoutParams)


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (width == 0)
            width = MeasureSpec.getSize(widthMeasureSpec)
    }
}