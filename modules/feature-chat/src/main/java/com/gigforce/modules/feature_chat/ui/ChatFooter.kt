package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import com.gigforce.modules.feature_chat.R

class ChatFooter(context: Context,  attrs: AttributeSet) :
    LinearLayout(context,attrs) {

     var et_message:EditText
     var btn_send:AppCompatImageButton
    var attachmentOptionButton : ImageView

    var replyBlockedLayout : TextView
    var replyLayout : View

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.fragment_chat_footer, this, true)

        setBackgroundColor(Color.parseColor("#f6f7f8"))
        et_message = this.findViewById(R.id.et_typedMessageValue)
        btn_send = this.findViewById(R.id.btn_send_chat)
        attachmentOptionButton = this.findViewById(R.id.iv_greyPlus)

        replyBlockedLayout = this.findViewById(R.id.group_does_support_replies_text)
        replyLayout = this.findViewById(R.id.chat_footer_type_layout)
    }
}