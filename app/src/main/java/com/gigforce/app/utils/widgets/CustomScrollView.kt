package com.gigforce.app.utils.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class CustomScrollView : ScrollView {
    // Getters & Setters
    var onScrollFireListener: onScrollListener? = null

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(context: Context?) : super(context) {}

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