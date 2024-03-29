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
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.*
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.*
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.ChatAttachmentDownloadState
import com.gigforce.modules.feature_chat.DownloadCompleted
import com.gigforce.modules.feature_chat.DownloadStarted
import com.gigforce.modules.feature_chat.ErrorWhileDownloadingAttachment
import com.gigforce.modules.feature_chat.models.AudioPassingDataModel
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatPageViewModel @Inject constructor(
    private val downloadAttachmentService: DownloadChatAttachmentService,
    private val firebaseStorage: FirebaseStorage,
    private val chatProfileFirebaseRepository: ChatProfileFirebaseRepository,
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val chatFileManager : ChatFileManager
) : ViewModel() {

    companion object{
        const val TAG: String = "chats/viewmodel"
    }

    var headerId: String = ""
    lateinit var otherUserId: String

    private val currentUser : FirebaseUser get() {
        return firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
    }


    private var otherUserName: String? = null
    private var otherUserProfilePicture: String? = null
    private var otherUserMobileNo: String? = null
    private var chatMessages: MutableList<ChatMessage>? = null

    private var _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private var _headerInfo = MutableLiveData<ChatHeader>()
    val headerInfo: LiveData<ChatHeader> = _headerInfo

    private var _otherUserInfo = MutableLiveData<ContactModel>()
    val otherUserInfo: LiveData<ContactModel> = _otherUserInfo

    private var _scrollToMessage = MutableLiveData<Int?>()
    val scrollToMessage: LiveData<Int?> = _scrollToMessage

    private var _scrollToMessageId = MutableLiveData<String?>()
    val scrollToMessageId: LiveData<String?> = _scrollToMessageId

    private var messagesListener: ListenerRegistration? = null
    private var headerInfoChangeListener: ListenerRegistration? = null
    private var contactInfoChangeListener: ListenerRegistration? = null
    private var currentChatHeader: ChatHeader? = null

    private var _askForPermission = MutableLiveData<Boolean>()
    val askForPermission: LiveData<Boolean> = _askForPermission

    private var _allStoragePermissionsGranted = MutableLiveData<Boolean>()
    val allStoragePermissionsGranted: LiveData<Boolean> = _allStoragePermissionsGranted

    private var _selectedChatMessage = MutableLiveData<List<ChatMessage>>()
    val selectedChatMessage: LiveData<List<ChatMessage>> = _selectedChatMessage

    private var _audioPlaying = MutableLiveData<Boolean>()
    val audioPlaying: LiveData<Boolean> = _audioPlaying

    private var _enableSelect = MutableLiveData<Boolean>()
    val enableSelect: LiveData<Boolean> = _enableSelect

    private var _recentLocationMessageId = MutableLiveData<Pair<String, String>>()
    val recentLocationMessageId: LiveData<Pair<String, String>> = _recentLocationMessageId

//    private var _recentReceiverId = MutableLiveData<String>()
//    val recentReceiverId: LiveData<String> = _recentReceiverId

    private var _audioData = MutableLiveData<AudioPassingDataModel>()
    val audioData: LiveData<AudioPassingDataModel> = _audioData

    private var _currentlyPlayingAudioMessageId = MutableLiveData<String>()
    val currentlyPlayingAudioMessageId: LiveData<String> = _currentlyPlayingAudioMessageId

    private var selectEnable: Boolean? = null
    private var selectedMessagesList = arrayListOf<ChatMessage>()
//    private var audioPlaying: Boolean? = null
    private var currentlyPlayingAudioMessage: String? = null


    fun setRequiredDataAndStartListeningToMessages(
        otherUserId: String,
        headerId: String?,
        otherUserName: String?,
        otherUserProfilePicture: String?,
        otherUserMobileNo: String?
    ) {
        this.otherUserId = otherUserId
        this.otherUserName = otherUserName
        this.otherUserProfilePicture = otherUserProfilePicture
        this.otherUserMobileNo = otherUserMobileNo

        if (headerId != null) {
            this.headerId = headerId
        }

        if (messagesListener == null) {

            if (!otherUserName.isNullOrBlank()) {
                _otherUserInfo.value = ContactModel(
                    id = otherUserId,
                    headerId = headerId,
                    name = otherUserName,
                    imageThumbnailPathInStorage = otherUserProfilePicture
                )
            }

            startListeningForNewMessages()
            startListeningForHeaderChanges()

            if (otherUserMobileNo.isNullOrBlank()) {
                viewModelScope.launch {
                    val userMobileNo = tryFetchingUsersNoFromProfile()

                    if (userMobileNo.isNotBlank()) {
                        startListeningForContactChanges(userMobileNo)
                    }
                }
            } else {
                startListeningForContactChanges(otherUserMobileNo)
            }
        }
    }


    private fun startListeningForHeaderChanges() {
        if (headerId.isBlank()) {
            return
        }

        headerInfoChangeListener = getHeaderReference(headerId)
            .addSnapshotListener { snapshot, error ->
                Log.d(TAG,"header info changed/subscribed, $headerId")

                snapshot?.let {
                    val chatHeader = it.toObject(ChatHeader::class.java)!!.apply {
                        id = it.id
                    }

                    currentChatHeader = chatHeader
                    _headerInfo.value = chatHeader

                    if (chatHeader.unseenCount != 0) {
                        setMessagesUnseenCountToZero()
                    }
                }
            }
    }

    fun setMessagesAsRead(
        unreadMessages: List<ChatMessage>
    ) = viewModelScope.launch {

        try {
            chatRepository.setMessagesAsRead(unreadMessages)
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().apply {

                this.log("Error while setting messages as unread")
                this.recordException(e)
            }
        }
    }


    fun startListeningForNewMessages() {

        if (!headerId.isBlank()) {
            //Header Id will be blank in case there had been no convo between users
            initForHeader()
        } else {
            checkIfHeaderIsPresentInHeadersList()
        }
    }

     fun startListeningForContactChanges(
        mobileNo: String
    ) {


        contactInfoChangeListener = chatRepository.getDetailsOfUserFromContactsQuery(
            formatMobileNoForChatContact(
                mobileNo
            )
        ).addSnapshotListener { value, error ->
                Log.d(TAG,"contact info changed/subscribed")

                value?.let {

                    if (it.exists()) {
                        val contactInfo = it.toObject(ContactModel::class.java).apply {
                            this?.id = it.id
                        }

                        _otherUserInfo.value = contactInfo
                    }
                }

                error?.let {
                    it.printStackTrace()
                }
            }

    }

    suspend fun getContactStoredByMobile(
        otherUserUID: String
    ) : String {

//        try {
            val contactModel = chatRepository.getDetailsOfUserFromContacts(otherUserUID)
            Log.d("ChatPageViewModel", "catchingH: ${contactModel.uid} , ${contactModel.name}")
            return contactModel.name.toString()
//        } catch (e: Exception) {
//            Log.d("ChatPageViewModel", "catching: ${e.message}")
//            otherUserName = ""
//        }
        //return  otherUserName
    }

    private fun checkIfHeaderIsPresentInHeadersList() = viewModelScope.launch {
        val querySnap = firebaseFirestore.collection("chats")
            .document(firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid)
            .collection("headers")
            .whereEqualTo("otherUserId", otherUserId)
            .getOrThrow()

        if (!querySnap.isEmpty) {
            headerId = querySnap.documents[0].id
            initForHeader()
        } else {
            _messages.postValue(emptyList())
        }
    }

    private fun getHeaderReference(headerId: String): DocumentReference {
        return firebaseFirestore.collection("chats")
            .document(firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid)
            .collection("headers")
            .document(headerId)
    }

    private fun getReference(headerId: String): CollectionReference {
        return getHeaderReference(headerId)
            .collection("chat_messages")
    }

    private fun initForHeader() {

        if (headerId.isEmpty()) {
            //Header Id is not generated yet
            return
        }

        messagesListener = getReference(headerId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                Log.v(TAG, "new Snapshot Received")
                Log.v(TAG, "${snapshot?.documents?.size} Documents")

                snapshot?.let {
                    val messages = it.documents.map {
                        it.toObject(ChatMessage::class.java)!!.apply {
                            this.id = it.id
                            this.chatType = ChatConstants.CHAT_TYPE_USER
                        }
                    }

                    messages.forEach { message ->
                        if (message.isAReplyToOtherMessage && message.replyForMessageId != null) {
                            message.replyForMessage =
                                messages.find { it.id == message.replyForMessageId || message.replyForMessageId == it.otherUsersMessageId }
                        }

                        if (message.flowType == ChatConstants.FLOW_TYPE_IN) {
                            message.senderInfo.name = currentChatHeader?.otherUser?.name ?: ""
                        }
                    }

                    this.chatMessages = messages.toMutableList()
                    chatMessages?.let {

                        val unreadMessages = it.filter {
                            it.flowType == ChatConstants.FLOW_TYPE_IN &&
                                    it.status < ChatConstants.MESSAGE_STATUS_READ_BY_USER &&
                                    it.senderMessageId.isNotBlank()
                        }
                        if (unreadMessages.isNotEmpty()){
                            setMessagesAsRead(unreadMessages)
                        }

                    }
                    if (messages.isNotEmpty()) {
                        var recentLiveLocationMessage : ChatMessage? = null
                        val messagesWithCurrentlySharingLiveLocation = messages.filter { it.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION && it.isLiveLocation && it.isCurrentlySharingLiveLocation }
                        if (messagesWithCurrentlySharingLiveLocation.isNotEmpty()){
                            recentLiveLocationMessage = messagesWithCurrentlySharingLiveLocation.last()
                            Log.d("locationupdate", "Sharing message with fragment ${recentLiveLocationMessage.id}")
                        }

                        if (recentLiveLocationMessage != null) {
                            _recentLocationMessageId.postValue(
                                Pair(
                                    recentLiveLocationMessage?.id,
                                    recentLiveLocationMessage?.receiverInfo?.id
                                ) as Pair<String, String>
                            )
                        }
                    }
                    _messages.postValue(messages)

                }
            }
    }

    fun isUpdatedAtAndEndDateDiffIsGreaterThanOneMinute(updatedAt: Date?, endDate: Date?): Boolean {
        val diff: Long = endDate?.time?.minus(updatedAt?.time!!) ?: 0
        val minDiff = TimeUnit.MILLISECONDS.toMinutes(diff)
        return minDiff > 1
    }

    fun stopAllPreviousLiveLocations(){
        val messagesWithActiveLiveLocations = this.chatMessages?.filter { it.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION && it.isLiveLocation }
        messagesWithActiveLiveLocations?.forEach {
            Log.d("locationupdate", "Stoping location for: ${it.headerId} , ${it.id}")
            stopSharingLocation(headerId, it.id)
            stopSharingLocationForReceiver(headerId, it.id, it.receiverInfo?.id.toString())
        }
    }

    private var _sendingMessage = MutableLiveData<ChatMessage>()
    val sendingMessageOld: LiveData<ChatMessage> = _sendingMessage

    fun sendNewText(
        text: String,
        replyToMessage: ChatMessage?
    ) = viewModelScope.launch {

        try {

            if (headerId.isEmpty()) {
                //Check If header is present in my chat
                createHeaderForBothUsers()
            }

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid,
                    mobileNo = currentUser.phoneNumber!!
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT,
                content = text,
                timestamp = Timestamp.now(),
                isAReplyToOtherMessage = replyToMessage != null,
                replyForMessageId = replyToMessage?.id,
                replyForMessage = replyToMessage
            )
            showMessageAsSending(message)
            getReference(headerId).document(message.id).setOrThrow(message)

            //Update Header for current User
//            firebaseFirestore.collection("chats")
//                .document(currentUser.uid)
//                .collection("headers")
//                .document(headerId)
//                .updateOrThrow(
//                    mapOf(
//                        "lastMessageType" to ChatConstants.MESSAGE_TYPE_TEXT,
//                        "lastMsgText" to text,
//                        "lastMsgTimestamp" to Timestamp.now(),
//                        "lastMsgFlowType" to ChatConstants.FLOW_TYPE_OUT,
//                        "unseenCount" to 0,
//                        "updatedAt" to Timestamp.now(),
//                        "updatedBy" to FirebaseAuthStateListener.getInstance()
//                            .getCurrentSignInUserInfoOrThrow().uid
//                    )
//                )
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
    }

     fun forwardMessage(forwardChat: ChatMessage, contactsList: List<ContactModel>)= viewModelScope.launch {
            Log.d("forward", "true")
        try {

            if (contactsList.isNotEmpty()) {
                //create header if not exists
                contactsList.forEach { it1 ->
                    var newHeaderId = ""
                    if (it1.headerId == null) {
                      createHeaderWithContactsForBothUsers(
                            currentUser?.uid,
                            it1.uid.toString(),
                            it1.getUserProfileImageUrlOrPath().toString(),
                            it1.profileName.toString()
                        )
                        Log.d("headerId", "new $newHeaderId")
                        it1.headerId = newHeaderId
                    }

                    forwardChat?.let { it ->
                        it.senderInfo = UserInfo(
                            id = currentUser.uid,
                            mobileNo = currentUser.phoneNumber!!
                        )
                        it.receiverInfo = UserInfo(
                            id = otherUserId
                        )
                        it.flowType = "out"
                        it.timestamp = Timestamp.now()
                    }
                }

                chatRepository.forwardChatMessage(contactsList, forwardChat)
            }
        } catch (e: Exception){
            Log.d("forward", "error: ${e.message}")
        }
    }

    private suspend fun createHeaderWithContactsForBothUsers(senderId: String, receiverId: String, profilePicture: String, name: String) : String{
        var headerIdToSend = ""
        val headerIdForChat = checkAndReturnIfHeaderIsPresentInchat(
            senderId,
            receiverId
        )

        if (headerIdForChat != null) {
            headerIdToSend = headerIdForChat
            Log.d("headerIdToChat", "id: $headerIdForChat")
        } else {
            headerIdToSend = createHeader(
                senderId,
                receiverId,
                name,
                profilePicture
            )
            createHeaderInOtherUsersCollection(senderId, receiverId)
        }

        try {
            saveHeaderIdToContact(receiverId, headerIdToSend)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initForHeader()
        Log.d("headerIdToSend", "id: $headerIdToSend")
        return headerIdToSend

    }

    private suspend fun createHeaderForBothUsers() {
        val headerIdFromChat = checkAndReturnIfHeaderIsPresentInchat(
            forUserId = currentUser.uid,
            otherUserId = otherUserId
        )

        if (headerIdFromChat != null) {
            headerId = headerIdFromChat
        } else {
            headerId = createHeader(
                currentUser.uid,
                otherUserId,
                otherUserName,
                otherUserProfilePicture
            )
            createHeaderInOtherUsersCollection(currentUser?.uid, otherUserId)
        }

        try {
            saveHeaderIdToContact(otherUserId, headerId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initForHeader()
    }



    private suspend fun checkAndReturnIfHeaderIsPresentInchat(
        forUserId: String,
        otherUserId: String
    ): String? {
        val query = firebaseFirestore.collection("chats")
            .document(forUserId)
            .collection("headers")
            .whereEqualTo("forUserId", forUserId)
            .whereEqualTo("otherUserId", otherUserId)
            .getOrThrow()

        return if (query.isEmpty)
            return null
        else {
            query.documents.get(0).get("headerId") as String
        }
    }

    private suspend fun createHeaderInOtherUsersCollection(senderId: String, receiverId: String) {
        val profileData = chatProfileFirebaseRepository.getProfileDataIfExist()

        var fullPath: String = ""
        profileData?.let {

            fullPath = if (it.profileAvatarName.isBlank() || it.profileAvatarName == "avatar.jpg") {
                ""
            } else {
                firebaseStorage
                    .reference
                    .child("profile_pics")
                    .child(profileData.profileAvatarName)
                    .getDownloadUrlOrThrow()
                    .toString()
            }
        }

        val query = firebaseFirestore.collection("chats")
            .document(receiverId)
            .collection("contacts")
            .whereEqualTo("uid", senderId)
            .getOrThrow()

        val contactModel = if (query.isEmpty) {
            null
        } else {
            query.documents[0].toObject(ContactModel::class.java).apply {
                this?.id = this?.mobile
            }
        }

        var userName = contactModel?.name
        if (userName == null && !contactModel?.mobile.isNullOrBlank()) {
            userName =
                "+" + contactModel?.mobile?.substring(0, 2) + "-" + contactModel?.mobile?.substring(
                    2
                )
        }

        if (userName.isNullOrBlank()) {
            val profile = chatProfileFirebaseRepository.getProfileDataIfExist()

            if (profile != null) {
                userName = profile.name
            } else {
                userName = currentUser.phoneNumber
                userName = userName?.substring(0, 3) + "-" + userName?.substring(3)
            }
        }

        val chatHeader = ChatHeader(
            forUserId = receiverId,
            otherUserId = senderId,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = senderId,
                name = userName,
                profilePic = fullPath,
                type = "user",
                mobileNo = profileData?.loginMobile ?: ""
            ),
            lastMsgFlowType = ""
        )

        firebaseFirestore.collection("chats")
            .document(receiverId)
            .collection("headers")
            .document(headerId)
            .setOrThrow(chatHeader)
    }



    fun sendNewDocumentMessage(
        context: Context,
        text: String = "",
        fileName: String?,
        uri: Uri
    ) = GlobalScope.launch {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = fileName
            )
            showMessageAsSending(message)
            chatRepository.sendDocumentMessage(
                context = context,
                chatHeaderId = headerId,
                message = message,
                fileName = fileName ?: "document",
                uri = uri
            )
        } catch (e: Exception) {
            //handle error
        }
    }

    private fun showMessageAsSending(message: ChatMessage) {

        if (chatMessages != null) {
            chatMessages!!.add(message)
        } else {
            chatMessages = mutableListOf()
            chatMessages!!.add(message)
        }

        _messages.postValue(chatMessages)
    }


    @SuppressLint("NewApi")
    fun sendNewImageMessage(
        context: Context,
        text: String = "",
        uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }


            val imageMetaData = ImageMetaDataHelpers.getImageMetaData(
                context = context,
                image = uri
            )

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                thumbnailBitmap = imageMetaData.thumbnail,
                imageMetaData = imageMetaData
            )
            showMessageAsSending(message)
            chatRepository.sendImageMessage(headerId, message, uri)

        } catch (e: Exception) {

            CrashlyticsLogger.e(
                TAG,
                "while sending image message",
                e
            )
        }
    }

    fun sendNewAudioMessage(
        context: Context,
        text: String = "",
        uri: Uri,
        audioInfo: AudioInfo
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = audioInfo.name,
                audioLength = audioInfo.duration
            )
            showMessageAsSending(message)

            chatRepository.sendAudioMessage(
                context = context,
                chatHeaderId = headerId,
                message = message,
                audiosDirectoryRef = chatFileManager.audioFilesDirectory,
                file = uri,
                audioInfo = audioInfo
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                TAG,
                "while sending audio message",
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

            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val thumbnailForUi =
                videoInfo.thumbnail?.copy(
                    videoInfo.thumbnail?.config, videoInfo.thumbnail?.isMutable
                        ?: false
                )
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = videoInfo.name,
                videoLength = videoInfo.duration,
                thumbnailBitmap = videoInfo.thumbnail
            )
            showMessageAsSending(message)

            chatRepository.sendVideoMessage(
                context = context,
                chatHeaderId = headerId,
                message = message,
                uri = uri,
                videoInfo = videoInfo
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                TAG,
                "while sending video message",
                e
            )
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

            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val mapImage: Bitmap? = if (mapImageFile != null) {
                BitmapFactory.decodeFile(mapImageFile.absolutePath)
            } else {
                null
            }

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                senderInfo = UserInfo(
                    id = currentUser.uid
                ),
                receiverInfo = UserInfo(
                    id = otherUserId
                ),
                flowType = "out",
                chatType = ChatConstants.CHAT_TYPE_USER,
                type = ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION,
                timestamp = Timestamp.now(),
                location = GeoPoint(latitude, longitude),
                locationPhysicalAddress = physicalAddress,
                thumbnailBitmap = mapImage?.copy(mapImage.config, mapImage.isMutable),
                isLiveLocation = isLiveLocation,
                isCurrentlySharingLiveLocation = isCurrentlySharingLiveLocation
            )

            showMessageAsSending(message)
            chatRepository.sendLocationMessage(
                chatHeaderId = headerId,
                message = message,
                bitmap = mapImage
            )
        } catch (e: Exception) {
            CrashlyticsLogger.e(
                TAG,
                "while sending location message",
                e
            )
        }
    }

    fun updateMuteNotificationsInDB(enable: Boolean, header: String) = GlobalScope.launch{
        if (header.isNotEmpty()){
            try {
                chatRepository.updateMuteNotifications(enable, header)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun updateLocationChatMessage(header: String, messageId: String, location: GeoPoint) = GlobalScope.launch{
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.setLocationToSenderChatMessage(header, messageId,location)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun stopLocationChatMessage(header: String, messageId: String, location: GeoPoint) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()) {
            try {
                chatRepository.stopLocationToSenderChatMessage(header, messageId, location)
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun stopLocationReceiverChatMessage(header: String, messageId: String, location: GeoPoint, receiverId: String) = GlobalScope.launch{
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.stopLocationToReceiverChatMessage(header, receiverId,  messageId,location)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun updateLocationReceiverChatMessage(header: String, messageId: String, location: GeoPoint, receiverId: String) = GlobalScope.launch{
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.setLocationToReceiverChatMessage(header, receiverId,  messageId,location)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun stopSharingLocation(header: String, messageId: String) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.stopSharingLocation(header, messageId)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun stopSharingLocationForReceiver(header: String, messageId: String, receiverId: String) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.stopLocationForReceiver(header, messageId, receiverId)
            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }

    private suspend fun createHeader(
        forUserId: String,
        otherUserId: String,
        otherUserName: String?,
        otherUserProfilePicture: String?
    ): String {

        val otherUserProfile = chatProfileFirebaseRepository.getProfileDataIfExist(otherUserId)

        val chatHeader = ChatHeader(
            forUserId = forUserId,
            otherUserId = otherUserId,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = "",
                name = otherUserName ?: "",
                profilePic = otherUserProfilePicture ?: "",
                type = "user",
                mobileNo = otherUserProfile?.loginMobile ?: ""
            ),
            lastMsgFlowType = ""
        )

        val docRef = firebaseFirestore
            .collection("chats")
            .document(currentUser.uid)
            .collection("headers")
            .addOrThrow(chatHeader)

        return docRef.id
    }

    private suspend fun saveHeaderIdToContact(
        userId: String,
        headerId: String
    ) {

        val userDocument = firebaseFirestore.collection("chats")
            .document(currentUser.uid)
            .collection("contacts")
            .whereEqualTo("uid", userId)
            .getOrThrow()

        if (userDocument.isEmpty) {
            throw IllegalStateException("ChatMessagesViewModel :saveHeaderIdToContact(), no user found with uid : $userId in contacts")
        } else {
            val userDocumentId = userDocument.documents.first().id

            firebaseFirestore.collection("chats")
                .document(currentUser.uid)
                .collection("contacts")
                .document(userDocumentId)
                .updateOrThrow(
                    mapOf(
                        "headerId" to headerId,
                        "updatedAt" to Timestamp.now(),
                        "updatedBy" to FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    )
                )
        }
    }


    fun setMessagesUnseenCountToZero() = GlobalScope.launch {
        if (headerId.isBlank()) {
            return@launch
        }
        Log.d(TAG, "CHAT 2 Setting count to Zero for $headerId")

        try {
            firebaseFirestore.collection("chats")
                .document(currentUser.uid)
                .collection("headers")
                .document(headerId)
                .updateOrThrow(mapOf("unseenCount" to 0,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to FirebaseAuthStateListener.getInstance()
                    .getCurrentSignInUserInfoOrThrow().uid))
        } catch (e: Exception) {
            Log.e(TAG, "Unable to set unseen count to zero", e)
        }
    }

    private val _chatAttachmentDownloadState: MutableLiveData<ChatAttachmentDownloadState> =
        MutableLiveData()
    val chatAttachmentDownloadState: LiveData<ChatAttachmentDownloadState> =
        _chatAttachmentDownloadState

    fun downloadAndSaveFile(
        appDirectoryFileRef: File,
        position: Int,
        mediaMessage: GroupMedia
    ) = viewModelScope.launch {

        val downloadLink = mediaMessage.attachmentPath ?: return@launch
        if (!appDirectoryFileRef.exists())
            appDirectoryFileRef.mkdirs()

        _chatAttachmentDownloadState.value = DownloadStarted(position)

        try {

            val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
            val fileRef = if (mediaMessage.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE) {
                val imagesDirectoryRef =
                    File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)

                if (!imagesDirectoryRef.exists())
                    imagesDirectoryRef.mkdirs()


                File(imagesDirectoryRef, fileName)
            } else if (mediaMessage.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO) {
                val videosDirectoryRef =
                    File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
                if (!videosDirectoryRef.exists())
                    videosDirectoryRef.mkdirs()


                File(videosDirectoryRef, fileName)
            } else if (mediaMessage.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT) {
                val imagesDirectoryRef =
                    File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)


                File(imagesDirectoryRef, fileName)
            } else if (mediaMessage.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO) {
                val audiosDirectoryRef =
                    File(appDirectoryFileRef, ChatConstants.DIRECTORY_AUDIOS)


                File(audiosDirectoryRef, fileName)
            } else {
                throw IllegalArgumentException("other types not supperted yet")
            }



            if (Patterns.WEB_URL.matcher(downloadLink).matches()) {
                firebaseStorage.getReferenceFromUrl(downloadLink).getFileOrThrow(fileRef)
            } else {
                firebaseStorage.getReference(downloadLink).getFileOrThrow(fileRef)
            }

            _chatAttachmentDownloadState.value = DownloadCompleted(position)
            _chatAttachmentDownloadState.value = null

//            val response = downloadAttachmentService.downloadAttachment(downloadLink)
//            if (response.isSuccessful) {
//                val body = response.body()!!
//                FileUtils.writeResponseBodyToDisk(body, fileRef)
//                _chatAttachmentDownloadState.value = DownloadCompleted(position)
//                _chatAttachmentDownloadState.value = null
//            } else {
//                throw Exception("Unable to dowload payslip, ${response.message()}")
//            }
        } catch (e: Exception) {
            _chatAttachmentDownloadState.value = ErrorWhileDownloadingAttachment(
                position,
                e.message ?: "Unable to download attachment"
            )
            _chatAttachmentDownloadState.value = null
        }
    }

    private var _blockingOrUnblockingUser = MutableLiveData<Lse>()
    val blockingOrUnblockingUser: LiveData<Lse> = _blockingOrUnblockingUser

    fun blockOrUnBlockUser(
        chatHeader: String,
        otherUserId: String,
        forceBlock: Boolean
    ) = viewModelScope.launch {
        _blockingOrUnblockingUser.value = Lse.loading()

        try {
            chatRepository.blockOrUnblockUser(
                chatHeaderId = chatHeader,
                otherUserId = otherUserId,
                forceBlock = forceBlock
            )

            _blockingOrUnblockingUser.value = Lse.success()
            _blockingOrUnblockingUser.value = null
        } catch (e: Exception) {
            _blockingOrUnblockingUser.value =
                Lse.error(e.message ?: "unable to block or unblock user")
            _blockingOrUnblockingUser.value = null
        }
    }

    private suspend fun tryFetchingUsersNoFromProfile(): String {
        try {
            val profile = chatProfileFirebaseRepository.getProfileDataIfExist(otherUserId)
            otherUserMobileNo = profile?.loginMobile

            Log.wtf("D", "S")
            return profile?.loginMobile ?: ""
        } catch (e: Exception) {
            // otherUserMobileNo = ""
            return ""
        }
    }

    fun reportAndBlockUser(
        chatHeader: String,
        otherUserId: String,
        reason: String
    ) = viewModelScope.launch {

        _blockingOrUnblockingUser.value = Lse.loading()
        try {

            chatRepository.reportAndBlockUser(
                chatHeader,
                otherUserId,
                reason
            )

            _blockingOrUnblockingUser.value = Lse.success()
            _blockingOrUnblockingUser.value = null
        } catch (e: Exception) {
            _blockingOrUnblockingUser.value =
                Lse.error(e.message ?: "unable to block or unblock user")
            _blockingOrUnblockingUser.value = null
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

    override fun onCleared() {
        super.onCleared()

        messagesListener?.remove()
        messagesListener = null

        headerInfoChangeListener?.remove()
        contactInfoChangeListener?.remove()
    }

    fun deleteMessage(
        messageId: String
    ) = viewModelScope.launch {
        try {

            chatRepository.deleteMessage(
                chatHeaderId = headerId,
                messageId = messageId
            )
        } catch (e: Exception) {

            CrashlyticsLogger.e(
                TAG,
                "while deleting users message",
                e
            )
        }
    }

    fun deleteMessages(
        messageIds: List<String>
    ) = viewModelScope.launch {
        try {

            chatRepository.deleteMessages(
                messageIds,
                headerId,
            )
        } catch (e: Exception) {

            CrashlyticsLogger.e(
                TAG,
                "while deleting users message",
                e
            )
        }
    }

    fun selectChatMessage(msg: ChatMessage, add: Boolean){
        val messageList = chatMessages ?: return
        messageList.forEachIndexed { index, chatMessage ->
            if (chatMessage.id == msg.id){
                if (add && !selectedMessagesList.contains(msg)) {
                    selectedMessagesList.add(msg)
                } else if (!add && selectedMessagesList.contains(msg)){
                    selectedMessagesList.remove(msg)
                }
                _selectedChatMessage.value = selectedMessagesList
            }
        }
//        if (index != -1) {
//            if (add && !selectedMessagesList.contains(msg)) {
//                selectedMessagesList.add(msg)
//            } else if (!add && selectedMessagesList.contains(msg)){
//                selectedMessagesList.remove(msg)
//            }
//            _selectedChatMessage.value = selectedMessagesList
//        }
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
    ) {
        val messageList = chatMessages ?: return
        val index = messageList.indexOf(replyMessage)
        if (index != -1) {
            _scrollToMessage.value = index
            _scrollToMessage.value = null

            _scrollToMessageId.value = replyMessage.id
        }
    }

    fun setScrollToMessageNull(){
        _scrollToMessageId.value = null
    }
    fun playMyAudio(play: Boolean, pause: Boolean, stop: Boolean, messageId: String, uri: Uri){
//        var audioDataToPass =  AudioPassingDataModel(playPause, true,  messageId, uri)
//        _audioData.value = audioDataToPass
        if(play || pause){
            currentlyPlayingAudioMessage = messageId
        } else if(stop){
            currentlyPlayingAudioMessage = ""
        }
        Log.d("viewModelChat", "id: $currentlyPlayingAudioMessage")
    }

}