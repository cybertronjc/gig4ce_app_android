package com.gigforce.app.modules.onboarding.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gigforce.app.R
import kotlinx.android.synthetic.main.item_ob_toggle_button.view.*

class OBToggleButton(context: Context,
                     attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_ob_toggle, this, true)
    }
}

class OBToggleButtonItem(context: Context,
                   attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_ob_toggle_button, this, true)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.OBToggleButtonItem, 0, 0)

            val text = styledAttributes.getText(R.styleable.OBToggleButtonItem_text)

            this.txt.setText(text)
        }
    }
}