package com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners

import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType
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