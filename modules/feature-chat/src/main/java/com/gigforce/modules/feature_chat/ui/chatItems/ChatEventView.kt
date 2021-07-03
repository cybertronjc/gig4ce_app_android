package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.common_ui.DisplayUtil
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.IViewHolder
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel


class ChatEventView(
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder {

    private lateinit var msgView: TextView

    private lateinit var message: ChatMessage
    private lateinit var oneToOneChatViewModel: ChatPageViewModel
    private lateinit var groupChatViewModel: GroupChatViewModel

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item_event, this, true)
        loadViews(view)
    }

    fun loadViews(
            view: View
    ) {
        msgView = this.findViewById(R.id.cv_msgContainer)
    }

    override fun bind(data: Any?) {
        data?.let {
            val dataAndViewModels = it as ChatMessageWrapper
            message = dataAndViewModels.message
            groupChatViewModel = dataAndViewModels.groupChatViewModel
            oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

            msgView.text = message.eventInfo?.eventText?: ""
        }
    }


}
