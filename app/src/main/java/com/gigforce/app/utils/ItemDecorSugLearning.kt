package com.gigforce.app.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

class ItemDecorSugLearning(context: Context?) :
    RecyclerView.ItemDecoration() {
    var size19 = 0;
    var size8 = 0;
    var size16 = 0;
    var size22 = 0


    init {
        size8 = context?.resources?.getDimensionPixelSize(R.dimen.size_8) ?: 0
        size19 = context?.resources?.getDimensionPixelSize(R.dimen.size_19) ?: 0
        size16 = context?.resources?.getDimensionPixelSize(R.dimen.size_16) ?: 0
        size22 = context?.resources?.getDimensionPixelSize(R.dimen.size_22) ?: 0


    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {


        if (parent.getChildAdapterPosition(view) == 0) {
            outRect[size19, size16, size8] = size22
        } else {
            outRect[size8, size16, size8] = size22
        }


    }

}