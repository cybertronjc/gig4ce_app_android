package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder
import com.google.android.material.button.MaterialButton

class StandardTextActionButton(context: Context, attrs: AttributeSet?) : FrameLayout(context,attrs),
    IViewHolder {
    private val cta : MaterialButton
    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        LayoutInflater.from(context).inflate(R.layout.cell_standard_text_action_button, this, true)

        cta = this.findViewById(R.id.action_button)
        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.StandardTextActionButton, 0, 0)
            val text = styledAttributes.getBoolean(
                R.styleable.StandardTextActionButton_android_text,
                false
            )
            val buttonCaps = styledAttributes.getBoolean(
                R.styleable.StandardTextActionButton_android_textAllCaps,
                false
            )
            isAllCaps = buttonCaps

        }
    }

    var text:String
        get() = this.toString()
        set(value) {
            cta.text = value
        }
    var isAllCaps: Boolean
        get() = this.isAllCaps
        set(value) {
            cta.isAllCaps = value
        }

    override fun bind(data: Any?) {

    }
}