package com.gigforce.modules.feature_chat.swipe

import com.gigforce.common_ui.chat.models.ChatMessage

interface SwipeControllerActions {

    fun showReplyUI(chatMessage: ChatMessage)
}