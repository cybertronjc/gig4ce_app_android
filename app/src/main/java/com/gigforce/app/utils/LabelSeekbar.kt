package com.gigforce.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatSeekBar


class LabelSeekbar : AppCompatSeekBar {
    private var paint: Paint? = null
    private var bounds: Rect? = null
    var dimension: String? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = Color.BLACK
        paint!!.style = Paint.Style.STROKE
        paint!!.textSize = sp2px(14).toFloat()
        bounds = Rect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val label = progress.toString()
        paint!!.getTextBounds(label, 0, label.length, bounds)
        val x = (progress.toFloat() * (width - 2 * thumbOffset) / max +
                (1 - progress.toFloat() / max) * bounds!!.width() / 2 - bounds!!.width() / 2
                + thumbOffset / (label.length - 1))
        canvas.drawText(label, x, paint!!.textSize, paint!!)
    }

    private fun sp2px(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp.toFloat(),
            resources.displayMetrics
        )
            .toInt()
    }

    companion object {
        private val TAG = LabelSeekbar::class.java.simpleName
    }
}