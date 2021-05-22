package com.gigforce.modules.feature_chat.screens.vm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
import com.gigforce.core.extensions.*
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.ChatAttachmentDownloadState
import com.gigforce.modules.feature_chat.DownloadCompleted
import com.gigforce.modules.feature_chat.DownloadStarted
import com.gigforce.modules.feature_chat.ErrorWhileDownloadingAttachment
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.*
import com.gigforce.modules.feature_chat.repositories.ChatProfileFirebaseRepository
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ChatPageViewModel constructor(
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
        //  private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
        private var downloadAttachmentService: DownloadChatAttachmentService = RetrofitFactory.createService(
                DownloadChatAttachmentService::class.java
        ),
        private var chatProfileFirebaseRepository: ChatProfileFirebaseRepository = ChatProfileFirebaseRepository(),
        private var chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val TAG: String = "chats/viewmodel"
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()
    private val currentUser: FirebaseUser by lazy {
        FirebaseAuth.getInstance().currentUser!!
    }

    var headerId: String = ""
    lateinit var otherUserId: String

    private var otherUserName: String? = null
    private var otherUserProfilePicture: String? = null
    private var chatMessages: MutableList<ChatMessage>? = null

    private var _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private var _headerInfo = MutableLiveData<ChatHeader>()
    val headerInfo: LiveData<ChatHeader> = _headerInfo

    private var _otherUserInfo = MutableLiveData<ContactModel>()
    val otherUserInfo: LiveData<ContactModel> = _otherUserInfo

    private var messagesListener: ListenerRegistration? = null
    private var headerInfoChangeListener: ListenerRegistration? = null


    fun setRequiredDataAndStartListeningToMessages(
            otherUserId: String,
            headerId: String?,
            otherUserName: String?,
            otherUserProfilePicture: String?
    ) {
        this.otherUserId = otherUserId
        this.otherUserName = otherUserName
        this.otherUserProfilePicture = otherUserProfilePicture

        if (headerId != null) {
            this.headerId = headerId
        }

        if (messagesListener == null) {

            if (otherUserName.isNullOrBlank()) {
                fetchContactDetailsWithUserId(otherUserId)
            } else {
                _otherUserInfo.value = ContactModel(
                        id = otherUserId,
                        headerId = headerId,
                        name = otherUserName,
                        imageThumbnailPathInStorage = otherUserProfilePicture
                )
            }

            startListeningForNewMessages()
            startListeningForHeaderChanges()
        }
    }

    private fun fetchContactDetailsWithUserId(otherUserId: String) = viewModelScope.launch {
        try {
            val contactInfo = chatRepository.getDetailsOfUserFromContacts(otherUserId)
            _otherUserInfo.postValue(contactInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startListeningForHeaderChanges() {
        if (headerId.isBlank()) {
            return
        }

        headerInfoChangeListener = getHeaderReference(headerId)
                .addSnapshotListener { snapshot, error ->

                    snapshot?.let {
                        val chatHeader = it.toObject(ChatHeader::class.java)!!.apply {
                            id = it.id
                        }

                        _headerInfo.value = chatHeader

                        if (chatHeader.unseenCount != 0) {
                            setMessagesUnseenCountToZero()
                        }
                    }
                }
    }

    private fun setMessagesAsRead(
            unreadMessages: List<ChatMessage>
    ) = GlobalScope.launch {

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

    private fun checkIfHeaderIsPresentInHeadersList() = viewModelScope.launch {
        val querySnap = firebaseDB.collection("chats")
                .document(uid)
                .collection("headers")
                .whereEqualTo("otherUserId", otherUserId)
                .getOrThrow()

        if (!querySnap.isEmpty) {
            headerId = querySnap.documents[0].id
            initForHeader()
        }
    }

    private fun getHeaderReference(headerId: String): DocumentReference {
        return firebaseDB.collection("chats")
                .document(uid)
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

        /*
                Things to handle in this:
                - Status Change in Message
                - Lazy / Paginated Load
                - Performance Optimization for Change in Message (loop over Documents should not be everytime)
         */

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
                        this.chatMessages = messages.toMutableList()

                        chatMessages?.let {

                            val unreadMessages = it.filter {
                                it.flowType == ChatConstants.FLOW_TYPE_IN &&
                                        it.status < ChatConstants.MESSAGE_STATUS_READ_BY_USER &&
                                        it.senderMessageId.isNotBlank()
                            }
                            setMessagesAsRead(unreadMessages)
                        }

                        _messages.postValue(messages)
                    }
                }
    }

    private var _sendingMessage = MutableLiveData<ChatMessage>()
    val sendingMessageOld: LiveData<ChatMessage> = _sendingMessage

    fun sendNewText(
            text: String
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
                    timestamp = Timestamp.now()
            )
            getReference(headerId).document(message.id).setOrThrow(message)

            //Update Header for current User
            firebaseDB.collection("chats")
                    .document(uid)
                    .collection("headers")
                    .document(headerId)
                    .updateOrThrow(
                            mapOf(
                                    "lastMessageType" to ChatConstants.MESSAGE_TYPE_TEXT,
                                    "lastMsgText" to text,
                                    "lastMsgTimestamp" to Timestamp.now(),
                                    "lastMsgFlowType" to ChatConstants.FLOW_TYPE_OUT,
                                    "unseenCount" to 0
                            )
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
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
            createHeaderInOtherUsersCollection()
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
        val query = firebaseDB.collection("chats")
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

    private suspend fun createHeaderInOtherUsersCollection() {
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

        val query = firebaseDB.collection("chats")
                .document(otherUserId)
                .collection("contacts")
                .whereEqualTo("uid", uid)
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
            userName = "+" + contactModel?.mobile?.substring(0, 2) + "-" + contactModel?.mobile?.substring(2)
        }

        if (userName == null) {
            userName = currentUser.phoneNumber
            userName = userName?.substring(0, 3) + "-" + userName?.substring(3)
        }

        val chatHeader = ChatHeader(
            forUserId = otherUserId,
            otherUserId = uid,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = uid,
                name = userName ?: "",
                profilePic = fullPath,
                type = "user"
            ),
            lastMsgFlowType = ""
        )

        firebaseDB.collection("chats")
                .document(otherUserId)
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
            text: String = "",
            uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val thumbnail = createThumbnail(uri)
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
                    thumbnailBitmap = thumbnail
            )
            showMessageAsSending(message)
//            _sendingMessage.postValue(message)

            chatRepository.sendImageMessage(headerId, message, uri)
        } catch (e: Exception) {
            //handle error
        }
    }

    private fun createThumbnail(uri: Uri) = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ThumbnailUtils.createImageThumbnail(File(uri.path), Size(96, 96), null)
        } else {
            ImageUtils.resizeBitmap(uri.path!!, 96, 96)
        }
    } catch (e: Exception) {
        null
    }

    @Suppress("DEPRECATION")
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

            //val thumbnailForUi = videoInfo.thumbnail?.copy(videoInfo.thumbnail.config, videoInfo.thumbnail.isMutable)
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

//            _sendingMessage.postValue(message)

            chatRepository.sendVideoMessage(
                    context = context,
                    chatHeaderId = headerId,
                    message = message,
                    uri = uri,
                    videoInfo = videoInfo
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
                    thumbnailBitmap = mapImage?.copy(mapImage.config, mapImage.isMutable)
            )
            showMessageAsSending(message)

//            _sendingMessage.postValue(message)
            chatRepository.sendLocationMessage(
                    chatHeaderId = headerId,
                    message = message,
                    bitmap = mapImage
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun uploadChatAttachment(fileNameWithExtension: String, image: Uri) =
            suspendCoroutine<String> { cont ->
                val filePathOnServer = firebaseStorage.reference
                        .child("chat_attachments")
                        .child(fileNameWithExtension)

                filePathOnServer
                        .putFile(image)
                        .addOnSuccessListener {
                            filePathOnServer
                                    .downloadUrl
                                    .addOnSuccessListener {
                                        cont.resume(it.toString())

                                    }.addOnFailureListener {
                                        cont.resumeWithException(it)
                                    }
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }

    private suspend fun uploadChatAttachment(fileNameWithExtension: String, data: ByteArray) =
            suspendCoroutine<String> { cont ->
                val filePathOnServer = firebaseStorage.reference
                        .child("chat_attachments")
                        .child(fileNameWithExtension)

                filePathOnServer
                        .putBytes(data)
                        .addOnSuccessListener {
                            filePathOnServer
                                    .downloadUrl
                                    .addOnSuccessListener {
                                        cont.resume(it.toString())

                                    }.addOnFailureListener {
                                        cont.resumeWithException(it)
                                    }
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }

    private fun prepareUniqueImageName(fileNameWithExtension: String): String {

        val extension = fileNameWithExtension.substringBeforeLast('.')
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        return "${currentUser.uid}$timeStamp$extension"
    }

    private suspend fun createHeader(
            forUserId: String,
            otherUserId: String,
            otherUserName: String?,
            otherUserProfilePicture: String?
    ): String {

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
                type = "user"
            ),
            lastMsgFlowType = ""
        )

        val docRef = firebaseDB
                .collection("chats")
                .document(uid)
                .collection("headers")
                .addOrThrow(chatHeader)

        return docRef.id
    }

    private suspend fun saveHeaderIdToContact(
            userId: String,
            headerId: String
    ) {

        val userDocument = firebaseDB.collection("chats")
                .document(uid)
                .collection("contacts")
                .whereEqualTo("uid", userId)
                .getOrThrow()

        if (userDocument.isEmpty) {
            throw IllegalStateException("ChatMessagesViewModel :saveHeaderIdToContact(), no user found with uid : $userId in contacts")
        } else {
            val userDocumentId = userDocument.documents.first().id

            firebaseDB.collection("chats")
                    .document(uid)
                    .collection("contacts")
                    .document(userDocumentId)
                    .updateOrThrow("headerId", headerId)
        }
    }


    fun setMessagesUnseenCountToZero() = GlobalScope.launch {
        if (headerId.isBlank()) {
            return@launch
        }
        Log.d(TAG, "CHAT 2 Setting count to Zero for $headerId")

        try {
            firebaseDB.collection("chats")
                    .document(uid)
                    .collection("headers")
                    .document(headerId)
                    .updateOrThrow("unseenCount", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to set unseen count to zero", e)
        }
    }

    private val _chatAttachmentDownloadState: MutableLiveData<ChatAttachmentDownloadState> =
            MutableLiveData()
    val chatAttachmentDownloadState: LiveData<ChatAttachmentDownloadState> =
            _chatAttachmentDownloadState

    fun downloadAndSaveFile(appDirectoryFileRef: File, position: Int, chatMessage: ChatMessage) =
            viewModelScope.launch {
                val downloadLink = chatMessage.attachmentPath ?: return@launch
                if (!appDirectoryFileRef.exists())
                    appDirectoryFileRef.mkdirs()

                _chatAttachmentDownloadState.value = DownloadStarted(position)

                try {

                    val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
                    val fileRef = if (chatMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE) {
                        val imagesDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)

                        if (!imagesDirectoryRef.exists())
                            imagesDirectoryRef.mkdirs()

                        File(imagesDirectoryRef, fileName)
                    } else if (chatMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO) {
                        val videosDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
                        if (!videosDirectoryRef.exists())
                            videosDirectoryRef.mkdirs()

                        File(videosDirectoryRef, fileName)
                    } else if (chatMessage.type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT) {
                        val imagesDirectoryRef =
                                File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)
                        File(imagesDirectoryRef, fileName)
                    } else {
                        throw IllegalArgumentException("other types not supperted yet")
                    }

                    val response = downloadAttachmentService.downloadAttachment(downloadLink)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        FileUtils.writeResponseBodyToDisk(body, fileRef)
                        _chatAttachmentDownloadState.value = DownloadCompleted(position)
                        _chatAttachmentDownloadState.value = null
                    } else {
                        throw Exception("Unable to dowload payslip, ${response.message()}")
                    }
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

    fun blockOrUnBlockUser() = viewModelScope.launch {

        if (headerId.isBlank())
            return@launch

        _blockingOrUnblockingUser.value = Lse.loading()
        try {
            chatRepository.blockOrUnblockUser(headerId)
            _blockingOrUnblockingUser.value = Lse.success()
            _blockingOrUnblockingUser.value = null
        } catch (e: Exception) {
            _blockingOrUnblockingUser.value =
                    Lse.error(e.message ?: "unable to block or unblock user")
            _blockingOrUnblockingUser.value = null
        }
    }

    fun reportAndBlockUser(
            chatHeader: String,
            otherUserId: String,
            reason: String
    ) = viewModelScope.launch {

        if (chatHeader.isBlank())
            return@launch

        _blockingOrUnblockingUser.value = Lse.loading()
        try {
            chatRepository.reportAndBlockUser(chatHeader, otherUserId, reason)
            _blockingOrUnblockingUser.value = Lse.success()
            _blockingOrUnblockingUser.value = null
        } catch (e: Exception) {
            _blockingOrUnblockingUser.value =
                    Lse.error(e.message ?: "unable to block or unblock user")
            _blockingOrUnblockingUser.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()

        messagesListener?.remove()
        messagesListener = null

        headerInfoChangeListener?.remove()
    }
}