package com.gigforce.modules.feature_chat.screens.vm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.getFileOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.*
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.repositories.ChatGroupRepository
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

interface GroupChatViewModelOutputs {

    val groupInfo: LiveData<ChatGroup>

    val messages: LiveData<List<ChatMessage>>
}

interface GroupChatViewModelInputs {

    fun setGroupId(groupId: String)

    fun getGroupInfoAndStartListeningToMessages()
}


class GroupChatViewModel constructor(
        private val chatContactsRepository: ChatContactsRepository,
        private val chatGroupRepository: ChatGroupRepository = ChatGroupRepository(),
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val chatLocalDirectoryReferenceManager: ChatLocalDirectoryReferenceManager = ChatLocalDirectoryReferenceManager(),
        private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository = ChatProfileFirebaseRepository()
) : ViewModel(),
        GroupChatViewModelInputs,
        GroupChatViewModelOutputs {

    val outputs: GroupChatViewModelOutputs = this
    val inputs: GroupChatViewModelInputs = this

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private lateinit var groupId: String

    private var groupMessagesListener: ListenerRegistration? = null
    private var groupDetailsListener: ListenerRegistration? = null
    private var groupContactsListener: ListenerRegistration? = null
    private var userGroupHeaderChangeListener: ListenerRegistration? = null

    private var groupDetails: ChatGroup? = null
    private var userContacts: List<ContactModel>? = null
    private var grpMessages: MutableList<ChatMessage>? = null

    //Create group
    override fun setGroupId(groupId: String) {
        this.groupId = groupId
    }

    private val _createGroup: MutableLiveData<Lce<String>> = MutableLiveData()
    val createGroup: LiveData<Lce<String>> = _createGroup

    fun createGroup(
            groupName: String,
            groupMembers: List<ContactModel>
    ) = viewModelScope.launch {
        _createGroup.value = Lce.loading()

        try {
            val groupId = chatGroupRepository.createGroup(groupName, groupMembers)
            _createGroup.value = Lce.content(groupId)
            _createGroup.value = null
        } catch (e: Exception) {
            _createGroup.value = Lce.error(e.toString())
            _createGroup.value = null
        }
    }

    //Add Users to group
    private val _addUsersGroup: MutableLiveData<Lse> = MutableLiveData()
    val addUsersGroup: LiveData<Lse> = _addUsersGroup

    fun addUsersGroup(
            groupMembers: List<ContactModel>
    ) = GlobalScope.launch {
        _addUsersGroup.postValue(Lse.loading())

        try {
            chatGroupRepository.addUserToGroup(groupId, groupMembers)
            _addUsersGroup.postValue(Lse.success())
            _addUsersGroup.postValue(null)
        } catch (e: Exception) {
            _addUsersGroup.postValue(Lse.error(e.toString()))
            _addUsersGroup.postValue(null)
        }
    }

    // Listening to group messages

    /**
     * -----------------------------------
     * Getting group details
     * -----------------------------------
     */

    private val _groupInfo: MutableLiveData<ChatGroup> = MutableLiveData()
    override val groupInfo: LiveData<ChatGroup> = _groupInfo

    fun startWatchingGroupDetails() {
        groupDetailsListener = chatGroupRepository.getGroupDetailsRef(groupId)
                .addSnapshotListener { data, error ->

                    error?.let {
                        CrashlyticsLogger.e(TAG, "In startWatchingGroupDetails()", it)
                    }

                    if (data != null) {
                        groupDetails = data.toObject(ChatGroup::class.java)!!.apply {
                            this.id = data.id
                        }

                        if (userContacts != null) {
                            compareGroupMembersWithContactsAndEmit()
                        }
                    }
                }

        startContactsChangeListener()
    }

    /**
     * --------------------------
     * Listeneing for Contact Details Changes
     * -----------------------------
     */

    private fun startContactsChangeListener() {
        if (groupContactsListener != null)
            return

        groupContactsListener = chatContactsRepository.getUserContacts()
                .addSnapshotListener { snap, error ->
                    error?.let {
                        CrashlyticsLogger.e(TAG, "In addContactsChangeListener()", it)
                    }

                    snap?.let {
                        userContacts = it.documents.map {
                            it.toObject(ContactModel::class.java)!!.apply {
                                this.id = it.id
                            }
                        }

                        if (groupDetails != null)
                            compareGroupMembersWithContactsAndEmit()

                        if (grpMessages != null)
                            compareGroupMessagesWithContactsAndEmit()
                    }
                }
    }


    /**
     * -------------------------------------------
     * Listening For group Messages
     * -------------------------------------------
     */

    private val _groupMessages: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    override val messages: LiveData<List<ChatMessage>> = _groupMessages


    override fun getGroupInfoAndStartListeningToMessages() {
        startWatchingGroupDetails()
        startWatchingGroupMessages()
        startWatchingUserGroupHeader()
        startContactsChangeListener()
    }

    private fun startWatchingUserGroupHeader() {
        userGroupHeaderChangeListener = chatGroupRepository
                .userGroupHeaderRef(groupId)
                .addSnapshotListener { value, error ->

                    error?.let {
                        CrashlyticsLogger.e(
                                TAG,
                                "Listening to user chat header",
                                it
                        )
                    }

                    val unseenMessageCount = value?.get(ChatHeader.KEY_UNSEEN_MESSAGE_COUNT) ?: 0
                    if (unseenMessageCount != 0)
                        setMessagesUnseenCountToZero()

                }
    }

    private fun startWatchingGroupMessages() {
        groupMessagesListener = chatGroupRepository
                .groupMessagesRef(groupId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->

                    if (error != null)
                        Log.e(TAG, "Error while listening group messages", error)

                    grpMessages = value?.documents?.map { doc ->
                        doc.toObject(ChatMessage::class.java)!!.apply {
                            id = doc.id
                            this.chatType = ChatConstants.CHAT_TYPE_GROUP
                        }
                    }?.toMutableList()

                    if (userContacts != null) {
                        compareGroupMessagesWithContactsAndEmit()
                    }
                }
    }


    private fun compareGroupMembersWithContactsAndEmit() {
        groupDetails!!.groupMembers.forEach { groupMember ->

            val matchInContact = userContacts!!.find { groupMember.uid == it.uid }

            if (matchInContact != null) {
                groupMember.name = matchInContact.name
            } else if (currentUser.phoneNumber!!.contains(groupMember.mobile)) {
                groupMember.name = "You"
            } else {
                groupMember.name = ""
            }
        }
        _groupInfo.value = groupDetails!!
    }

    private fun compareGroupMessagesWithContactsAndEmit() {
        grpMessages!!.forEach { groupMessage ->

            val matchInContact = userContacts!!.find { groupMessage.senderInfo.id == it.uid }

            if (matchInContact != null) {
                groupMessage.senderInfo.name = if (matchInContact.name.isNullOrBlank()) {
                    matchInContact.mobile
                } else {
                    matchInContact.name ?: ""
                }
            }
        }

        _groupMessages.postValue(grpMessages)
    }

    private var currentUserSenderInfo: UserInfo? = null
    private suspend fun createCurrentUserSenderInfo(): UserInfo {

        if (currentUserSenderInfo != null)
            return currentUserSenderInfo!!


       val profile =  chatProfileFirebaseRepository.getProfileDataIfExist()!!
        val profilePic =
            if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
                ""
            else {
                "profile_pics/${profile.profileAvatarName}"
            }
        return  UserInfo(id = currentUser.uid, name = profile.name, profilePic = profilePic)
    }

    //---------------------------
    // Sending Group messages
    //--------------------------

//    private var _sendingMessage = MutableLiveData<GroupMessage>()
//    val sendingMessage: LiveData<GroupMessage> = _sendingMessage

    fun sendNewText(
            text: String
    ) = viewModelScope.launch {

        try {
            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = createCurrentUserSenderInfo(),
                    type = ChatConstants.MESSAGE_TYPE_TEXT,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    content = text,
                    timestamp = Timestamp.now()
            )

            val groupMembers = groupDetails?.groupMembers
                    ?: chatGroupRepository.getGroupDetails(groupId).groupMembers
            chatGroupRepository.sendTextMessage(groupId, message)
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
    }

    @SuppressLint("NewApi")
    fun sendNewImageMessage(
            text: String = "",
            uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {

            val thumbnail =
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            ThumbnailUtils.createImageThumbnail(File(uri.path), Size(96, 96), null)
                        } else {
                            ImageUtils.resizeBitmap(uri.path!!, 96, 96)
                        }
                    } catch (e: Exception) {
                        null
                    }

            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = createCurrentUserSenderInfo(),
                    type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    content = text,
                    timestamp = Timestamp.now(),
                    thumbnailBitmap = thumbnail,
                    attachmentPath = null
            )
            grpMessages?.add(message)
            _groupMessages.postValue(grpMessages)

            chatGroupRepository.sendNewImageMessage(
                    groupId = groupId,
                    message = message,
                    imageUri = uri
            )
        } catch (e: Exception) {
            //handle error
        }
    }

    fun sendNewDocumentMessage(
            text: String = "",
            fileName: String,
            uri: Uri
    ) = viewModelScope.launch {

        try {
            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = createCurrentUserSenderInfo(),
                    type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    content = text,
                    attachmentName = fileName,
                    timestamp = Timestamp.now()
            )
            grpMessages?.add(message)
            _groupMessages.postValue(grpMessages)

            chatGroupRepository.sendNewDocumentMessage(
                    groupId,
                    message,
                    fileName,
                    uri
            )
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
    }

    fun sendNewVideoMessage(
            context: Context,
            text: String = "",
            videoInfo: VideoInfo,
            uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            val thumbnailForUi =
                    videoInfo.thumbnail?.copy(videoInfo.thumbnail.config, videoInfo.thumbnail.isMutable)

            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = createCurrentUserSenderInfo(),
                    type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    content = text,
                    attachmentName = videoInfo.name,
                    timestamp = Timestamp.now(),
                    videoLength = videoInfo.duration,
                    thumbnailBitmap = thumbnailForUi
            )

            grpMessages?.add(message)
            _groupMessages.postValue(grpMessages)

            chatGroupRepository.sendNewVideoMessage(
                    context = context.applicationContext,
                    groupId = groupId,
                    videosDirectoryRef = chatLocalDirectoryReferenceManager.videosDirectoryRef,
                    videoInfo = videoInfo,
                    uri = uri,
                    message = message
            )
        } catch (e: Exception) {
            //handle error
        }
    }

    fun sendLocationMessage(
            latitude: Double,
            longitude: Double,
            physicalAddress: String,
            mapImageFile: File?
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {


            val mapImage: Bitmap? = if (mapImageFile != null) {
                BitmapFactory.decodeFile(mapImageFile.absolutePath)
            } else {
                null
            }

            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = UserInfo(
                            id = currentUser.uid
                    ),
                    receiverInfo = null,
                    type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    timestamp = Timestamp.now(),
                    location = GeoPoint(latitude, longitude),
                    locationPhysicalAddress = physicalAddress,
                    thumbnailBitmap = mapImage?.copy(mapImage.config, mapImage.isMutable)
            )

            grpMessages?.add(message)
            _groupMessages.postValue(grpMessages)

            chatGroupRepository.sendLocationMessage(
                    groupId = groupId,
                    message = message,
                    bitmap = mapImage
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private val _changeGroupName: MutableLiveData<Lse> = MutableLiveData()
    val changeGroupName: LiveData<Lse> = _changeGroupName

    fun changeGroupName(newGroupName: String) = viewModelScope.launch {
        chatGroupRepository.changeGroupName(groupId, newGroupName)
    }

    fun setMessagesUnseenCountToZero() = GlobalScope.launch {
        try {
            chatGroupRepository.setUnseenMessagecountToZero(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to set unseen count to zero", e)
        }
    }

    private val _deactivatingGroup: MutableLiveData<Lse> = MutableLiveData()
    val deactivatingGroup: LiveData<Lse> = _deactivatingGroup

    fun deactivateOrActivateGroup() = viewModelScope.launch {
        _deactivatingGroup.value = Lse.loading()

        try {
            chatGroupRepository.deactivateOrActivateGroup(groupId)

            _deactivatingGroup.value = Lse.success()
            _deactivatingGroup.value = null
        } catch (e: Exception) {
            _deactivatingGroup.value = Lse.error(e.message ?: "Unable to deactivate group")
            _deactivatingGroup.value = null
        }
    }

    private val _removingUser: MutableLiveData<Lse> = MutableLiveData()
    val removingUser: LiveData<Lse> = _removingUser

    fun removeUserFromGroup(uid: String) = viewModelScope.launch {
        _removingUser.value = Lse.loading()

        try {
            chatGroupRepository.removeUserFromGroup(groupId, uid)

            _removingUser.value = Lse.success()
            _removingUser.value = null
        } catch (e: Exception) {
            _removingUser.value = Lse.error(e.message ?: "Unable to remove user from group")
            _removingUser.value = null
        }
    }

    private val _chatAttachmentDownloadState: MutableLiveData<ChatAttachmentDownloadState> =
            MutableLiveData()
    val chatAttachmentDownloadState: LiveData<ChatAttachmentDownloadState> =
            _chatAttachmentDownloadState

    fun downloadAndSaveFile(appDirectoryFileRef: File, position: Int, groupMessage: GroupMessage) =
            viewModelScope.launch {
                val downloadLink = groupMessage.attachmentPath ?: return@launch

                if (!appDirectoryFileRef.exists())
                    appDirectoryFileRef.mkdirs()

                _chatAttachmentDownloadState.value = DownloadStarted(position)

                try {

                    val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
                    val fileRef = if (groupMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE) {
                        val imagesDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)

                        if (!imagesDirectoryRef.exists())
                            imagesDirectoryRef.mkdirs()

                        File(imagesDirectoryRef, fileName)
                    } else if (groupMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO) {
                        val videosDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
                        if (!videosDirectoryRef.exists())
                            videosDirectoryRef.mkdirs()

                        File(videosDirectoryRef, fileName)
                    } else if (groupMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT) {
                        val documentsDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)

                        if (!documentsDirectoryRef.exists())
                            documentsDirectoryRef.mkdirs()

                        File(documentsDirectoryRef, fileName)
                    } else {
                        throw IllegalArgumentException("other types not supported yet")
                    }

                    firebaseStorage.getReferenceFromUrl(downloadLink).getFileOrThrow(fileRef)
                    _chatAttachmentDownloadState.value = DownloadCompleted(position)
                    _chatAttachmentDownloadState.value = null

                } catch (e: Exception) {
                    _chatAttachmentDownloadState.value = ErrorWhileDownloadingAttachment(
                            position,
                            e.message ?: "Unable to download attachment"
                    )
                    _chatAttachmentDownloadState.value = null
                }
            }


    fun downloadAndSaveFile(appDirectoryFileRef: File, position: Int, media: GroupMedia) =
            viewModelScope.launch {
                val downloadLink = media.attachmentPath ?: return@launch

                if (!appDirectoryFileRef.exists())
                    appDirectoryFileRef.mkdirs()

                _chatAttachmentDownloadState.value = DownloadStarted(position)

                try {

                    val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
                    val fileRef = if (media.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE) {
                        val imagesDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)

                        if (!imagesDirectoryRef.exists())
                            imagesDirectoryRef.mkdirs()

                        File(imagesDirectoryRef, fileName)
                    } else if (media.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO) {
                        val videosDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
                        if (!videosDirectoryRef.exists())
                            videosDirectoryRef.mkdirs()

                        File(videosDirectoryRef, fileName)
                    } else if (media.attachmentType == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
                        val documentsDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)

                        if (!documentsDirectoryRef.exists())
                            documentsDirectoryRef.mkdirs()

                        File(documentsDirectoryRef, fileName)
                    } else {
                        throw IllegalArgumentException("other types not supported yet")
                    }

                    firebaseStorage.getReferenceFromUrl(downloadLink).getFileOrThrow(fileRef)
                    _chatAttachmentDownloadState.value = DownloadCompleted(position)
                    _chatAttachmentDownloadState.value = null

                } catch (e: Exception) {
                    _chatAttachmentDownloadState.value = ErrorWhileDownloadingAttachment(
                            position,
                            e.message ?: "Unable to download attachment"
                    )
                    _chatAttachmentDownloadState.value = null
                }
            }


    override fun onCleared() {
        super.onCleared()
        groupMessagesListener?.remove()
        groupMessagesListener = null

        groupDetailsListener?.remove()
        groupDetailsListener = null

        groupContactsListener?.remove()
        groupContactsListener = null
    }

    companion object {
        const val TAG: String = "GroupChatVM"
    }


}