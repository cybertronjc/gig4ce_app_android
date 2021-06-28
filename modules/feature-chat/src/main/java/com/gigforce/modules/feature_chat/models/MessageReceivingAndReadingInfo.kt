package com.gigforce.modules.feature_chat.models

import com.gigforce.common_ui.chat.models.MessageReceivingInfo

data class MessageReceivingAndReadingInfo(
    val totalMembers : Int,
    val receivingInfo : List<MessageReceivingInfo>,
    val readingInfo : List<MessageReceivingInfo>
)
