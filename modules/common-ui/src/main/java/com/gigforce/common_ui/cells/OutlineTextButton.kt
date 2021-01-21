package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.OutlineTextDVM
import com.gigforce.core.IViewHolder
import com.google.android.material.button.MaterialButton

class OutlineTextButton @JvmOverloads constructor(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    private var text_button: TextView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_outline_text_button, this, true)
        text_button = this.findViewById(R.id.text_button)
        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.OutlineTextButton, 0, 0)
            val buttonText =
                styledAttributes.getString(R.styleable.OutlineTextButton_android_text) ?: ""
            val buttonCaps = styledAttributes.getBoolean(
                R.styleable.OutlineTextButton_android_textAllCaps,
                false
            )
            text = buttonText
            isAllCaps = buttonCaps
        }
    }

    var isAllCaps: Boolean
        get() = this.isAllCaps
        set(value) {
            text_button.isAllCaps = value
        }

    var text: String
        get() = this.toString()
        set(value) {
            text_button.text = value
        }

//    fun setText(text: String) {
//        text_button.text = text
//    }

    override fun bind(data: Any?) {
        if (data is OutlineTextDVM) {
            text_button.text = data.title
        }
    }
}