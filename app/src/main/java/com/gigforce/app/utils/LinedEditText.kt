package com.gigforce.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.gigforce.app.R

class LinedEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    private val mRect: Rect
    private val mPaint: Paint
    private var linepadding: Int
    private var notelineHeight: Int
    override fun onDraw(canvas: Canvas) {
        //int count = getLineCount();
        val height = height
        val line_height = lineHeight
        var count = height / line_height
        if (lineCount > count) count = lineCount //for long text with scrolling
        val r = mRect
        val paint = mPaint
        var baseline = getLineBounds(0, r) //first line
        for (i in 0 until count) {
            canvas.drawLine(
                r.left.toFloat(),
                (baseline + linepadding).toFloat(),
                r.right.toFloat(),
                baseline + linepadding.toFloat(),
                paint
            )
            baseline += lineHeight //next line
        }
        super.onDraw(canvas)
    }

    // we need this constructor for LayoutInflater
    init {
        mRect = Rect()
        mPaint = Paint()

        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = context.resources.getColor(R.color.edit_note_line) //SET YOUR OWN COLOR HERE
        linepadding = context.resources.getDimensionPixelSize(R.dimen.size_18)
        notelineHeight = context.resources.getDimensionPixelSize(R.dimen.size_2)
        mPaint.strokeWidth = notelineHeight.toFloat()
    }

    fun setNoteLineheight(heightInPx: Int) {
        this.notelineHeight = heightInPx
    }

    fun setLinePaddingFromBottom(heightInPx: Int) {
        this.notelineHeight = heightInPx;
    }
}