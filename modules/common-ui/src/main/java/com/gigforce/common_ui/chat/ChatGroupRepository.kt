package com.gigforce.common_ui.chat

//import com.gigforce.modules.feature_chat.models.*
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.gigforce.common_ui.chat.models.*
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.*
import com.gigforce.core.file.FileUtils
import com.gigforce.core.image.ImageUtils
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatGroupRepository constructor(
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository = ChatProfileFirebaseRepository()
        //  private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : BaseChatRepository() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val userChatCollectionRef: DocumentReference by lazy {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHATS)
                .document(getUID())
    }

    private val userChatContactsCollectionRef: CollectionReference by lazy {
        userChatCollectionRef.collection(COLLECTION_CHATS_CONTACTS)
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    fun getUserContacts(): CollectionReference {
        return userChatCollectionRef
                .collection(COLLECTION_CHATS_CONTACTS)
    }

    fun groupMessagesRef(groupId: String) = db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .collection(COLLECTION_GROUP_MESSAGES)

    fun userGroupHeaderRef(groupId: String) = db.collection(COLLECTION_CHATS)
            .document(getUID())
            .collection(COLLECTION_CHAT_HEADERS)
            .document(groupId)

    suspend fun createGroup(groupName: String, groupMembers: List<ContactModel>): String {

        val currentUserInfo = createContactModelsForCurrentUser()
        val members = groupMembers.toMutableList().apply {
            add(currentUserInfo)
        }

        members.forEach { it.name = "" }

        val group = createGroupData(groupName, members, currentUserInfo)
        val groupDocRef = db.collection(COLLECTION_GROUP_CHATS).addOrThrow(group)
        group.id = groupDocRef.id

        //Creating Headers in each users doc
        val batch = db.batch()
        members.forEach {

            val headerRef = db.collection(COLLECTION_CHATS)
                    .document(it.uid!!)
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(group.id)

            batch.set(
                    headerRef, ChatHeader(
                    forUserId = it.uid!!,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    groupId = group.id,
                    groupName = groupName,
                    lastMsgTimestamp = Timestamp.now(),
                    lastMsgFlowType = ChatConstants.FLOW_TYPE_OUT
            )
            )
        }
        batch.commitOrThrow()
        return group.id
    }

    suspend fun addUserToGroup(groupId: String, members: List<ContactModel>) {

        val groupInfo = getGroupDetails(groupId)
        val grpMembers = groupInfo.groupMembers.toMutableList()

        //Creating Headers in each users doc
        val batch = db.batch()

        val filteredMemList = members.filter {
            grpMembers.find { grpMem -> grpMem.uid == it.uid } == null
        }

        filteredMemList.forEach {
            it.name = chatProfileFirebaseRepository.getProfileDataIfExist(it.uid)?.name ?: ""
        }

        grpMembers.addAll(filteredMemList)
        groupInfo.groupMembers = grpMembers

        val groupDataRef = db.collection(COLLECTION_GROUP_CHATS).document(groupId)
        batch.set(groupDataRef, groupInfo)

        filteredMemList.forEach {
            val headerRef = db.collection(COLLECTION_CHATS)
                    .document(it.uid!!)
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(groupId)

            batch.set(
                    headerRef, ChatHeader(
                    forUserId = it.uid!!,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    groupId = groupId,
                    groupName = groupInfo.name,
                    lastMsgTimestamp = Timestamp.now(),
                    removedFromGroup = false,
                    lastMsgFlowType = ChatConstants.FLOW_TYPE_OUT
            )
            )
        }
        batch.commitOrThrow()
    }

    suspend fun getProfileData(): ChatProfileData = suspendCoroutine { cont ->

        db.collection("Profiles")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener {

                    val profileData = it.toObject(ChatProfileData::class.java)
                            ?: throw  IllegalStateException("unable to parse profile object")
                    profileData.id = it.id
                    cont.resume(profileData)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }


    private suspend fun createContactModelsForCurrentUser(): ContactModel {
        val profile = getProfileData()

        val profilePic =
                if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
                    null
                else {
                    "profile_pics/${profile.profileAvatarName}"
                }

        return ContactModel(
                name = profile.name,
                uid = getUID(),
                imageUrl = profilePic,
                isUserGroupManager = true,
                mobile = currentUser.phoneNumber ?: ""
        )
    }

    private suspend fun createGroupData(
            groupName: String,
            groupMembers: List<ContactModel>,
            currentUserInfo: ContactModel
    ): ChatGroup {
        val chatGroup = ChatGroup(
                name = groupName,
                groupMembers = groupMembers,
                creationDetails = GroupCreationDetails(
                        createdBy = currentUserInfo.uid!!,
                        creatorName = currentUserInfo.name!!,
                        createdOn = Timestamp.now()
                )
        )

        chatGroup.groupMembers.forEach {

            val profileName = chatProfileFirebaseRepository.getProfileDataIfExist(it.uid)
            it.name = profileName?.name ?: ""
        }

        return chatGroup
    }

    fun getGroupDetailsRef(groupId: String) =
            db.collection(COLLECTION_GROUP_CHATS).document(groupId)

    suspend fun getGroupDetails(groupId: String): ChatGroup {
        val groupSnap = getGroupDetailsRef(groupId).getOrThrow()
        return groupSnap.toObject(ChatGroup::class.java)!!.apply {
            id = groupId
        }
    }

    suspend fun sendTextMessage(
            groupId: String,
            message: ChatMessage
    ) {
        createMessageEntry(groupId, message)
    }

    suspend fun sendNewImageMessage(
            groupId: String,
            message: ChatMessage,
            imageUri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        val file = imageUri.toFile()
        val thumbnail = message.thumbnailBitmap?.copy(message.thumbnailBitmap?.config, true)

        val thumbnailPathOnServer = if (thumbnail != null) {
            val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
            uploadChatAttachment(
                    "thumb-${file.name}",
                    imageInBytes,
                    groupId,
                    isGroupChatMessage = true,
                    messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
            )
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(
                file.name,
                imageUri,
                groupId,
                isGroupChatMessage = true,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
        )
        message.thumbnail = thumbnailPathOnServer
        message.attachmentPath = pathOnServer

        createMessageEntry(groupId, message)
        updateMediaInfoInGroupMedia(
                groupId,
                ChatConstants.ATTACHMENT_TYPE_IMAGE,
                message.id,
                "",
                pathOnServer,
                thumbnailPathOnServer
        )
    }


    @Suppress("DEPRECATION")
    suspend fun sendNewVideoMessage(
            context: Context,
            groupId: String,
            videosDirectoryRef: File,
            videoInfo: VideoInfo,
            uri: Uri,
            message: ChatMessage
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

        val thumbnail = message.thumbnailBitmap?.copy(message.thumbnailBitmap?.config, true)
        val thumbnailPathOnServer = if (thumbnail != null) {
            val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
            uploadChatAttachment(
                    "thumb-$newFileName",
                    imageInBytes,
                    groupId,
                    isGroupChatMessage = true,
                    messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
            )
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(
                newFileName,
                compressedFileUri,
                groupId,
                true,
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO
        )

        message.attachmentPath = pathOnServer
        message.thumbnail = thumbnailPathOnServer

        createMessageEntry(groupId, message)
        updateMediaInfoInGroupMedia(
                groupId,
                ChatConstants.ATTACHMENT_TYPE_VIDEO,
                message.id,
                videoInfo.name,
                pathOnServer,
                thumbnailPathOnServer,
                message.videoLength
        )
    }

    suspend fun sendNewDocumentMessage(
            context: Context,
            groupId: String,
            message: ChatMessage,
            fileName: String,
            uri: Uri
    ) {
        val newFileName =
                "Doc-$groupId-${DateHelper.getFullDateTimeStamp()}.${getExtensionFromUri(context, uri)}"


        val pathOnServer = uploadChatAttachment(
                newFileName,
                uri,
                groupId,
                isGroupChatMessage = true,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT
        )
        message.attachmentPath = pathOnServer

        createMessageEntry(groupId, message)
        updateMediaInfoInGroupMedia(
                groupId,
                ChatConstants.ATTACHMENT_TYPE_DOCUMENT,
                message.id,
                fileName,
                pathOnServer,
                null
        )
    }


    private suspend fun createMessageEntry(
            groupId: String,
            message: ChatMessage
    ) {
        db.collection(COLLECTION_GROUP_CHATS)
                .document(groupId)
                .collection(COLLECTION_GROUP_MESSAGES)
                .document(message.id)
                .setOrThrow(message)
    }


    private suspend fun updateMediaInfoInGroupMedia(
            groupId: String,
            type: String,
            messageId: String,
            fileName: String?,
            pathOnServer: String,
            thumbnailPath: String? = null,
            videoAttachmentLength: Long = 0
    ) {
        db.collection(COLLECTION_GROUP_CHATS)
                .document(groupId)
                .updateOrThrow(
                        "groupMedia", FieldValue.arrayUnion(
                        GroupMedia(
                                id = UUID.randomUUID().toString(),
                                groupHeaderId = groupId,
                                messageId = messageId,
                                attachmentType = type,
                                timestamp = Timestamp.now(),
                                thumbnail = thumbnailPath,
                                attachmentName = fileName,
                                attachmentPath = pathOnServer,
                                videoAttachmentLength = videoAttachmentLength
                        )
                )
                )
    }

    suspend fun changeGroupName(groupId: String, newGroupName: String) {

        db.collection(COLLECTION_GROUP_CHATS)
                .document(groupId)
                .updateOrThrow("name", newGroupName)

        val groupDetails = getGroupDetails(groupId)

        val batch = db.batch()
        groupDetails.groupMembers.forEach {

            val headerRef = db.collection(COLLECTION_CHATS)
                    .document(it.uid!!)
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(groupId)

            batch.update(
                    headerRef,
                    mapOf("groupName" to newGroupName)
            )
        }
        batch.commitOrThrow()
    }

    suspend fun setUnseenMessagecountToZero(groupHeaderId: String) {
        db.collection("chats")
                .document(getUID())
                .collection("headers")
                .document(groupHeaderId)
                .updateOrThrow("unseenCount", 0)
    }

    suspend fun deactivateOrActivateGroup(groupHeaderId: String) {
        val groupDetails = getGroupDetails(groupHeaderId)

        val batch = db.batch()
        val groupRef = db.collection(COLLECTION_GROUP_CHATS)
                .document(groupHeaderId)
        batch.update(groupRef, "groupDeactivated", !groupDetails.groupDeactivated)

        groupDetails.groupMembers.forEach {
            val headerRef = db.collection("chats")
                    .document(it.uid!!)
                    .collection("headers")
                    .document(groupHeaderId)

            batch.update(headerRef, "groupDeactivated", !groupDetails.groupDeactivated)
        }

        batch.commit()
    }

    suspend fun removeUserFromGroup(groupHeaderId: String, userUid: String) {
        val groupDetails = getGroupDetails(groupHeaderId)
        val updatedUserList = groupDetails.groupMembers.filter {
            it.uid != userUid
        }
        groupDetails.groupMembers = updatedUserList

        val batch = db.batch()
        val groupRef = db.collection(COLLECTION_GROUP_CHATS)
                .document(groupHeaderId)
        batch.set(groupRef, groupDetails)

        val userHeaderRef = db.collection(COLLECTION_CHATS)
                .document(userUid)
                .collection(COLLECTION_CHAT_HEADERS)
                .document(groupHeaderId)
        batch.update(userHeaderRef, "removedFromGroup", true)

        batch.commit()
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

    suspend fun sendLocationMessage(
            groupId: String,
            message: ChatMessage,
            bitmap: Bitmap?
    ) {

        val attachmentPathOnServer = if (bitmap != null) {
            val imageInBytes = ImageUtils.convertToByteArray(bitmap)
            uploadChatAttachment(
                    fileNameWithExtension = "map-${DateHelper.getFullDateTimeStamp()}.png",
                    file = imageInBytes,
                    headerId = groupId,
                    isGroupChatMessage = false,
                    messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION
            )
        } else {
            null
        }

        message.attachmentPath = attachmentPathOnServer
        createMessageEntry(groupId, message)
//        updateMediaInfoInGroupMedia(
//                groupId,
//                ChatConstants.ATT,
//                message.id,
//                "",
//                attachmentPathOnServer,
//                thumbnailPathOnServer,
//                message.videoLength
//        )
    }

    fun getExtensionFromUri(
            context: Context,
            uri: Uri
    ): String? {

        return if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme)) {
            val cr: ContentResolver = context.getContentResolver()
            val mimeType = cr.getType(uri)
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }
    }

    suspend fun deleteMessage(
            groupId: String,
            messageId: String
    ) = db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .collection(COLLECTION_GROUP_MESSAGES)
            .document(messageId)
            .deleteOrThrow()

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_GROUP_CHATS = "chat_groups"
        const val COLLECTION_GROUP_MESSAGES = "group_messages"
        const val COLLECTION_CHAT_HEADERS = "headers"
        const val COLLECTION_CHAT_REPORTED_USER = "chat_reported_users"
    }

}