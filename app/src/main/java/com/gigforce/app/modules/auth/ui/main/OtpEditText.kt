package com.gigforce.app.modules.auth.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ActionMode
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatEditText
import com.gigforce.app.R


class OtpEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    private var mSpace: Float = 24f
    private var mLineSpacing: Float = 8f
    private var mNumChars = 6
    private var mMaxLength = 6
    private var mLineStroke: Float = 2f

    private lateinit var mLinesPaint: Paint
    private var mClickListener: OnClickListener? = null

    init {
        val multi = context.resources.displayMetrics.density
        mLineStroke = multi * mLineStroke
        mLinesPaint = Paint(paint).apply {
            this.strokeWidth = mLineStroke
            this.color = resources.getColor(R.color.colorPrimary)
        }

        setBackgroundResource(0)
        mSpace *= multi
        mLineSpacing *= multi
        mNumChars = mMaxLength

        super.setOnClickListener(OnClickListener {
            val view = it
            setSelection(text!!.length)
            mClickListener?.let {
                it.onClick(view)
            }
        })
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        mClickListener = l
    }

    override fun setCustomInsertionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.");
    }

    override fun onDraw(canvas: Canvas?) {
        val availableWidth = width - paddingRight - paddingLeft
        val mCharSize: Float
        if (mSpace < 0) {
            mCharSize = availableWidth / (mNumChars * 2f - 1)
        } else {
            mCharSize = (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom

        //Text Width

        //Text Width
        val text = text
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)

        for (i in 0 until mNumChars) {
            canvas!!.drawLine(
                    startX.toFloat(),
                    bottom.toFloat(), startX + mCharSize,
                    bottom.toFloat(), mLinesPaint
            )
            if (getText()!!.length > i) {
                val middle = startX + mCharSize / 2
                canvas.drawText(
                        text,
                        i,
                        i + 1,
                        middle - textWidths[0] / 2,
                        bottom - mLineSpacing,
                        paint
                )
            }
            if (mSpace < 0) {
                startX += (mCharSize * 2).toInt()
            } else {
                startX += (mCharSize + mSpace.toInt()).toInt()
            }
        }
    }

    fun setNumChars(numChars: Int) {
        this.mNumChars = numChars;
        invalidate()

    }
}