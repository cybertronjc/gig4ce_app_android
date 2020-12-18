package com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners

import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.GroupChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType
import java.io.File

interface OnGroupChatMessageClickListener {

        fun chatMessageClicked(
            messageType: MessageType,
            position: Int,
            message: GroupChatMessage,
            fileDownloaded : Boolean,
            downloadedFile : File?
        )
}