package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

class ItemDecorationAddContact(private val context: Context) : RecyclerView.ItemDecoration() {
    var size23 = 0;
    var size28 = 0;
    var size27 = 0;

    init {
        size23 = context.resources.getDimensionPixelSize(R.dimen.size_23);
        size28 = context.resources.getDimensionPixelSize(R.dimen.size_28);
        size27 = context.resources.getDimensionPixelSize(R.dimen.size_27);


    }


    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect[size28, size23, size27] = 0

    }
}