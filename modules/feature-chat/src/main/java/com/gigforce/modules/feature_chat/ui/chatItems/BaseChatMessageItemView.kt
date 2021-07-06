package com.gigforce.modules.feature_chat.ui.chatItems

import com.gigforce.common_ui.chat.models.ChatMessage

interface BaseChatMessageItemView {

    fun getCurrentChatMessageOrThrow() : ChatMessage
}