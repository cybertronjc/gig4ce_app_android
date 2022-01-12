package com.gigforce.common_ui.components.atoms

import android.view.View.MeasureSpec

import android.content.Context

import android.content.res.TypedArray
import android.util.AttributeSet

import android.widget.LinearLayout
import androidx.appcompat.widget.ViewUtils
import com.gigforce.common_ui.R
import com.google.android.material.internal.ViewUtils.dpToPx
import android.util.DisplayMetrics

class MaxHeightLinearLayout : LinearLayout {
    private var maxHeightDp = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a: TypedArray = context.getTheme()
            .obtainStyledAttributes(attrs, R.styleable.MaxHeightLinearLayout, 0, 0)
        maxHeightDp = try {
            a.getInteger(R.styleable.MaxHeightLinearLayout_maxHeightDp, 0)
        } finally {
            a.recycle()
        }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        val maxHeightPx: Int = dpToPx(maxHeightDp)
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setMaxHeightDp(maxHeightDp: Int) {
        this.maxHeightDp = maxHeightDp
        invalidate()
    }

    fun pxToDp(px: Int): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}
