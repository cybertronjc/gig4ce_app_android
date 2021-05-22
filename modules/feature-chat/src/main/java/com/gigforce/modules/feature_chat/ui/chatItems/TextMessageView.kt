package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.DisplayUtil
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage


abstract class TextMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    private lateinit var containerView: View
    private lateinit var senderNameTV: TextView
    private lateinit var msgView: TextView
    private lateinit var timeView: TextView
    private lateinit var receivedStatusIV: ImageView

    init {
        setDefault()
        inflate()
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val view = if (type == MessageFlowType.IN)
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_out, this, true)
        loadViews(view)

    }

    fun loadViews(
            view: View
    ) {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        msgView = this.findViewById(R.id.tv_msgValue)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)

        containerView = this.findViewById(R.id.ll_msgContainer)
        containerView.setOnLongClickListener(this)

        val screenWidth = DisplayUtil.getScreenWidthInPx(context)
        val maxWidth = (screenWidth * 0.70).toInt()
        msgView.maxWidth = maxWidth
    }

    override fun bind(data: Any?) {
        data?.let {
            val msg = it as ChatMessage

            senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = msg.senderInfo.name
            msgView.setText(msg.content)
            LinkifyCompat.addLinks(msgView,Linkify.ALL)

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

        val popUpMenu = PopupMenu(context, v)
        popUpMenu.setOnMenuItemClickListener(this)
        popUpMenu.inflate(R.menu.menu_chat_clipboard)
        popUpMenu.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        val clip: ClipData = ClipData.newPlainText("Copy", msgView.text)
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()

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