package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class ItemOffsetDecoration(private val mItemOffset: Int) : ItemDecoration() {

    constructor(
        context: Context?,
        @DimenRes itemOffsetId: Int
    ) : this(context?.resources?.getDimensionPixelSize(itemOffsetId)!!)

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect[mItemOffset, mItemOffset * 2, mItemOffset] = mItemOffset / 2
        } else {
            outRect[mItemOffset, mItemOffset / 2, mItemOffset] = mItemOffset / 2
        }
    }

}