package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.dp2Px

class ItemDecorSugLearning(context: Context?) :
    RecyclerView.ItemDecoration() {
    var size19 = 0;
    var size8 = 0;
    var size16 = 0;
    var size22 = 0


    init {
        size8 = 8.dp2Px
        size19 = 19.dp2Px
        size16 = 16.dp2Px
        size22 = 22.dp2Px
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