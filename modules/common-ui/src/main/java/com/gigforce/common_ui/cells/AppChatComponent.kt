package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder

class AppChatComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    val chatImage: ImageView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_chat, this, true)
        chatImage = this.findViewById(R.id.chat_icon_iv)
    }

//    var chatImageView: ImageView
//        get() = chatImage
//        set(value) {
//        }

    override fun bind(data: Any?) {
    }


}