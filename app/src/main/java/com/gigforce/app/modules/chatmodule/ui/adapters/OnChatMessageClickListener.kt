package com.gigforce.app.modules.chatmodule.ui.adapters

import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType

interface OnChatMessageClickListener {
        fun chatMessageClicked(
            messageType: MessageType,
            position: Int,
            message: ChatMessage
        )
}