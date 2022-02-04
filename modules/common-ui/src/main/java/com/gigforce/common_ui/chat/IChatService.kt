package com.gigforce.common_ui.chat

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.gigforce.common_ui.chat.models.AudioInfo
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.chat.models.VideoInfo
import com.google.firebase.firestore.GeoPoint
import java.io.File

//import com.gigforce.modules.feature_chat.models.ChatMessage
//import com.gigforce.modules.feature_chat.models.VideoInfo

interface IChatService {

    suspend fun sendMessages(
        chatHeaderId: String,
        message: List<ChatMessage>
    )

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

    suspend fun sendAudioMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        audiosDirectoryRef: File,
        file: Uri,
        audioInfo: AudioInfo
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

    suspend fun setHeadersAsRead(
        headerIds: List<String>,
        senderId: String
    )

    suspend fun setHeaderMuteNotifications(
        headerIds: List<String>,
        enable: Boolean
    )

    suspend fun forwardChatMessage(
        contacts: List<ContactModel>,
        chatMessage: ChatMessage
    )

    suspend fun setLocationToSenderChatMessage(
        id: String,
        messageId: String,
        location: GeoPoint
    )

    suspend fun stopLocationToSenderChatMessage(
        id: String,
        messageId: String,
        location: GeoPoint
    )

    suspend fun setLocationToReceiverChatMessage(
        id: String,
        receiverId: String,
        messageId: String,
        location: GeoPoint,
    )

    suspend fun stopLocationToReceiverChatMessage(
        id: String,
        receiverId: String,
        messageId: String,
        location: GeoPoint,
    )

    suspend fun stopLocationForReceiver(
        id: String,
        messageId: String,
        receiverId: String
    )

    suspend fun stopSharingLocation(
        id: String,
        messageId: String
    )

    suspend fun stopReceiverSharingLocation(
        id: String,
        messageId: String,
        receiverId: String
    )

    suspend fun updateMuteNotifications(
        enable: Boolean,
        headerId: String
    )
}