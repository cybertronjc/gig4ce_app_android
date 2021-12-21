package com.gigforce.common_ui.chat

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
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.core.StringConstants
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.*
import com.gigforce.core.file.FileUtils
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class ChatGroupRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository
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

    fun groupEventsRef(groupId: String) = db.collection(COLLECTION_GROUP_CHATS)
        .document(groupId)
        .collection(COLLECTION_GROUP_EVENTS)
        .whereArrayContains("showEventToUsersWithUid", getUID())

    fun userGroupHeaderRef(groupId: String) = db.collection(COLLECTION_CHATS)
        .document(getUID())
        .collection(COLLECTION_CHAT_HEADERS)
        .document(groupId)

    suspend fun createGroup(groupName: String, groupAvatar: String?, groupMembers: List<ContactModel>): String {

        val currentUserInfo = createContactModelsForCurrentUser()
        val members = groupMembers.toMutableList().apply {
            add(currentUserInfo)
        }

        members.forEach {
            if (!it.profileName.isNullOrBlank()) {
                it.name = it.profileName
            } else {
                it.name = chatProfileFirebaseRepository.getProfileDataIfExist(it.uid)?.name ?: ""
            }
        }

        val group = createGroupData(groupName, groupAvatar,  members, currentUserInfo)
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
                    groupAvatar = groupAvatar.toString(),
                    lastMsgTimestamp = Timestamp.now(),
                    lastMsgFlowType = ChatConstants.FLOW_TYPE_OUT
                )
            )
        }
        batch.commitOrThrow()
        return group.id
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
        groupAvatar: String?,
        groupMembers: List<ContactModel>,
        currentUserInfo: ContactModel
    ): ChatGroup {

        val chatGroup = ChatGroup(
            name = groupName,
            groupMembers = groupMembers,
            groupAvatar = groupAvatar.toString(),
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

    private fun prepareUniqueImageName(): String {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return getUID() + timeStamp + ".jpg"
    }

    fun getGroupDetailsRef(groupId: String) =
        db.collection(COLLECTION_GROUP_CHATS).document(groupId)

    suspend fun getGroupDetails(groupId: String): ChatGroup {
        val groupSnap = getGroupDetailsRef(groupId).getOrThrow()
        return groupSnap.toObject(ChatGroup::class.java)!!.apply {
            id = groupId
            setUpdatedAtAndBy(getUID())
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

        val thumbnail = message.thumbnailBitmap?.copy(message.thumbnailBitmap?.config, true)
        val thumbnailPathOnServer = if (thumbnail != null) {
            val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
            uploadChatAttachment(
                "thumb-${message.imageMetaData?.name}",
                imageInBytes,
                groupId,
                isGroupChatMessage = true,
                messageType = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE
            )
        } else {
            null
        }

        val pathOnServer = uploadChatAttachment(
            message.imageMetaData!!.name,
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
                mapOf(
                    "groupMedia" to FieldValue.arrayUnion(
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
                    ), "updatedAt" to Timestamp.now(), "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )
    }

    suspend fun changeGroupName(groupId: String, newGroupName: String) {

        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .updateOrThrow(
                mapOf(
                    "name" to newGroupName,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )

        val groupDetails = getGroupDetails(groupId)

        val batch = db.batch()
        groupDetails.groupMembers.forEach {

            val headerRef = db.collection(COLLECTION_CHATS)
                .document(it.uid!!)
                .collection(COLLECTION_CHAT_HEADERS)
                .document(groupId)

            batch.update(
                headerRef,
                mapOf(
                    "groupName" to newGroupName,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to it.uid!!
                )
            )
        }
        batch.commitOrThrow()
    }

    suspend fun setUnseenMessagecountToZero(groupHeaderId: String) {
        db.collection("chats")
            .document(getUID())
            .collection("headers")
            .document(groupHeaderId)
            .updateOrThrow(
                mapOf(
                    "unseenCount" to 0,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to getUID()
                )
            )
    }

    suspend fun deactivateOrActivateGroup(groupHeaderId: String) {
        val groupDetails = getGroupDetails(groupHeaderId)

        val batch = db.batch()
        val groupRef = db.collection(COLLECTION_GROUP_CHATS)
            .document(groupHeaderId)
        batch.update(
            groupRef,
            mapOf(
                "groupDeactivated" to !groupDetails.groupDeactivated,
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        )

        groupDetails.groupMembers.forEach {
            val headerRef = db.collection("chats")
                .document(it.uid!!)
                .collection("headers")
                .document(groupHeaderId)

            batch.update(
                headerRef, mapOf(
                    "groupDeactivated" to !groupDetails.groupDeactivated,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to it.uid!!
                )
            )
        }

        batch.commit()
    }

    suspend fun addUserToGroup(groupId: String, members: List<ContactModel>) {

        val groupInfo = getGroupDetails(groupId)
        val grpMembers = groupInfo.groupMembers.toMutableList()
        val deletedGrpMembers = groupInfo.deletedGroupMembers.toMutableList()

        //Creating Headers in each users doc
        val batch = db.batch()

        val filteredMemList = members.filter {
            grpMembers.find { grpMem -> grpMem.uid == it.uid } == null
        }

        filteredMemList.forEach {
            it.name = chatProfileFirebaseRepository.getProfileDataIfExist(it.uid)?.name ?: ""
        }

        grpMembers.addAll(filteredMemList)
        grpMembers.onEach {
            it.deletedOn = null
        }
        groupInfo.groupMembers = grpMembers

        val updatedDeletedMemebers = deletedGrpMembers.filter {
            members.find { member -> it.uid == member.uid } == null
        }
        groupInfo.deletedGroupMembers = updatedDeletedMemebers

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

    suspend fun removeUserFromGroup(groupHeaderId: String, userUid: String) {
        val groupDetails = getGroupDetails(groupHeaderId)
        val updatedUserList = groupDetails.groupMembers.filter {
            it.uid != userUid
        }
        val deletedUser = groupDetails.groupMembers.filter {
            it.uid == userUid
        }.onEach {
            it.deletedOn = Timestamp.now()
        }

        groupDetails.groupMembers = updatedUserList
        groupDetails.deletedGroupMembers = groupDetails.deletedGroupMembers.plus(deletedUser)

        val batch = db.batch()
        val groupRef = db.collection(COLLECTION_GROUP_CHATS)
            .document(groupHeaderId)
        batch.set(groupRef, groupDetails)

        val userHeaderRef = db.collection(COLLECTION_CHATS)
            .document(userUid)
            .collection(COLLECTION_CHAT_HEADERS)
            .document(groupHeaderId)
        batch.update(
            userHeaderRef, mapOf(
                "removedFromGroup" to true,
                "updatedAt" to Timestamp.now(),
                "updatedBy" to userUid
            )
        )
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

    suspend fun deleteMessage(
        groupId: String,
        messageId: String
    ) = db.collection(COLLECTION_GROUP_CHATS)
        .document(groupId)
        .collection(COLLECTION_GROUP_MESSAGES)
        .document(messageId)
        .updateOrThrow(
            mapOf(
                "isDeleted" to true,
                "deletedOn" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid
            )
        )


    suspend fun makeUserGroupAdmin(
        groupId: String,
        uid: String
    ) {
        val groupDetails = getGroupDetails(groupId)

        groupDetails.groupMembers.find { contact ->
            contact.uid == uid
        }?.isUserGroupManager = true

        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .updateOrThrow(
                mapOf(
                    "groupMembers" to groupDetails.groupMembers,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )

        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .collection(COLLECTION_GROUP_EVENTS)
            .addOrThrow(
                EventInfo(
                    groupId = groupId,
                    showEventToUsersWithUid = arrayListOf(uid),
                    eventDoneByUserUid = currentUser.uid,
                    eventText = "You're now an admin"
                )
            )
    }

    suspend fun dismissUserAsGroupAdmin(
        groupId: String,
        uid: String
    ) {
        val groupDetails = getGroupDetails(groupId)

        groupDetails.groupMembers.find { contact ->
            contact.uid == uid
        }?.isUserGroupManager = false

        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .updateOrThrow(
                mapOf(
                    "groupMembers" to groupDetails.groupMembers,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )

        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .collection(COLLECTION_GROUP_EVENTS)
            .addOrThrow(
                EventInfo(
                    groupId = groupId,
                    showEventToUsersWithUid = arrayListOf(uid),
                    eventDoneByUserUid = currentUser.uid,
                    eventText = "You've been dismissed as admin"
                )
            )
    }

    suspend fun allowEveryoneToPostInThisGroup(
        groupId: String
    ) {
        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .updateOrThrow(
                mapOf(
                    "onlyAdminCanPostInGroup" to false,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )
    }

    suspend fun limitPostingToAdminsInGroup(
        groupId: String
    ) {
        db.collection(COLLECTION_GROUP_CHATS)
            .document(groupId)
            .updateOrThrow(
                mapOf(
                    "onlyAdminCanPostInGroup" to true,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )
    }

    private var currentBatchSize = 0
    private val mutex = Mutex()
    private var batch = db.batch()

    suspend fun markMessagesAsRead(
        groupId: String,
        messageWithNotReceivedStatus: List<ChatMessage>
    ) = mutex.withLock {
        val groupMessagesCollectionRef = groupMessagesRef(groupId)

        val currentUserModel = createContactModelsForCurrentUser()
        val receivingObject = MessageReceivingInfo(
            uid = currentUser.uid,
            profileName = currentUserModel.name ?: "",
            profilePicture = currentUserModel.getUserProfileImageUrlOrPath() ?: ""
        )

        messageWithNotReceivedStatus.forEach {
            val messageRef = groupMessagesCollectionRef.document(it.id)
            batch.update(
                messageRef,
                mapOf(
                    "groupMessageReadBy" to FieldValue.arrayUnion(receivingObject),
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid
                )
            )
            checkBatchForOverFlowAndCommit()
        }

        if (currentBatchSize != 0) {
            batch.commitOrThrow()
        }
    }

    private suspend fun checkBatchForOverFlowAndCommit() {
        currentBatchSize++
        Log.d(TAG, "Size updated to $currentBatchSize")

        if (currentBatchSize > 480) {
            //   batchArray.add(batch)
            batch.commitOrThrow()
            currentBatchSize = 0

            batch = db.batch()
            Log.d(TAG, "New Batch $batch")
        }
    }

    suspend fun getChatMessage(
        groupId: String,
        messageId: String
    ) = groupMessagesRef(groupId)
        .document(messageId)
        .getOrThrow()
        .toObject(ChatMessage::class.java)


    companion object {
        const val TAG = "ChatGrouprepository"

        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_GROUP_CHATS = "chat_groups"
        const val COLLECTION_GROUP_MESSAGES = "group_messages"
        const val COLLECTION_CHAT_HEADERS = "headers"
        const val COLLECTION_CHAT_REPORTED_USER = "chat_reported_users"
        const val COLLECTION_GROUP_EVENTS = "group_events"
    }

}