package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

class ItemDecoratorAssessmentOptions(context: Context?) : RecyclerView.ItemDecoration() {
    var size16 = 0;
    var size17 = 0;
    var size8 = 0

    init {
        size16 = context?.resources?.getDimensionPixelSize(R.dimen.size_16) ?: 0
        size17 = context?.resources?.getDimensionPixelSize(R.dimen.size_17) ?: 0
        size8 = context?.resources?.getDimensionPixelSize(R.dimen.size_8) ?: 0

    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect[size17, size16, size17] = size8
        } else {
            outRect[size17, size8, size17] = size8
        }
    }

}