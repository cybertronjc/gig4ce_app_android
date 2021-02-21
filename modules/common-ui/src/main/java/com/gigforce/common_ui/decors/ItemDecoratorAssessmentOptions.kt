package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.dp2Px

class ItemDecoratorAssessmentOptions(context: Context?) : RecyclerView.ItemDecoration() {
    var size16 = 0;
    var size17 = 0;
    var size8 = 0

    init {
        size16 = 16.dp2Px
        size17 = 17.dp2Px
        size8 = 8.dp2Px

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