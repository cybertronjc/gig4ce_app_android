package com.gigforce.modules.feature_chat.adapters


import com.gigforce.modules.feature_chat.models.ChatMessage
import com.gigforce.modules.feature_chat.models.MessageType
import java.io.File

interface OnChatMessageClickListener {
        fun chatMessageClicked(
            messageType: MessageType,
            position: Int,
            message: ChatMessage,
            fileDownloaded : Boolean,
            downloadedFile : File?
        )
}