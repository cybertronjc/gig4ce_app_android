package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.PagerSnapHelper
import com.gigforce.common_ui.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.recyclerView.CoreRecyclerView
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator

open class HorizontalScrollingPageComponent<T>(
    context: Context,
    attrs: AttributeSet?,
    private val shouldShowScrollIndicator: Boolean,
    private val shouldEnablePageSnap: Boolean
) : LinearLayout(
    context,
    attrs
) {


    private lateinit var containerLayout: View
    private lateinit var recyclerView: CoreRecyclerView
    private lateinit var scrollingPageIndicator: ScrollingPagerIndicator

    init {
        this.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        LayoutInflater.from(context).inflate(R.layout.component_horizontal_scroll_page, this, true)
        initViews()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        containerLayout = findViewById(R.id.card_view)
        scrollingPageIndicator = findViewById(R.id.indicator)

        if (shouldShowScrollIndicator) {
            scrollingPageIndicator.attachToRecyclerView(recyclerView)
        } else {
            hideScrollIndicator()
        }

        if (shouldEnablePageSnap) {
            val pagerSnapHelper = PagerSnapHelper()
            pagerSnapHelper.attachToRecyclerView(recyclerView)
        }
    }

    private fun hideScrollIndicator() {
        scrollingPageIndicator.gone()
    }

    private fun showScrollIndicator() {
        scrollingPageIndicator.visible()
    }

    fun setData(data: List<T>) {
        if (data.isEmpty()) {
            //hide the whole layout
            recyclerView.collection = emptyList()
            containerLayout.gone()
        } else {
            //show whole layout
            containerLayout.visible()

            if(shouldShowScrollIndicator) {
                showScrollIndicator()
                if (data.size == 1) {
                    hideScrollIndicator()
                } else {
                    showScrollIndicator()
                }
            }

            recyclerView.collection = data as List<Any>
        }
    }
}