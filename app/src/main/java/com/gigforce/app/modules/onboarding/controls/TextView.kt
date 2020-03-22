package com.gigforce.app.modules.onboarding.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gigforce.app.R
import kotlinx.android.synthetic.main.item_ob_toggle_button.view.*

class OBTextView(context: Context,
                     attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_textview_ob, this, true)
    }
}

class OBTextViewItem(context: Context,
                         attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_textview_ob, this, true)

        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.OBTextViewItem, 0, 0)

            val text = styledAttributes.getText(R.styleable.OBTextViewItem_textview)

            this.txt.setText(text)
        }
    }
}