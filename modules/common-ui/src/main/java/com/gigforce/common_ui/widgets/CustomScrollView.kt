package com.gigforce.common_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class CustomScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
    // Getters & Setters
    var onScrollFireListener: onScrollListener? = null



    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        val view = getChildAt(childCount - 1)
        val diff = view.bottom - (height + scrollY) - view.paddingBottom
        onScrollFireListener!!.onBottomReached(diff <= 0 && onScrollFireListener != null)
        onScrollFireListener?.onScrollChanged();
        super.onScrollChanged(l, t, oldl, oldt)
    }

    fun setScrollerListener(item: onScrollListener) {
        this.onScrollFireListener = item;
    }

    //Event listener.
    interface onScrollListener {
        fun onBottomReached(reached: Boolean)
        fun onScrollChanged()

    }
}