package com.gigforce.app.modules.chatmodule.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.chatmodule.*
import com.gigforce.app.modules.chatmodule.models.*
import com.gigforce.app.modules.chatmodule.repository.ChatContactsRepository
import com.gigforce.app.modules.chatmodule.repository.ChatGroupRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.wallet.remote.GeneratePaySlipService
import com.gigforce.app.utils.*
import com.gigforce.app.utils.network.RetrofitFactory
import com.gigforce.core.utils.EventLogs.getDownloadUrlOrThrow
import com.gigforce.core.utils.FileUtils
import com.gigforce.core.utils.FirebaseUtils
import com.gigforce.core.utils.ImageUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class GroupChatViewModel constructor(
    private val chatContactsRepository: ChatContactsRepository,
    private val chatGroupRepository: ChatGroupRepository = ChatGroupRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val downloadAttachmentService: GeneratePaySlipService = RetrofitFactory.generatePaySlipService()
) : ViewModel() {

    private val uid by lazy { FirebaseAuth.getInstance().uid!! }
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private lateinit var groupId: String

    private var groupMessagesListener: ListenerRegistration? = null
    private var groupDetailsListener: ListenerRegistration? = null
    private var groupContactsListener: ListenerRegistration? = null

    //Create group
    fun setGroupId(groupId: String) {
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

    private val _chatGroupDetails: MutableLiveData<Lce<ChatGroup>> = MutableLiveData()
    val chatGroupDetails: LiveData<Lce<ChatGroup>> = _chatGroupDetails

    private var groupDetails: ChatGroup? = null
    private var userContacts: List<ContactModel>? = null
    private var grpMessages: List<GroupMessage>? = null

    fun startWatchingGroupDetails() {
        _chatGroupDetails.value = Lce.loading()
        groupDetailsListener = chatGroupRepository.getGroupDetailsRef(groupId)
            .addSnapshotListener { data, error ->

                if (error != null) {
                    _chatGroupDetails.value = Lce.error(error.toString())
                } else if (data != null) {
                    groupDetails = data.toObject(ChatGroup::class.java)!!.apply {
                        this.id = data.id
                    }

                    if (userContacts != null) {
                        compareGroupMembersWithContactsAndEmit()
                    }
                }
            }

        addContactsChangeListener()
    }

    private fun addContactsChangeListener() {
        if (groupContactsListener != null)
            return

        groupContactsListener = chatContactsRepository.getUserContacts()
            .addSnapshotListener { snap, error ->
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

    private val _groupMessages: MutableLiveData<List<GroupMessage>> = MutableLiveData()
    val groupMessages: LiveData<List<GroupMessage>> = _groupMessages

    fun startListeningForGroupMessages() {
        groupMessagesListener = chatGroupRepository
            .groupMessagesRef(groupId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->

                if (error != null)
                    Log.e(TAG, "Error while listening group messages", error)

                grpMessages = value?.documents?.map { doc ->
                    doc.toObject(GroupMessage::class.java)!!.apply {
                        id = doc.id
                    }
                }

                if (userContacts != null) {
                    compareGroupMessagesWithContactsAndEmit()
                }
            }

        addContactsChangeListener()
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
        _chatGroupDetails.value = Lce.content(groupDetails!!)
    }

    private fun compareGroupMessagesWithContactsAndEmit() {
        grpMessages!!.forEach { groupMessage ->

            val matchInContact = userContacts!!.find { groupMessage.senderInfo!!.id == it.uid }

            if (matchInContact != null) {
                groupMessage.senderInfo!!.name = if (matchInContact.name.isNullOrBlank()) {
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

        val profile = profileFirebaseRepository.getProfileData()
        val profilePic =
            if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
                null
            else {
                firebaseStorage
                    .reference
                    .child("profile_pics")
                    .child(profile.profileAvatarName)
                    .getDownloadUrlOrThrow().toString()
            }
        currentUserSenderInfo =
            UserInfo(id = uid, name = profile.name, profilePic = profilePic ?: "")
        return currentUserSenderInfo!!
    }

    //---------------------------
    // Sending Group messages
    //--------------------------

    private var _sendingMessage = MutableLiveData<GroupChatMessage>()
    val sendingMessage: LiveData<GroupChatMessage> = _sendingMessage

    fun sendNewText(
        text: String
    ) = viewModelScope.launch {

        try {
            val message = GroupMessage(
                id = UUID.randomUUID().toString(),
                groupHeaderId = groupId,
                senderInfo = createCurrentUserSenderInfo(),
                type = Message.MESSAGE_TYPE_TEXT,
                content = text,
                timestamp = Timestamp.now()
            )

            val groupMembers = groupDetails?.groupMembers ?: chatGroupRepository.getGroupDetails(groupId).groupMembers
            chatGroupRepository.sendTextMessage(groupId,groupMembers, message)
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

            val message = GroupMessage(
                id = UUID.randomUUID().toString(),
                groupHeaderId = groupId,
                senderInfo = createCurrentUserSenderInfo(),
                type = Message.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                content = text,
                timestamp = Timestamp.now(),
                thumbnailBitmap = thumbnail,
                attachmentPath = null
            )
            _sendingMessage.postValue(GroupChatMessage.fromMessage(message))

            val groupMembers = groupDetails?.groupMembers ?: chatGroupRepository.getGroupDetails(groupId).groupMembers
            chatGroupRepository.sendNewImageMessage(
                groupId = groupId,
                message = message,
                imageUri = uri,
                groupMembers = groupMembers
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
            val message = GroupMessage(
                id = UUID.randomUUID().toString(),
                groupHeaderId = groupId,
                senderInfo = createCurrentUserSenderInfo(),
                type = Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT,
                content = text,
                attachmentName = fileName,
                timestamp = Timestamp.now()
            )

            val groupMembers = groupDetails?.groupMembers ?: chatGroupRepository.getGroupDetails(groupId).groupMembers
            chatGroupRepository.sendNewDocumentMessage(groupId, groupMembers,message, fileName, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
    }

    fun sendNewVideoMessage(
        context: Context,
        text: String = "",
        videosDirectoryRef: File,
        videoInfo: VideoInfo,
        uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            val thumbnailForUi = videoInfo.thumbnail?.copy(videoInfo.thumbnail.config,videoInfo.thumbnail.isMutable)

            val message = GroupMessage(
                id = UUID.randomUUID().toString(),
                groupHeaderId = groupId,
                senderInfo = createCurrentUserSenderInfo(),
                type = Message.MESSAGE_TYPE_TEXT_WITH_VIDEO,
                content = text,
                attachmentName = videoInfo.name,
                timestamp = Timestamp.now(),
                videoAttachmentLength = videoInfo.duration,
                thumbnailBitmap = thumbnailForUi
            )
            _sendingMessage.postValue(GroupChatMessage.fromMessage(message))

            val groupMembers = groupDetails?.groupMembers ?: chatGroupRepository.getGroupDetails(groupId).groupMembers
            chatGroupRepository.sendNewVideoMessage(
                context = context.applicationContext,
                groupId =  groupId,
                videosDirectoryRef = videosDirectoryRef,
                videoInfo = videoInfo,
                uri = uri,
                message = message,
                groupMembers = groupMembers
            )
        } catch (e: Exception) {
            //handle error
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

                val response = downloadAttachmentService.downloadPaySlip(downloadLink)

                if (response.isSuccessful) {
                    val body = response.body()!!
                    FileUtils.writeResponseBodyToDisk(body, fileRef)
                    _chatAttachmentDownloadState.value = DownloadCompleted(position)
                    _chatAttachmentDownloadState.value = null
                } else {
                    throw Exception("Unable to download attachment, ${response.message()}")
                }
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

                val response = downloadAttachmentService.downloadPaySlip(downloadLink)

                if (response.isSuccessful) {
                    val body = response.body()!!
                    FileUtils.writeResponseBodyToDisk(body, fileRef)
                    _chatAttachmentDownloadState.value = DownloadCompleted(position)
                    _chatAttachmentDownloadState.value = null
                } else {
                    throw Exception("Unable to dowload attachment, ${response.message()}")
                }
            } catch (e: Exception) {
                _chatAttachmentDownloadState.value = ErrorWhileDownloadingAttachment(
                    position,
                    e.message ?: "Unable to download attachment"
                )
                _chatAttachmentDownloadState.value = null
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