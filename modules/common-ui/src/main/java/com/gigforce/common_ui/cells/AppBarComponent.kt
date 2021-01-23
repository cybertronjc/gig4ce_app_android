package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder

class AppBarComponent (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_bar, this, true)
    }

    override fun bind(data: Any?) {

    }
}