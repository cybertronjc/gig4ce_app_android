package com.gigforce.app.modules.onboarding.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gigforce.app.R

class ViewChanger (context: Context,
                   attrs: AttributeSet? = null): FrameLayout(context, attrs){

    /*
        Inflate the view needed and pass it to the changeView
    */

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_ob_toggle, this, true)
    }

    fun changeView(view: View) {
        this.removeAllViews()
        this.addView(view)
        this.invalidate()
    }
}