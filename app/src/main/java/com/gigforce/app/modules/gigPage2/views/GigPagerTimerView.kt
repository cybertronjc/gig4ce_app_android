package com.gigforce.app.modules.gigPage2.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R

class GigPagerTimerView(
        context: Context,
        attrs: AttributeSet
) : ConstraintLayout(
        context,
        attrs
) {

    init {
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(
                R.layout.fragment_gig_page_2_timer_layout,
                this,
                false
        )
    }


}