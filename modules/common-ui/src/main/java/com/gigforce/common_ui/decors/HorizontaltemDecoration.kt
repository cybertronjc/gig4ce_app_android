package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class HorizontaltemDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(
        context: Context?,
        @DimenRes itemOffsetId: Int
    ) : this(context?.resources?.getDimensionPixelSize(itemOffsetId)!!)

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect[mItemOffset / 2, mItemOffset / 2, mItemOffset / 2] = mItemOffset / 2

    }

}