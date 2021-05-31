package com.gigforce.common_ui.decors

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.dp2Px

class ItemDecorationAddContact(private val context: Context) : RecyclerView.ItemDecoration() {
    var size23 = 0;
    var size28 = 0;
    var size27 = 0;

    init {
        size23 = 23.dp2Px
        size28 = 28.dp2Px
        size27 = 27.dp2Px
    }


    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect[size28, size23, size27] = 0

    }
}