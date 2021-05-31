package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.gigforce.common_ui.utils.dp2Px

class ItemDecor(context: Context?) :
    ItemDecoration() {
    var size24 = 0;
    var size17 = 0;
    var size5 = 0;


    init {
        size24 = 24.dp2Px
        size17 = 17.dp2Px
        size5 = 5.dp2Px


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