package com.gigforce.modules.feature_chat.models

import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel

data class ChatMessageWrapper(
        val message : ChatMessage,
        val oneToOneChatViewModel : ChatPageViewModel,
        val groupChatViewModel : GroupChatViewModel,
)
