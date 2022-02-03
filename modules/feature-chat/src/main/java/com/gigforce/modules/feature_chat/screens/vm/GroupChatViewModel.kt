package com.gigforce.modules.feature_chat.screens.vm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.ChatGroupRepository
import com.gigforce.common_ui.chat.models.*
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.getFileOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.models.GroupChatMember
import com.gigforce.modules.feature_chat.models.MessageReceivingAndReadingInfo
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

interface GroupChatViewModelOutputs {

    val groupInfo: LiveData<ChatGroup>

    val messages: LiveData<List<ChatMessage>>
}

interface GroupChatViewModelInputs {

    fun setGroupId(groupId: String)

    fun getGroupInfoAndStartListeningToMessages()
}


@HiltViewModel
class GroupChatViewModel @Inject constructor(
        private val chatContactsRepository: ChatContactsRepository,
        private val chatGroupRepository: ChatGroupRepository,
        private val firebaseStorage: FirebaseStorage,
        private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository,
        private val chatFileManager : ChatFileManager
) : ViewModel(),
        GroupChatViewModelInputs,
        GroupChatViewModelOutputs {

    val outputs: GroupChatViewModelOutputs = this
    val inputs: GroupChatViewModelInputs = this

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private lateinit var groupId: String

    private var groupEventsListener: ListenerRegistration? = null
    private var groupMessagesListener: ListenerRegistration? = null
    private var groupDetailsListener: ListenerRegistration? = null
    private var groupContactsListener: ListenerRegistration? = null
    private var userGroupHeaderChangeListener: ListenerRegistration? = null

    private var groupDetails: ChatGroup? = null
    private var userContacts: List<ContactModel>? = null
    private var grpMessages: MutableList<ChatMessage>? = null
    private var grpEvents: MutableList<EventInfo>? = null

    private var _chatHeaderInfo = MutableLiveData<ChatHeader?>()
    val chatHeaderInfo: LiveData<ChatHeader?> = _chatHeaderInfo

    private var groupMessagesShownOnView: MutableList<ChatMessage>? = null


    private var _selectedChatMessage = MutableLiveData<List<ChatMessage>>()
    val selectedChatMessage: LiveData<List<ChatMessage>> = _selectedChatMessage

    private var selectedMessagesList = arrayListOf<ChatMessage>()

    private var selectEnable: Boolean? = null

    private var _enableSelect = MutableLiveData<Boolean>()
    val enableSelect: LiveData<Boolean> = _enableSelect

    private var _scrollToMessageId = MutableLiveData<String?>()
    val scrollToMessageId: LiveData<String?> = _scrollToMessageId

    private var _recentLocationMessageId = MutableLiveData<Pair<String, String>>()
    val recentLocationMessageId: LiveData<Pair<String, String>> = _recentLocationMessageId

    //Create group
    override fun setGroupId(groupId: String) {
        this.groupId = groupId
        Log.d(TAG, "Group id set $groupId")
    }

    private val _createGroup: MutableLiveData<Lce<String>> = MutableLiveData()
    val createGroup: LiveData<Lce<String>> = _createGroup

    private var _scrollToMessage = MutableLiveData<Int?>()
    val scrollToMessage: LiveData<Int?> = _scrollToMessage

    fun createGroup(
            groupName: String,
            groupAvatar: String?,
            groupMembers: List<ContactModel>
    ) = viewModelScope.launch {
        _createGroup.value = Lce.loading()
            try {
                val groupId = chatGroupRepository.createGroup(groupName,groupAvatar, groupMembers)
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

        if (groupDetailsListener != null) {
            Log.d(TAG, "already a listener attached,no-op")
            return
        }

        try {
            val groupDetailRef = chatGroupRepository.getGroupDetailsRef(groupId)
            groupDetailsListener = groupDetailRef
                .addSnapshotListener { data, error ->
                    Log.d(TAG, "group details changed/subscribed, groupId - $groupId")

                    error?.let {
                        CrashlyticsLogger.e(TAG, "In startWatchingGroupDetails()", it)
                    }

                    if (data != null) {
                        groupDetails = data.toObject(ChatGroup::class.java)?.apply {
                            this.id = data.id
                        }

                        if (userContacts != null) {
                            compareGroupMembersWithContactsAndEmit()
                        }

                        val isUserDeletedFromgroup =
                            groupDetails?.deletedGroupMembers?.find { it.uid == currentUser.uid } != null
                        val limitToTimeStamp = if (isUserDeletedFromgroup) {
                            groupDetails?.deletedGroupMembers?.find { it.uid == currentUser.uid }?.deletedOn
                        } else
                            null

                        startWatchingGroupMessagesAndEvents(limitToTimeStamp)
                    }
                }
        } catch (e: Exception){
            e.printStackTrace()
        }

        startContactsChangeListener()
    }

    fun stopWatchingGroupDetails(){
        if (groupDetailsListener != null){
            Log.d(TAG, "stopping snapshot listener")
            groupDetailsListener!!.remove()
        }
    }

    /**
     * --------------------------
     * Listeneing for Contact Details Changes
     * -----------------------------
     */

    private fun startContactsChangeListener() {
        if (groupContactsListener != null)
            return

        groupContactsListener = chatContactsRepository.getUserGigforceContacts()
                .addSnapshotListener { snap, error ->
                    Log.d(TAG, "user contacts contacts data changed/subscribed")

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
        //startWatchingGroupMessages()
        startWatchingUserGroupHeader()
        startContactsChangeListener()
    }

    private fun startWatchingUserGroupHeader() {
        if (userGroupHeaderChangeListener != null) {
            Log.d(TAG, "already a listener attached,no-op")
            return
        }

        userGroupHeaderChangeListener = chatGroupRepository
                .userGroupHeaderRef(groupId)
                .addSnapshotListener { value, error ->
                    Log.d(TAG, "group header data changed/subscribed, group-id : $groupId")

                    error?.let {
                        CrashlyticsLogger.e(
                                TAG,
                                "Listening to user chat header",
                                it
                        )
                    }

                    val unseenMessageCount = value?.get(ChatHeader.KEY_UNSEEN_MESSAGE_COUNT) ?: 0L
                    if (unseenMessageCount != 0L)
                        setMessagesUnseenCountToZero()

                }

        Log.d(TAG, "userGroupHeaderChangeListener attached")
    }

    private fun startWatchingGroupMessagesAndEvents(
            limitToTimeStamp: Timestamp? = null
    ) {
        if (groupMessagesListener != null && limitToTimeStamp != null) {
            Log.d(TAG, "already a listener attached,user removed from group")
            groupMessagesListener?.remove()
            groupMessagesListener = null

            groupEventsListener?.remove()
            groupEventsListener = null
            return
        }

        if (groupMessagesListener != null) {
            Log.d(TAG, "already a listener attached,no-op")
            return
        }

        startListeningToGroupMessages(limitToTimeStamp)
        startListeningToGroupEvents(limitToTimeStamp)
    }

    private fun startListeningToGroupMessages(limitToTimeStamp: Timestamp?) {
        var getGroupMessagesQuery = chatGroupRepository
                .groupMessagesRef(groupId)
                .orderBy("timestamp", Query.Direction.ASCENDING)

        if (limitToTimeStamp != null) {
            getGroupMessagesQuery = getGroupMessagesQuery.whereLessThan("timestamp", limitToTimeStamp)
        }

        groupMessagesListener = getGroupMessagesQuery
                .addSnapshotListener { value, error ->
                    Log.d(TAG, "group messages changed/subscribed, groupId - $groupId")

                    if (error != null)
                        Log.e(TAG, "Error while listening group messages", error)
                    if (value?.isEmpty == true){
                        _groupMessages.postValue(emptyList())
                    }

                    grpMessages = value?.documents?.map { doc ->
                        doc.toObject(ChatMessage::class.java)!!.also {
                            it.id = doc.id
                            it.chatType = ChatConstants.CHAT_TYPE_GROUP
                            it.groupId = groupId
                        }
                    }?.toMutableList()
                        //checkForRecevinginfoElseMarkMessageAsReceived(grpMessages!!)

                    if (userContacts != null) {
                        compareGroupMessagesWithContactsAndEmit()
                    }
                }
    }

    private fun startListeningToGroupEvents(limitToTimeStamp: Timestamp?) {
        var getGroupEventsQuery = chatGroupRepository
                .groupEventsRef(groupId)
                .orderBy("eventTime", Query.Direction.ASCENDING)

        if (limitToTimeStamp != null) {
            getGroupEventsQuery = getGroupEventsQuery.whereLessThan("eventTime", limitToTimeStamp)
        }

        groupEventsListener = getGroupEventsQuery
                .addSnapshotListener { value, error ->
                    Log.d(TAG, "group events changed/subscribed, groupId - $groupId")

                    if (error != null) {
                        Log.e(TAG, "Error while listening group messages", error)

                    }

                    if (value != null) {
                        grpEvents = value.documents.map { doc ->
                            doc.toObject(EventInfo::class.java)!!
                        }.toMutableList()

                        compareGroupMessagesWithContactsAndEmit()

                    }
                }
    }

    fun checkForRecevinginfoElseMarkMessageAsReceived(

    ) = viewModelScope.launch {

        val messageWithNotDeliveredStatus = arrayListOf<String>()
        val messageWithNotReceivedStatus = arrayListOf<String>()

        grpMessages?.forEach { it1 ->
            val chatMessageDeliveredTo =
                chatGroupRepository.getMessageDeliveredInfo(groupId, it1.id)
                    ?: throw IllegalStateException("no chat message found, for group id $groupId message: ${it1.id}")
            val chatMessageReceivedBy = chatGroupRepository.getMessageReceivedInfo(groupId, it1.id)
                ?: throw IllegalStateException("no chat message found, for group id $groupId message: ${it1.id}")
            if (chatMessageDeliveredTo.isEmpty()) {
                messageWithNotDeliveredStatus.add(it1.id)
            }
            if (chatMessageReceivedBy.isEmpty()) {
                messageWithNotReceivedStatus.add(it1.id)
            }

            if (messageWithNotReceivedStatus.isNotEmpty()) {
                try {
                    chatGroupRepository.markAsReadMessages(
                        groupId,
                        messageWithNotReceivedStatus
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            if (messageWithNotDeliveredStatus.isNotEmpty()) {
                try {
                    chatGroupRepository.markAsDelivered(
                        groupId,
                        messageWithNotDeliveredStatus
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    suspend fun getContactStoredByMobile(
        otherUserUID: String
    ) : String {

//        try {
        val contactModel = chatGroupRepository.getDetailsOfUserFromContacts(otherUserUID)
        Log.d("ChatPageViewModel", "catchingH: ${contactModel.uid} , ${contactModel.name}")
        return contactModel.name.toString()
//        } catch (e: Exception) {
//            Log.d("ChatPageViewModel", "catching: ${e.message}")
//            otherUserName = ""
//        }
        //return  otherUserName
    }


    private fun compareGroupMembersWithContactsAndEmit() = viewModelScope.launch {
        groupDetails!!.groupMembers.forEach { groupMember ->

            val matchInContact = userContacts!!.find { groupMember.uid == it.uid }

            if (matchInContact != null) {
                groupMember.name = matchInContact.name
            } else if (currentUser.phoneNumber!!.contains(groupMember.mobile)) {
                groupMember.name = "You"
            } else if (groupMember.name == null) {
                groupMember.name = ""
            }
        }

        groupDetails!!.currenUserRemovedFromGroup = groupDetails!!.groupMembers.find {
            it.uid == currentUser.uid
        } == null

        _groupInfo.value = groupDetails!!
    }

    private fun compareGroupMessagesWithContactsAndEmit() {
        if (grpMessages == null ||
                grpEvents == null ||
                userContacts == null
        ) {
            return
        }

        val groupEvents = grpEvents!!.map { it.toChatMessage() }
        groupMessagesShownOnView = (grpMessages!! + groupEvents).toMutableList()

        groupMessagesShownOnView!!.onEach { groupMessage ->

            val matchInContact = userContacts!!.find { groupMessage.senderInfo.id == it.uid }

            if (matchInContact != null) {
                groupMessage.senderInfo.name = if (matchInContact.name.isNullOrBlank()) {
                    matchInContact.mobile
                } else {
                    matchInContact.name ?: ""
                }
            }

            if(groupMessage.isAReplyToOtherMessage && groupMessage.replyForMessageId != null){
                groupMessage.replyForMessage = grpMessages!!.find { it.id == groupMessage.replyForMessageId }
            }

        }.sortBy {
            it.timestamp!!.seconds
        }

        _groupMessages.postValue(groupMessagesShownOnView)
    }

    private var currentUserSenderInfo: UserInfo? = null
    private suspend fun createCurrentUserSenderInfo(): UserInfo {

        if (currentUserSenderInfo != null)
            return currentUserSenderInfo!!


        val profile = chatProfileFirebaseRepository.getProfileDataIfExist()!!
        val profilePic =
                if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
                    ""
                else {
                    "profile_pics/${profile.profileAvatarName}"
                }
        return UserInfo(
                id = currentUser.uid,
                name = profile.name,
                profilePic = profilePic
        )
    }

    //---------------------------
    // Sending Group messages
    //--------------------------

//    private var _sendingMessage = MutableLiveData<GroupMessage>()
//    val sendingMessage: LiveData<GroupMessage> = _sendingMessage

    fun sendNewText(
            text: String,
            mentionUsers: List<MentionUser>,
            replyToMessage : ChatMessage?
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
                    timestamp = Timestamp.now(),
                    mentionedUsersInfo = mentionUsers,
                    isAReplyToOtherMessage = replyToMessage != null,
                    replyForMessageId = replyToMessage?.id,
                    replyForMessage = replyToMessage
            )

            chatGroupRepository.sendTextMessage(groupId, message)
        } catch (e: Exception) {
            e.printStackTrace()

            CrashlyticsLogger.e(
                TAG,
                "while sending text message",
                e
            )
        }
    }

    @SuppressLint("NewApi")
    fun sendNewImageMessage(
            context: Context,
            text: String = "",
            uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            val imageMetaData = ImageMetaDataHelpers.getImageMetaData(
                    context = context,
                    image = uri
            )

            val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    headerId = groupId,
                    senderInfo = createCurrentUserSenderInfo(),
                    type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                    chatType = ChatConstants.CHAT_TYPE_GROUP,
                    flowType = ChatConstants.FLOW_TYPE_OUT,
                    content = text,
                    timestamp = Timestamp.now(),
                    thumbnailBitmap = imageMetaData.thumbnail,
                    attachmentPath = null,
                    imageMetaData = imageMetaData
            )

            groupMessagesShownOnView?.add(message)
            _groupMessages.postValue(groupMessagesShownOnView)

            chatGroupRepository.sendNewImageMessage(
                    groupId = groupId,
                    message = message,
                    imageUri = uri
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendNewDocumentMessage(
            context: Context,
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
            groupMessagesShownOnView?.add(message)
            _groupMessages.postValue(groupMessagesShownOnView)

            chatGroupRepository.sendNewDocumentMessage(
                    context,
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

    fun sendNewAudioMessage(
        context: Context,
        text: String = "",
        uri: Uri,
        audioInfo: AudioInfo
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = groupId,
                senderInfo = createCurrentUserSenderInfo(),
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO,
                chatType = ChatConstants.CHAT_TYPE_GROUP,
                flowType = ChatConstants.FLOW_TYPE_OUT,
                content = text,
                attachmentName = audioInfo.name,
                timestamp = Timestamp.now(),
                videoLength = audioInfo.duration
            )
            groupMessagesShownOnView?.add(message)
            _groupMessages.postValue(groupMessagesShownOnView)

            chatGroupRepository.sendNewAudioMessage(
                context = context,
                groupId = groupId,
                audiosDirectoryRef = chatFileManager.audioFilesDirectory,
                uri = uri,
                audioInfo = audioInfo,
                message = message,
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                ChatPageViewModel.TAG,
                "while sending video message",
                e
            )
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
                    videoInfo.thumbnail?.copy(
                            videoInfo.thumbnail!!.config,
                            videoInfo.thumbnail!!.isMutable
                    )
            Log.d(TAG, "text: $text")
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

            groupMessagesShownOnView?.add(message)
            _groupMessages.postValue(groupMessagesShownOnView)

            chatGroupRepository.sendNewVideoMessage(
                    context = context.applicationContext,
                    groupId = groupId,
                    videosDirectoryRef = chatFileManager.videoFilesDirectory,
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
            mapImageFile: File?,
            isLiveLocation: Boolean,
            isCurrentlySharingLiveLocation: Boolean,
            liveEndTime: Date?
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
                    thumbnailBitmap = mapImage?.copy(mapImage.config, mapImage.isMutable),
                    isLiveLocation = isLiveLocation,
                    liveEndTime = liveEndTime,
                    isCurrentlySharingLiveLocation = isCurrentlySharingLiveLocation
            )

            groupMessagesShownOnView?.add(message)
            _groupMessages.postValue(groupMessagesShownOnView)

            chatGroupRepository.sendLocationMessage(
                    groupId = groupId,
                    message = message,
                    bitmap = mapImage
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAllPreviousLiveLocations(){
        val messagesWithActiveLiveLocations = this.grpMessages?.filter { it.type == com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION && it.isLiveLocation && it.isCurrentlySharingLiveLocation }
        messagesWithActiveLiveLocations?.forEach {
            Log.d("locationupdate", "Stoping location for: ${it.headerId} , ${it.id}")
            stopSharingLocation(groupId, it.id)
        }
    }

    fun stopSharingLocation(header: String, messageId: String) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatGroupRepository.stopSharingLocation(header, messageId)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun updateLocationChatMessage(header: String, messageId: String, location: GeoPoint) = GlobalScope.launch{
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatGroupRepository.setLocationToGroupChatMessage(header, messageId,location)
            } catch (e: Exception){
                Log.d(ChatPageViewModel.TAG, e.message.toString())
            }
        }
    }


    fun getChatHeaderInfo(headerInfoId: String) = viewModelScope.launch{
        try {
            val headerInfo = chatGroupRepository.getChatHeader(headerInfoId)
            _chatHeaderInfo.value = headerInfo
        } catch (e: Exception){
            _chatHeaderInfo.value = null
        }
    }

    private val _changeGroupName: MutableLiveData<Lse> = MutableLiveData()
    val changeGroupName: LiveData<Lse> = _changeGroupName

    fun changeGroupName(newGroupName: String) = viewModelScope.launch {
        chatGroupRepository.changeGroupName(groupId, newGroupName)
    }

    fun setMessagesUnseenCountToZero() = GlobalScope.launch {
        try {
            Log.d(TAG, "CHAT 2, Setting count to Zero for $groupId")
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
                    } else if (media.attachmentType == ChatConstants.ATTACHMENT_TYPE_AUDIO) {
                        val audiossDirectoryRef =
                            File(appDirectoryFileRef, ChatConstants.DIRECTORY_AUDIOS)

                        if (!audiossDirectoryRef.exists())
                            audiossDirectoryRef.mkdirs()

                        File(audiossDirectoryRef, fileName)
                    } else {
                        throw IllegalArgumentException("other types not supported yet")
                    }

                    if (Patterns.WEB_URL.matcher(downloadLink).matches()) {
                        firebaseStorage.getReferenceFromUrl(downloadLink).getFileOrThrow(fileRef)
                    } else {
                        firebaseStorage.getReference(downloadLink).getFileOrThrow(fileRef)
                    }

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

    fun deleteMessage(id: String) = viewModelScope.launch {
        try {
            chatGroupRepository.deleteMessage(
                    groupId,
                    id
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "deleting group message",
                    e
            )
        }
    }

    fun deleteMessages(
        messageIds: List<String>
    ) = viewModelScope.launch {
        try {

            chatGroupRepository.deleteMessages(
                messageIds,
                groupId,
            )
        } catch (e: Exception) {

            CrashlyticsLogger.e(
                ChatPageViewModel.TAG,
                "while deleting users message",
                e
            )
        }
    }

    fun isUserGroupAdmin(): Boolean {
        val groupDetails = groupDetails ?: return false
        val currentUserInGroup =
                groupDetails.groupMembers.find { it.uid == currentUser.uid } ?: return false
        return currentUserInGroup.isUserGroupManager
    }

    fun isContactModelOfCurrentUser(
            contact: ContactModel
    ): Boolean {
        return contact.uid == currentUser.uid
    }

    fun dismissAsGroupAdmin(
            uid: String
    ) = viewModelScope.launch {
        try {

            chatGroupRepository.dismissUserAsGroupAdmin(
                    groupId,
                    uid
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "dismissAsGroupAdmin",
                    e
            )
        }
    }

    fun makeUserGroupAdmin(
            uid: String
    ) = viewModelScope.launch {
        try {

            chatGroupRepository.makeUserGroupAdmin(
                    groupId,
                    uid
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "dismissAsGroupAdmin",
                    e
            )
        }
    }

    fun getCurrentChatGroupInfo(): ChatGroup? = groupDetails

    fun allowEveryoneToPostInThisGroup() = viewModelScope.launch {
        try {

            chatGroupRepository.allowEveryoneToPostInThisGroup(
                    groupId
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "dismissAsGroupAdmin",
                    e
            )
        }
    }

    fun limitPostingToAdminsInGroup() = viewModelScope.launch {
        try {

            chatGroupRepository.limitPostingToAdminsInGroup(
                    groupId
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "dismissAsGroupAdmin",
                    e
            )
        }
    }

    //Message reading info
    private val _messageReadingInfo: MutableLiveData<MessageReceivingAndReadingInfo> =
            MutableLiveData()
    val messageReadingInfo: LiveData<MessageReceivingAndReadingInfo> = _messageReadingInfo

    fun getMessageReadingInfo(
            groupId: String,
            messageId: String
    ) = viewModelScope.launch {

        try {
            val chatGroup = chatGroupRepository.getGroupDetails(groupId)
            val chatMessagesReceived = chatGroupRepository.getMessageReceivedByInfo(groupId, messageId)
            val chatMessagesDelivered = chatGroupRepository.getMessageDeliveredToInfo(groupId, messageId)

            val messageReadBy = arrayListOf<MessageReceivingInfo>()
            chatMessagesReceived.forEach {
                messageReadBy.add(it)
            }

            val messageDeliveredTo = arrayListOf<MessageReceivingInfo>()
            chatMessagesDelivered.forEach {
                messageDeliveredTo.add(it)
            }

            val totalUsersCount = chatGroup?.groupMembers
                    .filter { it.uid != currentUser.uid }
                    .count()

            _messageReadingInfo.value = totalUsersCount?.let {
                MessageReceivingAndReadingInfo(
                    totalMembers = it,
                    receivingInfo = messageDeliveredTo,
                    readingInfo = messageReadBy
                )
            }
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                    TAG,
                    "while getting message info for reading data",
                    e
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        groupMessagesListener?.remove()
        groupMessagesListener = null

        groupEventsListener?.remove()
        groupEventsListener = null

        groupDetailsListener?.remove()
        groupDetailsListener = null

        groupContactsListener?.remove()
        groupContactsListener = null

        userGroupHeaderChangeListener?.remove()
        userGroupHeaderChangeListener = null

        Log.d(TAG, "userGroupHeaderChangeListener detached")
    }

     fun getGroupMembersNameSuggestions(keywords: String): List<GroupChatMember> {
        val chatGroupMembers = groupDetails?.groupMembers ?: return emptyList()
        return chatGroupMembers.filter {
            it.uid != currentUser.uid
        }.filter {
            it.name?.startsWith(keywords, true) ?: false
        }.map {
            GroupChatMember(
                    it.name!!,
                    it.uid!!,
                    it.getUserProfileImageUrlOrPath() ?: ""
            )
        }
    }

    fun selectChatMessage(msg: ChatMessage, add: Boolean){
        val messageList = grpMessages ?: return
        val index = messageList.indexOf(msg)
        if (index != -1) {
            if (add && !selectedMessagesList.contains(msg)) {
                selectedMessagesList.add(msg)
            } else if (!add && selectedMessagesList.contains(msg)){
                selectedMessagesList.remove(msg)
            }
            _selectedChatMessage.value = selectedMessagesList
        }
    }


    fun makeSelectEnable(enable: Boolean){
        selectEnable = enable
        _enableSelect.value = enable
    }

    fun clearSelection(){
        selectedMessagesList.clear()
        _selectedChatMessage.value = emptyList()
    }

    fun getSelectEnable(): Boolean?{
        return selectEnable
    }


    fun scrollToMessage(
        replyMessage: ChatMessage
    ){
        val messageList =  grpMessages ?: return
        val index =  messageList.indexOf(replyMessage)
        if(index != -1){
            _scrollToMessage.value = index
            _scrollToMessage.value = null

            _scrollToMessageId.value = replyMessage.id
        }
    }

    fun setScrollToMessageNull(){
        _scrollToMessageId.value = null
    }
    companion object {
        const val TAG: String = "GroupChatViewModel"
    }
}