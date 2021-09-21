package com.gigforce.common_ui.chat

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.VideoInfo

//import com.gigforce.modules.feature_chat.models.ChatMessage
//import com.gigforce.modules.feature_chat.models.VideoInfo

interface IChatService {

    suspend fun sendTextMessage(
        chatHeaderId: String,
        message: ChatMessage
    )

    suspend fun sendVideoMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        uri: Uri,
        videoInfo: VideoInfo
    )

    suspend fun sendImageMessage(
        chatHeaderId: String,
        message: ChatMessage,
        imageUri: Uri
    )

    suspend fun sendDocumentMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        fileName: String,
        file: Uri
    )

    suspend fun sendLocationMessage(
        chatHeaderId: String,
        message: ChatMessage,
        bitmap: Bitmap?
    )

    suspend fun createHeaders(
        otherUserId: String
    )

    suspend fun getHeaderFromHeaders(
        userId: String
    )

    suspend fun reportAndBlockUser(
        chatHeaderId: String,
        otherUserId: String,
        reason: String
    )

    suspend fun blockOrUnblockUser(
        chatHeaderId: String,
        otherUserId: String,
        forceBlock : Boolean = false
    )

    suspend fun setMessagesAsRead(
        unreadMessages: List<ChatMessage>
    )
}