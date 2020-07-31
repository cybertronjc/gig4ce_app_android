package com.gigforce.app.utils.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout

class CircularView : FrameLayout {
    private var strokeWidth = 0f
    var strokeColor = 0
    var bgColor = Color.parseColor("#d9000000")

    constructor(context: Context) : super(context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun dispatchDraw(canvas: Canvas) {
        canvas.setMatrix(matrix)
        val circlePaint = Paint()
        circlePaint.color = bgColor
        circlePaint.flags = Paint.ANTI_ALIAS_FLAG
        val strokePaint = Paint()
        strokePaint.color = strokeColor
        strokePaint.flags = Paint.ANTI_ALIAS_FLAG
        val h = this.height
        val w = this.width
        val diameter = if (h > w) h else w
        val radius = diameter / 2
        val lp = layoutParams
        lp.width = diameter
        lp.height = diameter
        layoutParams = lp
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

        super.dispatchDraw(canvas)
    }


    fun setStrokeWidth(dp: Int) {
        val scale = context.resources.displayMetrics.density
        strokeWidth = dp * scale
    }

    fun setStrokeColor(color: String?) {
        strokeColor = Color.parseColor(color)
    }

    fun setSolidColor(color: String?) {
        bgColor = Color.parseColor(color)

    }
}