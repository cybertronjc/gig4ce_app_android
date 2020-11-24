package com.gigforce.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RatioLayoutManager constructor(
        context: Context?,
        @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
        reverseLayout: Boolean = false,
        private val ratio: Float
) : LinearLayoutManager(context, orientation, reverseLayout) {


    private val horizontalSpace get() = width - paddingStart - paddingEnd

    private val verticalSpace get() = width - paddingTop - paddingBottom

    override fun generateDefaultLayoutParams() =
            scaledLayoutParams(super.generateDefaultLayoutParams())

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?) =
            scaledLayoutParams(super.generateLayoutParams(lp))

    override fun generateLayoutParams(c: Context?, attrs: AttributeSet?) =
            scaledLayoutParams(super.generateLayoutParams(c, attrs))

    private fun scaledLayoutParams(layoutParams: RecyclerView.LayoutParams) =
            layoutParams.apply {
                when (orientation) {
                    HORIZONTAL -> width = (horizontalSpace * ratio).toInt()
                    VERTICAL -> height = (verticalSpace * ratio).toInt()
                }
            }
}