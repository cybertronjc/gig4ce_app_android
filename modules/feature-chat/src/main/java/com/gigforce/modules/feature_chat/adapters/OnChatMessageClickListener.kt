package com.gigforce.modules.feature_chat.adapters


import com.gigforce.modules.feature_chat.models.OldChatMessage
import com.gigforce.modules.feature_chat.models.MessageType
import java.io.File

interface OnChatMessageClickListener {
        fun chatMessageClicked(
                messageType: MessageType,
                position: Int,
                messageOld: OldChatMessage,
                fileDownloaded : Boolean,
                downloadedFile : File?
        )
}