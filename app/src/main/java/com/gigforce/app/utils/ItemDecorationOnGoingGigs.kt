package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationOnGoingGigs(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {


    constructor(
        context: Context?,
        @DimenRes itemOffsetId: Int
    ) : this(context?.resources?.getDimensionPixelSize(itemOffsetId)!!)

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect[if(parent.getChildAdapterPosition(view)==0) mItemOffset else mItemOffset/2, 0, 0] = mItemOffset

    }


}