package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import com.gigforce.modules.feature_chat.R

class ChatFooter(context: Context,  attrs: AttributeSet) :
    LinearLayout(context,attrs) {

    lateinit var et_message:EditText
    lateinit var btn_send:AppCompatImageButton
    lateinit var attachmentOptionButton : ImageView

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.footer_chat_page, this, true)

        et_message = this.findViewById(R.id.et_typedMessageValue)
        btn_send = this.findViewById(R.id.btn_send_chat)
        attachmentOptionButton = this.findViewById(R.id.iv_greyPlus)
    }
}