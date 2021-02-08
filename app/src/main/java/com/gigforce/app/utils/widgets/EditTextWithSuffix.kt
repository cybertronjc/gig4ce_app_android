package com.gigforce.app.utils.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.gigforce.app.R

open class EditTextWithSuffix : AppCompatEditText {
    var textPaint = TextPaint()
    open var suffix: String? = ""
    private var suffixPadding = 0f

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getAttributes(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttributes(context, attrs, defStyleAttr)
    }

    public override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val suffixXPosition = textPaint.measureText(text.toString()).toInt() + paddingLeft
        c.drawText(suffix!!, Math.max(suffixXPosition.toFloat(), suffixPadding), baseline.toFloat(), textPaint)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        textPaint.color = currentTextColor
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun getAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EditTextWithSuffix, defStyleAttr, 0)
        if (a != null) {
            suffix = a.getString(R.styleable.EditTextWithSuffix_suffix)
            if (suffix == null) {
                suffix = ""
            }
            suffixPadding = a.getDimension(R.styleable.EditTextWithSuffix_suffixPadding, 0f)
        }
        a.recycle()
    }
}