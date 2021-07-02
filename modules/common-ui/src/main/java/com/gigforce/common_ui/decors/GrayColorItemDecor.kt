package com.gigforce.common_ui.decors

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GrayColorItemDecor(val isVertical: Boolean, val colorValue: Int, val size: Int) :
    RecyclerView.ItemDecoration() {
    constructor() : this(false, Color.parseColor("#ff000000"), 100)

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        color = colorValue
    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (isVertical) {
            outRect.set(0, 0, size, 0)
        } else {
            outRect.set(0, 0, 0, size)
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (isVertical) {
            drawVertical(c, parent, state)
        } else {
            drawHorizontal(c, parent, state)
        }
    }

    /**
     * Draw horizontal dividing line
     */
    private fun drawHorizontal(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val param = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - param.leftMargin
            val top = child.bottom + param.bottomMargin
            val right = child.right + param.rightMargin
            val bottom = top + size
            c.drawRect(Rect(left, top, right, bottom), paint)
        }
    }

    /**
    Draw a vertical parting line *
     **/
    private fun drawVertical(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val param = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + param.rightMargin
            val top = child.top - param.topMargin
            val right = left + size
            val bottom = child.bottom + param.bottomMargin
            c.drawRect(Rect(left, top, right, bottom), paint)
        }
    }
}