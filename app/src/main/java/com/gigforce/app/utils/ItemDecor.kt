package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.gigforce.app.R

class ItemDecor(context: Context?) :
    ItemDecoration() {
    var size24 = 0;
    var size17 = 0;
    var size5 = 0;


    init {
        size24 = context?.resources?.getDimensionPixelSize(R.dimen.size_24) ?: 0
        size17 = context?.resources?.getDimensionPixelSize(R.dimen.size_17) ?: 0
        size5 = context?.resources?.getDimensionPixelSize(R.dimen.size_5) ?: 0


    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {


        if (parent.getChildAdapterPosition(view) < 5) {
            outRect.top = size17
        } else {
            outRect.top = size24
        }
        outRect.left =
            size5
        outRect.right =
            size5
        outRect.bottom = 0


    }

}