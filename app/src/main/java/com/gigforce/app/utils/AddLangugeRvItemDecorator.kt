package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

class AddLangugeRvItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {
    var size39 = 0;
    var size29 = 0;
    var size27 = 0;

    init {
        size39 = context.resources.getDimensionPixelSize(R.dimen.size_39);
        size29 = context.resources.getDimensionPixelSize(R.dimen.size_29);
        size27 = context.resources.getDimensionPixelSize(R.dimen.size_27);


    }


    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect[size29, size39, size27] = 0

    }

}