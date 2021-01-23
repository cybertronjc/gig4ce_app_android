package com.gigforce.common_ui.cells

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder

class AppChatComponent(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatImageButton(context, attrs),
    IViewHolder {

    init {
        // LayoutInflater.from(context).inflate(R.layout.cell_app_chat, this, true)
        // chatImage = this.findViewById(R.id.chat_icon_iv)
        // this.setImageDrawable()
    }

    override fun bind(data: Any?) {
    }


}