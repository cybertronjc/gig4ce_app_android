package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatMessage


abstract class TextMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) :  RelativeLayout(context, attrs),
        IViewHolder, View.OnLongClickListener {

    private lateinit var senderNameTV: TextView
    private lateinit var msgView:TextView
    private lateinit var timeView:TextView
    private lateinit var receivedStatusIV : ImageView

    init {
        setDefault()
        inflate()
    }

    fun setDefault(){
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate(){
        if(type == MessageFlowType.IN)
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_out, this, true)
        loadViews()
    }

    fun loadViews(){
        senderNameTV = this.findViewById(R.id.user_name_tv)
        msgView = this.findViewById(R.id.tv_msgValue)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)

        msgView.setOnLongClickListener(this)
    }

    override fun bind(data: Any?) {
        data?.let {
            val msg = it as ChatMessage

            senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = msg.senderInfo.name

            msgView.setText(msg.content)
            timeView.setText(msg.timestamp?.toDisplayText())
            setReceivedStatus(msg)
        }
    }

    private fun setReceivedStatus(msg: ChatMessage) = when (msg.status) {
        ChatConstants.MESSAGE_STATUS_NOT_SENT -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_DELIVERED_TO_SERVER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_sent)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_delivered)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_READ_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_seen)
                    .into(receivedStatusIV)
        }
        else -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
        }
    }

    override fun onLongClick(v: View?): Boolean {

        if(v is TextView) {

            val clip: ClipData = ClipData.newPlainText("Copy", v.text)
            (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(clip)
        }
        return true
    }

}

class InTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.IN,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class OutTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.OUT,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class GroupInTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.IN,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)

class GroupOutTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.OUT,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)