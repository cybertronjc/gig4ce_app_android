package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper


abstract class DeletedMessageView(
        private val type: MessageFlowType,
        private val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder {

    private lateinit var containerView: View
    private lateinit var senderNameTV: TextView
    private lateinit var timeView: TextView

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val view = if (type == MessageFlowType.IN)
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_deleted_message_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_deleted_message_out, this, true)

        loadViews(view)
    }

    private fun loadViews(
            view: View
    ) {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
        containerView = this.findViewById(R.id.ll_msgContainer)
    }

    override fun bind(data: Any?) {
        data?.let {
            val message = (it as ChatMessageWrapper).message

            senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = message.senderInfo.name
            timeView.text = message.timestamp?.toDisplayText()
        }
    }
}

class InDeletedMessageView(
        context: Context,
        attrs: AttributeSet?
) : DeletedMessageView(
        MessageFlowType.IN,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class OutDeletedMessageView(
        context: Context,
        attrs: AttributeSet?
) : DeletedMessageView(
        MessageFlowType.OUT,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class GroupInDeletedMessageView(
        context: Context,
        attrs: AttributeSet?
) : DeletedMessageView(
        MessageFlowType.IN,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)

class GroupOutDeletedMessageView(
        context: Context,
        attrs: AttributeSet?
) : DeletedMessageView(
        MessageFlowType.OUT,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)