package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.Message

abstract class TextMessage(val type:String, context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs), IViewHolder {

    private lateinit var msgView:TextView
    private lateinit var timeView:TextView

    init {
        setDefault()
        inflate()
    }

    fun setDefault(){
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate(){
        if(type == "in")
            LayoutInflater.from(context).inflate(R.layout.msg_chat_in_text, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.msg_chat_out_text, this, true)
        loadViews()
    }

    fun loadViews(){
       msgView = this.findViewById(R.id.tv_msgValue)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
    }

    override fun bind(data: Any?) {
        data?.let {
            val msg = it as Message

         //   msgView.setText(msg.content)
            timeView.setText(msg.timestamp?.toDisplayText())
        }
    }
}

class InTextMessage(context: Context, attrs: AttributeSet?): TextMessage("in", context, attrs){}
class OutTextMessage(context: Context, attrs: AttributeSet?): TextMessage("out", context, attrs){}