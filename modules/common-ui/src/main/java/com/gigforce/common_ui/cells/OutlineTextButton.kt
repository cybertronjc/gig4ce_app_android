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

class OutlineTextButton (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    private var text_button: TextView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_outline_text_button, this, true)
        text_button = this.findViewById(R.id.text_button)
    }
    var text:String
        get() = ""
        set(value) {
            text_button.text = value
        }
    override fun bind(data: Any?) {
        if(data is OutlineTextDVM){
            text_button.text = data.title
        }
    }
}