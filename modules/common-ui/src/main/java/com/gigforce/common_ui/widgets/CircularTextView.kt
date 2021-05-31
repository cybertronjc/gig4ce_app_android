package com.gigforce.common_ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

class CircularTextView : androidx.appcompat.widget.AppCompatTextView {
    private var strokeWidth = 0f
    var strokeColor = 0
    var solidColor_ = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun draw(canvas: Canvas) {
        val circlePaint = Paint()
        circlePaint.color = solidColor_
        circlePaint.flags = Paint.ANTI_ALIAS_FLAG
        val strokePaint = Paint()
        strokePaint.color = strokeColor
        strokePaint.flags = Paint.ANTI_ALIAS_FLAG
        val h = this.height
        val w = this.width
        val diameter = if (h > w) h else w
        val radius = diameter / 2
        this.height = diameter
        this.width = diameter
        canvas.drawCircle(
            diameter / 2.toFloat(),
            diameter / 2.toFloat(),
            radius.toFloat(),
            strokePaint
        )
        canvas.drawCircle(
            diameter / 2.toFloat(),
            diameter / 2.toFloat(),
            radius - strokeWidth,
            circlePaint
        )
        super.draw(canvas)
    }

    fun setStrokeWidth(dp: Int) {
        val scale = context.resources.displayMetrics.density
        strokeWidth = dp * scale
    }

    fun setStrokeColor(color: String?) {
        strokeColor = Color.parseColor(color)
    }

    fun setSolidColor(color: String?) {
        solidColor_ = Color.parseColor(color)
    }
}