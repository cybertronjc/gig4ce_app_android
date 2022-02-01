package com.gigforce.common_ui.chat

//import com.gigforce.modules.feature_chat.ChatLocalDirectoryReferenceManager
//import com.gigforce.modules.feature_chat.core.ChatConstants
//import com.gigforce.modules.feature_chat.models.*
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.gigforce.common_ui.chat.models.*
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.core.StringConstants
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.addOrThrow
import com.gigforce.core.extensions.commitOrThrow
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.file.FileUtils
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.contract


@Singleton
class ChatRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val chatLocalDirectoryReferenceManager: ChatFileManager
) : BaseChatRepository(),
    IChatService {

    private val userChatCollectionRef: DocumentReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHATS)
            .document(getUID())
    }

    private val userReportedCollectionRef: CollectionReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHAT_REPORTED_USER)
    }

    private fun getChatHeaderDocumentRef(userId: String, headerId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHATS)
            .document(userId)
            .collection(COLLECTION_CHAT_HEADERS)
            .document(headerId)
    }

    private fun getChatHeaderCollectionRef(userId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHATS)
            .document(userId)
    }

    private fun getChatMessagesCollectionRef(
        headerId: String
    ) = userChatCollectionRef
        .collection(COLLECTION_CHAT_HEADERS)
        .document(headerId)
        .collection(COLLECTION_CHATS_MESSAGES)

    suspend fun getChatHeader(chatHeaderId: String): ChatHeader? {
        if (chatHeaderId.isBlank())
            return null

        val docRef = userChatCollectionRef
            .collection(COLLECTION_CHAT_HEADERS)
            .document(chatHeaderId)
            .getOrThrow()

        if (docRef.exists()) {
            return docRef.toObject(ChatHeader::class.java)!!.apply {
                id = docRef.id
            }
        } else {
            return null
        }
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    override suspend fun sendTextMessage(
        chatHeaderId: String,
        message: ChatMessage
    ) {

        getChatMessagesCollectionRef(chatHeaderId)
            .addOrThrow(message)
    }

    override suspend fun updateMuteNotifications(enable: Boolean, headerId: String) {
        try {
            val chatHeaderReference = getChatHeaderDocumentRef(getUID() , headerId)
            chatHeaderReference.updateOrThrow(
                mapOf(
                    "settings.muteNotifications" to enable
                )
            )

        } catch (e: Exception){
            Log.d("ChatRepository", "MuteNot exc: ${e.message}")
        }
    }

     override suspend fun setLocationToSenderChatMessage(
        id: String,
        messageId: String,
        location: GeoPoint
    ){
        try {
            val chatMessagesRef = getChatMessagesCollectionRef(id)
            chatMessagesRef.document(messageId).updateOrThrow(
                    mapOf(
                        "location" to location,
                        "isCurrentlySharingLiveLocation" to true,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to getUID()
                )
                )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun stopLocationToSenderChatMessage(
        id: String,
        messageId: String,
        location: GeoPoint
    ){
        try {
            val chatMessagesRef = getChatMessagesCollectionRef(id)
            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "location" to location,
                    "isCurrentlySharingLiveLocation" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun stopLocationToReceiverChatMessage(
        id: String,
        receiverId: String,
        messageId: String,
        location: GeoPoint
    ) {
        try {
            val chatMessagesRef = FirebaseFirestore.getInstance()
                .collection(ChatGroupRepository.COLLECTION_CHATS)
                .document(receiverId).collection(COLLECTION_CHAT_HEADERS)
                .document(id)
                .collection(COLLECTION_CHATS_MESSAGES)

            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "location" to location,
                    "isCurrentlySharingLiveLocation" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun stopLocationForReceiver(id: String, messageId: String, receiverId: String) {
        try {
            val chatMessagesRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHATS)
                .document(receiverId).collection(COLLECTION_CHAT_HEADERS)
                .document(id)
                .collection(COLLECTION_CHATS_MESSAGES)

            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "isCurrentlySharingLiveLocation" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun setLocationToReceiverChatMessage(
        id: String,
        receiverId: String,
        messageId: String,
        location: GeoPoint
    ) {
        try {
            val chatMessagesRef = FirebaseFirestore.getInstance()
                .collection(ChatGroupRepository.COLLECTION_CHATS)
                .document(receiverId).collection(COLLECTION_CHAT_HEADERS)
                .document(id)
                .collection(COLLECTION_CHATS_MESSAGES)

            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "location" to location,
                    "isCurrentlySharingLiveLocation" to true,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun stopSharingLocation(id: String, messageId: String) {
        try {
            val chatMessagesRef = getChatMessagesCollectionRef(id)
            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "isCurrentlySharingLiveLocation" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun stopReceiverSharingLocation(
        id: String,
        messageId: String,
        receiverId: String
    ) {
        try {
            val chatMessagesRef = FirebaseFirestore.getInstance()
                .collection(ChatGroupRepository.COLLECTION_CHATS)
                .document(receiverId).collection(COLLECTION_CHAT_HEADERS)
                .document(id)
                .collection(COLLECTION_CHATS_MESSAGES)
            chatMessagesRef.document(messageId).updateOrThrow(
                mapOf(
                    "isCurrentlySharingLiveLocation" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
        } catch (e: Exception){
            Log.d("ChatRepository", "exc: ${e.message}")
        }
    }

    override suspend fun sendLocationMessage(
        chatHeaderId: String,
        message: ChatMessage,
        bitmap: Bitmap?
    ) {

        val attachmentPathOnServer = if (bitmap != null) {
            val imageInBytes = ImageUtils.convertToByteArray(bitmap)
            uploadChatAttachment(
                fileNameWithExtension = "map-${DateHelper.getFullDateTimeStamp()}.png",
                file = imageInBytes,
                headerId = chatHeaderId,
                isGroupChatMessage = false,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION
            )
        } else {
            null
        }

        message.attachmentPath = attachmentPathOnServer
        getChatMessagesCollectionRef(chatHeaderId)
            .addOrThrow(message)
    }

    override suspend fun sendImageMessage(
        chatHeaderId: String,
        message: ChatMessage,
        imageUri: Uri
    ) {
        val thumbnailPathOnServer = if (message.thumbnailBitmap != null) {
            val thumbnail = message.thumbnailBitmap!!.copy(message.thumbnailBitmap?.config, true)

            val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
            uploadChatAttachment(
                    fileNameWithExtension = "thumb-${message.imageMetaData?.name}",
                    file = imageInBytes,
                    headerId = chatHeaderId,
                    isGroupChatMessage = false,
                    messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
            )
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(
                fileNameWithExtension = message.imageMetaData?.name ?: "",
                file = imageUri,
                headerId = chatHeaderId,
                isGroupChatMessage = false,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
        )

        message.thumbnail = thumbnailPathOnServer
        message.attachmentPath = pathOnServer

        getChatMessagesCollectionRef(chatHeaderId)
            .addOrThrow(message)
    }

    override suspend fun sendVideoMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        uri: Uri,
        videoInfo: VideoInfo
    ) {

        val newFileName = if (videoInfo.name.isBlank()) {
            "${getUID()}-${DateHelper.getFullDateTimeStamp()}.mp4"
        } else {

            if (videoInfo.name.endsWith(".mp4", true)) {
                "${getUID()}-${DateHelper.getFullDateTimeStamp()}-${videoInfo.name}"
            } else {
                "${getUID()}-${DateHelper.getFullDateTimeStamp()}-${videoInfo.name}.mp4"
            }
        }

        val videosDirectoryRef = chatLocalDirectoryReferenceManager.videoFilesDirectory
        if (!videosDirectoryRef.exists())
            videosDirectoryRef.mkdirs()

        val shouldCompressVideo = shouldCompressVideo(videoInfo)
        val compressedFileUri = if (shouldCompressVideo) {
            val transcodedFile = File(
                videosDirectoryRef,
                newFileName
            )
            transcodeVideo(context, uri, transcodedFile)
            transcodedFile.toUri()
        } else {
            val file = File(videosDirectoryRef, newFileName)

            FileUtils.copyFile(context, newFileName, uri, file)
            file.toUri()
        }

        val thumbnailPathOnServer = if (message.thumbnailBitmap != null) {
            val thumbnail = message.thumbnailBitmap!!.copy(
                message.thumbnailBitmap?.config,
                message.thumbnailBitmap!!.isMutable
            )

            val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
            uploadChatAttachment(
                "thumb-$newFileName",
                imageInBytes,
                chatHeaderId,
                isGroupChatMessage = false,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO
            )
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(
            newFileName,
            compressedFileUri,
            chatHeaderId,
            isGroupChatMessage = false,
            messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO
        )

        message.attachmentPath = pathOnServer
        message.thumbnail = thumbnailPathOnServer

        getChatMessagesCollectionRef(chatHeaderId)
            .addOrThrow(message)
    }

    override suspend fun sendAudioMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        audiosDirectoryRef: File,
        file: Uri,
        audioInfo: AudioInfo
    ) {
        val newFileName = if (audioInfo.name.isBlank()) {
            "${getUID()}-${DateHelper.getFullDateTimeStamp()}.mp3"
        } else {

            if (audioInfo.name.endsWith(".mp3", true)) {
                "${getUID()}-${DateHelper.getFullDateTimeStamp()}-${audioInfo.name}"
            } else {
                "${getUID()}-${DateHelper.getFullDateTimeStamp()}-${audioInfo.name}.mp3"
            }
        }

//        val audiosDirectoryRef = chatLocalDirectoryReferenceManager.audioFilesDirectory
//        val audioFile = File(audiosDirectoryRef, newFileName)
//        FileUtils.copyFile(context.applicationContext, newFileName, file, audioFile)

        if (!audiosDirectoryRef.exists())
            audiosDirectoryRef.mkdirs()
        val audioFile = File(audiosDirectoryRef, newFileName)
        FileUtils.copyFile(context, newFileName, file, audioFile)

        val pathOnServer = uploadChatAttachment(
            fileNameWithExtension = newFileName,
            file = file,
            headerId = chatHeaderId,
            isGroupChatMessage = false,
            messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO
        )
        message.attachmentPath = pathOnServer

        getChatMessagesCollectionRef(headerId = chatHeaderId)
            .addOrThrow(message)
    }

    override suspend fun sendDocumentMessage(
        context: Context,
        chatHeaderId: String,
        message: ChatMessage,
        fileName: String,
        uri: Uri
    ) {

        val newFileName = "Doc-${getUID()}-${DateHelper.getFullDateTimeStamp()}.${
            getExtensionFromUri(
                context,
                uri
            )
        }"

        val documentsDirectoryRef = chatLocalDirectoryReferenceManager.documentFilesDirectory
        val documentFile = File(documentsDirectoryRef, newFileName)
        FileUtils.copyFile(context.applicationContext, fileName, uri, documentFile)

        val pathOnServer = uploadChatAttachment(
            fileNameWithExtension = newFileName,
            file = uri,
            headerId = chatHeaderId,
            isGroupChatMessage = false,
            messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT
        )
        message.attachmentPath = pathOnServer

        getChatMessagesCollectionRef(headerId = chatHeaderId)
            .addOrThrow(message)
    }


    override suspend fun createHeaders(
        otherUserId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getHeaderFromHeaders(
        userId: String
    ) {
        TODO("Not yet implemented")
    }


    override suspend fun reportAndBlockUser(
        chatHeaderId: String,
        otherUserId: String,
        reason: String
    ) {

        blockOrUnblockUser(
            chatHeaderId,
            otherUserId,
            true
        )

        userReportedCollectionRef
            .addOrThrow(
                ChatReportedUser(
                    id = null,
                    reportedUserUid = otherUserId,
                    reportedBy = getUID(),
                    reportedOn = Timestamp.now(),
                    reportingReason = reason
                )
            )

    }

    override suspend fun blockOrUnblockUser(
        chatHeaderId: String,
        otherUserId: String,
        forceBlock: Boolean
    ) {
        if (forceBlock) {

            if (chatHeaderId.isNotBlank()) {
                userChatCollectionRef
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(chatHeaderId)
                    .updateOrThrow(
                        mapOf(
                            "isBlocked" to true,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                .getCurrentSignInUserInfoOrThrow().uid
                        )
                    )
            }

            val contactDetails = if (otherUserId.isNotBlank())
                getDetailsOfUserFromContacts(otherUserId)
            else
                null

            if (contactDetails != null)
                tryUpdatingBlockedInFlagInContacts(contactDetails.mobile, true)
        } else {
            var isUserBlocked = false
            val chatHeader = if (chatHeaderId.isNotBlank())
                getChatHeader(chatHeaderId)
            else
                null

            val contactDetails = if (otherUserId.isNotBlank())
                getDetailsOfUserFromContacts(otherUserId)
            else
                null

            isUserBlocked = chatHeader?.isBlocked ?: false || contactDetails?.isUserBlocked ?: false

            if (chatHeaderId.isNotBlank()) {
                userChatCollectionRef
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(chatHeaderId)
                    .updateOrThrow(
                        mapOf(
                            "isBlocked" to !isUserBlocked,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                .getCurrentSignInUserInfoOrThrow().uid
                        )
                    )
            }

            if (contactDetails != null)
                tryUpdatingBlockedInFlagInContacts(contactDetails.mobile, !isUserBlocked)
        }
    }

    private suspend fun tryUpdatingBlockedInFlagInContacts(
        otherUserMobileNo: String,
        block: Boolean
    ) {
        if (otherUserMobileNo.isEmpty())
            return

        try {
            getDetailsOfUserFromContactsQuery(
                otherUserMobileNo = formatMobileNoForChatContact(
                    otherUserMobileNo
                )
            )
                .updateOrThrow(
                    mapOf(
                        "isUserBlocked" to block,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    )
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun setMessagesAsRead(unreadMessages: List<ChatMessage>) {
        if (unreadMessages.isNotEmpty()) {
            val unreadMessage = unreadMessages.first()

            val senderId = unreadMessage.senderInfo.id
            val headerId = unreadMessage.headerId

            val headerRef = FirebaseFirestore.getInstance()
                .collection(ChatGroupRepository.COLLECTION_CHATS)
                .document(senderId)
                .collection(COLLECTION_CHAT_HEADERS)
                .document(headerId)

            val chatHeader = headerRef.getOrThrow()
            val lastMessageIdInHeader = chatHeader.getString("lastMsgId")

            val chatMessageCollection = headerRef.collection(COLLECTION_CHATS_MESSAGES)

            val batch = db.batch()
            if (lastMessageIdInHeader != null) {
                val shouldUpdateInHeader = getChatMessagesCollectionRef(headerId)
                    .document(lastMessageIdInHeader)
                    .getOrThrow()
                    .exists()

                if (shouldUpdateInHeader) {
                    batch.update(
                        headerRef, mapOf(
                            "status" to ChatConstants.MESSAGE_STATUS_READ_BY_USER,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                .getCurrentSignInUserInfoOrThrow().uid
                        )
                    )
                }
            }

            unreadMessages.forEach {

                val messageRef = chatMessageCollection.document(it.senderMessageId)
                batch.update(
                    messageRef, mapOf(
                        "status" to ChatConstants.MESSAGE_STATUS_READ_BY_USER,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    )
                )
            }

            batch.commitOrThrow()
        }
    }

    override suspend fun setHeadersAsRead(headerIds: List<String>, senderId: String) {
        if (headerIds.isNotEmpty()){
            val batch = db.batch()

            headerIds.forEach {

                val headerReference = FirebaseFirestore.getInstance()
                    .collection(COLLECTION_CHATS)
                    .document(senderId)
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(it)


                batch.update(
                    headerReference, mapOf(
                        "unseenCount" to ChatConstants.MARK_AS_READ,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    )
                )
            }

            batch.commitOrThrow()

        }
    }

    override suspend fun setHeaderMuteNotifications(headerIds: List<String>, enable: Boolean) {
        if (headerIds.isNotEmpty()){
            val batch = db.batch()

            headerIds.forEach {

                val headerReference = FirebaseFirestore.getInstance()
                    .collection(COLLECTION_CHATS)
                    .document(getUID())
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(it)


                batch.update(
                    headerReference, mapOf(
                        "settings.muteNotifications" to enable
                    )
                )
            }

            batch.commitOrThrow()

        }
    }

    override suspend fun forwardChatMessage(
        contacts: List<ContactModel>,
        chatMessage: ChatMessage
    ) {
        if (contacts.isNotEmpty()){
            val batch = db.batch()

            contacts.forEach {
                val headerRef = FirebaseFirestore.getInstance()
                    .collection(COLLECTION_CHATS)
                    .document(it.uid.toString())
                    .collection("headers")
                    .document(it.headerId.toString())

                val chatMessageRef = headerRef.collection(COLLECTION_CHATS_MESSAGES).document(chatMessage.id)
                batch.set(chatMessageRef, chatMessage)
            }
            batch.commitOrThrow()
        }
    }

    private fun shouldCompressVideo(videoInfo: VideoInfo): Boolean {
        if (videoInfo.size != 0L) {
            if (videoInfo.size <= ChatConstants.MB_10) {
                return false
            } else if (videoInfo.size <= ChatConstants.MB_15 && videoInfo.duration <= ChatConstants.TWO_MINUTES) {
                return false
            } else if (videoInfo.size <= ChatConstants.MB_25 && videoInfo.duration <= ChatConstants.FIVE_MINUTES) {
                return false
            }
        }

        return true
    }

    suspend fun getDetailsOfUserFromContacts(otherUserId: String): ContactModel {
        val contactRef = db.collection(COLLECTION_CHATS)
            .document(getUID())
            .collection(COLLECTION_CHATS_CONTACTS)
            .whereEqualTo("uid", otherUserId)
            .getOrThrow()

        return if (contactRef.isEmpty) {
            ContactModel(id = otherUserId)
        } else {
            contactRef.first().toObject(ContactModel::class.java).apply {
                this.id = contactRef.first().id
            }
        }
    }

    fun getDetailsOfUserFromContactsQuery(otherUserMobileNo: String): DocumentReference {
        return db.collection(COLLECTION_CHATS)
            .document(getUID())
            .collection(COLLECTION_CHATS_CONTACTS)
            .document(otherUserMobileNo)

    }

    suspend fun sentMessagesSentMessageAsDelivered(
        headerId: String,
        otherUserId: String
    ) {

        val headerRef = getChatHeaderCollectionRef(otherUserId)
            .collection(COLLECTION_CHAT_HEADERS)
            .document(headerId)

        val chatHeader = headerRef.getOrThrow()
        val lastMessageIdInHeader = chatHeader.getString("lastMsgId")

        val chatCollectionRef = headerRef.collection(COLLECTION_CHATS_MESSAGES)
        val querySnap = chatCollectionRef
            .whereLessThan("status", ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER)
            .getOrThrow()

        Log.d("ChatRepo", "Other User - $otherUserId, header-id - $headerId")
        if (querySnap.size() > 0) {

            val batch = db.batch()
            querySnap.documents.forEach {
                val blockedDuringMessageWasSent =
                    it.getBoolean("wasUserBlockedByOtherUserWhenMessageWasSent") ?: false

                if (!blockedDuringMessageWasSent) {
                    Log.d("ChatRepo", "Message Id - ${it.id}")
                    val messageDocRef = chatCollectionRef.document(it.id)
                    batch.update(
                        messageDocRef, mapOf(
                            "status" to ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                .getCurrentSignInUserInfoOrThrow().uid
                        )
                    )
                }
            }

            if (lastMessageIdInHeader != null) {
                val shouldUpdateInHeader = getChatMessagesCollectionRef(headerId)
                    .document(lastMessageIdInHeader)
                    .getOrThrow()
                    .exists()

                if (shouldUpdateInHeader) {
                    batch.update(
                        headerRef, mapOf(
                            "status" to ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                .getCurrentSignInUserInfoOrThrow().uid
                        )
                    )
                }
            }

            batch.commitOrThrow()
        }
    }

    fun getExtensionFromUri(
        context: Context,
        uri: Uri
    ): String? {

        return if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme)) {
            val cr: ContentResolver = context.contentResolver
            val mimeType = cr.getType(uri)
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val mimeType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }
    }

    fun formatMobileNoForChatContact(
        mobileNo: String
    ): String {
        return if (mobileNo.length == 10) {
            "91$mobileNo"
        } else if (mobileNo.length == 12) {
            mobileNo
        } else if (mobileNo.length > 12) {
            mobileNo.substring(1)
        } else {
            throw IllegalArgumentException("invalid mobile no")
        }
    }

    /**
     * Updating delete flag from current users messages , on update
     * a trigger runs and updated flag in other users messages
     */
    suspend fun deleteMessage(
        chatHeaderId: String,
        messageId: String
    ) = db.collection(COLLECTION_CHATS)
        .document(getUID())
        .collection(COLLECTION_CHAT_HEADERS)
        .document(chatHeaderId)
        .collection(COLLECTION_CHATS_MESSAGES)
        .document(messageId)
        .updateOrThrow(
            mapOf(
                "isDeleted" to true,
                "deletedOn" to Timestamp.now(),
                "updatedAt" to Timestamp.now(), "updatedBy" to getUID()
            )
        )

    suspend fun deleteMessages(messageIds: List<String>, headerId: String) {
        if (messageIds.isNotEmpty()){
            val batch = db.batch()

            messageIds.forEach {

                val headerReference = FirebaseFirestore.getInstance()
                    .collection(COLLECTION_CHATS)
                    .document(getUID())
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(headerId)
                    .collection(COLLECTION_CHATS_MESSAGES)
                    .document(it)


                batch.update(
                    headerReference, mapOf(
                        "isDeleted" to true,
                        "deletedOn" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(), "updatedBy" to getUID()
                    )
                )
            }

            batch.commitOrThrow()

        }
    }


    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_CHATS_MESSAGES = "chat_messages"

        const val COLLECTION_GROUP_CHATS = "chat_groups"
        const val COLLECTION_GROUP_MESSAGES = "group_messages"
        const val COLLECTION_CHAT_HEADERS = "headers"
    }
}