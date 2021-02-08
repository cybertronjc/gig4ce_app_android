package com.gigforce.modules.feature_chat.repositories

import com.gigforce.modules.feature_chat.models.ChatMessage

interface IChatContactViewModelContract {

    fun onNewMessagesReceived(chatMessage: List<ChatMessage>)

}