package com.gigforce.modules.feature_chat.repositories

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.*
import com.gigforce.core.file.FileUtils
import com.gigforce.core.file.FileUtils.copyFile
import com.gigforce.core.image.ImageUtils
import com.gigforce.modules.feature_chat.models.*
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

class ChatGroupRepository constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
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
                    lastMsgTimestamp = Timestamp.now()
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
                    removedFromGroup = false
                )
            )
        }
        batch.commitOrThrow()
    }


    private suspend fun createContactModelsForCurrentUser(): ContactModel {
//        val profile = profileFirebaseRepository.getProfileData()
//        val profilePic =
//            if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
//                null
//            else {
//                firebaseStorage
//                    .reference
//                    .child("profile_pics")
//                    .child(profile.profileAvatarName)
//                    .getDownloadUrlOrThrow().toString()
//            }
//        return ContactModel(
//            name = profile.name,
//            uid = getUID(),
//            imageUrl = profilePic,
//            isUserGroupManager = true,
//            mobile = currentUser.phoneNumber ?: ""
//        )

        //todo fix it
                return ContactModel(
            name = "test",
            uid = getUID(),
            imageUrl = "",
            isUserGroupManager = true,
            mobile = currentUser.phoneNumber ?: ""
        )
    }

    private fun createGroupData(
        groupName: String,
        groupMembers: List<ContactModel>,
        currentUserInfo: ContactModel
    ): ChatGroup {
        return ChatGroup(
            name = groupName,
            groupMembers = groupMembers,
            creationDetails = GroupCreationDetails(
                createdBy = currentUserInfo.uid!!,
                creatorName = currentUserInfo.name!!,
                createdOn = Timestamp.now()
            )
        )
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
        groupMembers: List<ContactModel>,
        message: GroupMessage
    ) {
        createMessageEntry(groupId, groupMembers, message)
    }

    suspend fun sendNewImageMessage(
        groupId: String,
        groupMembers: List<ContactModel>,
        message: GroupMessage,
        imageUri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        val file = imageUri.toFile()
        val thumbnailPathOnServer = if (message.thumbnailBitmap != null) {
            val imageInBytes = ImageUtils.convertToByteArray(message.thumbnailBitmap!!)
            uploadChatAttachment("thumb-${file.name}", imageInBytes)
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(file.name, imageUri)
        message.thumbnail = thumbnailPathOnServer
        message.attachmentPath = pathOnServer

        createMessageEntry(groupId, groupMembers, message)
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
        groupMembers: List<ContactModel>,
        videosDirectoryRef: File,
        videoInfo: VideoInfo,
        uri: Uri,
        message: GroupMessage
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

        val thumbnailPathOnServer = if (message.thumbnailBitmap != null) {
            val imageInBytes = ImageUtils.convertToByteArray(message.thumbnailBitmap!!)
            uploadChatAttachment("thumb-$newFileName", imageInBytes)
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(newFileName, compressedFileUri)

        message.attachmentPath = pathOnServer
        message.thumbnail = thumbnailPathOnServer

        createMessageEntry(groupId, groupMembers, message)
        updateMediaInfoInGroupMedia(
            groupId,
            ChatConstants.ATTACHMENT_TYPE_VIDEO,
            message.id,
            videoInfo.name,
            pathOnServer,
            thumbnailPathOnServer,
            message.videoAttachmentLength
        )
    }

    suspend fun sendNewDocumentMessage(
        groupId: String,
        groupMembers: List<ContactModel>,
        message: GroupMessage,
        fileName: String,
        uri: Uri
    ) {
        val pathOnServer = uploadChatAttachment(fileName, uri)
        message.attachmentPath = pathOnServer

        createMessageEntry(groupId, groupMembers, message)
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
        groupMembers: List<ContactModel>,
        message: GroupMessage
    ) {
        val batch = db.batch()


        val messageRef = db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .collection(COLLECTION_GROUP_MESSAGES)
            .document(message.id)
        batch.set(messageRef, message)

        groupMembers.forEach {

            it.uid?.let { uid ->

                val headerRef = db.collection(COLLECTION_CHATS)
                    .document(uid)
                    .collection(COLLECTION_CHAT_HEADERS)
                    .document(groupId)

                batch.update(
                    headerRef, mapOf(
                        "lastMessageType" to message.type,
                        "lastMsgText" to message.content,
                        "lastMsgTimestamp" to message.timestamp,
                        "unseenCount" to FieldValue.increment(1)
                    )
                )
            }
        }

        batch.commitOrThrow()
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

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_GROUP_CHATS = "chat_groups"
        const val COLLECTION_GROUP_MESSAGES = "group_messages"
        const val COLLECTION_CHAT_HEADERS = "headers"
        const val COLLECTION_CHAT_REPORTED_USER = "chat_reported_users"
    }

}