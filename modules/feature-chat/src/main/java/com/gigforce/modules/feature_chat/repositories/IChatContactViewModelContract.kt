package com.gigforce.modules.feature_chat.repositories

import com.gigforce.common_ui.chat.models.ChatMessage

interface IChatContactViewModelContract {

    fun onNewMessagesReceived(chatMessage: List<ChatMessage>)

}