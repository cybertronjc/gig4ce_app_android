package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.dp2Px

class AddLangugeRvItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {
    var size39 = 0;
    var size29 = 0;
    var size27 = 0;

    init {
        size39 = 39.dp2Px
        size29 = 29.dp2Px
        size27 = 27.dp2Px


    }


    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect[size29, size39, size27] = 0

    }

}