package com.gigforce.app.modules.chatmodule.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.chatmodule.*
import com.gigforce.app.modules.chatmodule.models.*
import com.gigforce.app.modules.chatmodule.repository.ChatRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.wallet.remote.GeneratePaySlipService
import com.gigforce.app.utils.*
import com.gigforce.app.utils.network.RetrofitFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ChatMessagesViewModel constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private var paySlipService: GeneratePaySlipService = RetrofitFactory.generatePaySlipService(),
    private var chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val TAG: String = "chats/viewmodel"
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    var headerId: String = ""
    var otherUserName: String? = null
    var otherUserProfilePicture: String? = null
    lateinit var forUserId: String
    lateinit var otherUserId: String

    private var _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private var _headerInfo = MutableLiveData<ChatHeader>()
    val headerInfo: LiveData<ChatHeader> = _headerInfo

    private var messagesListener: ListenerRegistration? = null
    private var headerInfoChangeListener: ListenerRegistration? = null

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

        headerInfoChangeListener = getHeaderReference(headerId)
            .addSnapshotListener { snapshot, error ->

                snapshot?.let {
                    val chatHeader = it.toObject(ChatHeader::class.java)!!.apply {
                        id = it.id
                    }
                    _headerInfo.value = chatHeader
                }
            }


        messagesListener = getReference(headerId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                Log.v(TAG, "new Snapshot Received")
                Log.v(TAG, "${snapshot?.documents?.size} Documents")

                snapshot?.let {
                    val messages = it.documents.map {
                        it.toObject(Message::class.java)!!.apply {
                            this.id = it.id
                        }
                    }
                    _messages.postValue(messages)
                }
            }
    }

    private var _sendingMessage = MutableLiveData<ChatMessage>()
    val sendingMessage: LiveData<ChatMessage> = _sendingMessage

    fun sendNewText(
        text: String
    ) = viewModelScope.launch {

        try {

            if (headerId.isEmpty()) {
                //Check If header is present in my chat
                createHeaderForBothUsers()
            }

            val message = Message(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT,
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
                        "lastMessageType" to Message.MESSAGE_TYPE_TEXT,
                        "lastMsgText" to text,
                        "lastMsgTimestamp" to Timestamp.now(),
                        "unseenCount" to 0
                    )
                )
        } catch (e: Exception) {
            e.printStackTrace()
            //handle error
        }
    }

    private suspend fun createHeaderForBothUsers() {
        val headerIdFromChat = checkAndReturnIfHeaderIsPresentInchat(forUserId, otherUserId)

        if (headerIdFromChat != null) {
            headerId = headerIdFromChat
        } else {
            headerId = createHeader(
                forUserId,
                otherUserId,
                otherUserName,
                otherUserProfilePicture
            )
            createHeaderInOtherUsersCollection()
        }

        saveHeaderIdToContact(otherUserId, headerId)
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
        val profileData = profileFirebaseRepository.getProfileData()

        val fullPath =
            if (profileData.profileAvatarName.isBlank() || profileData.profileAvatarName == "avatar.jpg") {
                null
            } else {
                firebaseStorage
                    .reference
                    .child("profile_pics")
                    .child(profileData.profileAvatarName)
                    .getDownloadUrlOrThrow()
            }

        val query = firebaseDB.collection("chats")
            .document(otherUserId)
            .collection("contacts")
            .whereEqualTo("uid", forUserId)
            .getOrThrow()

        val contactModel = if (query.isEmpty) {
            null
        } else {
            query.documents[0].toObject(ContactModel::class.java).apply {
                this?.id = this?.mobile
            }
        }

        var userName = contactModel?.name
        if (userName == null) {
            userName = contactModel?.mobile
        }

        if (userName == null) {
            userName = currentUser?.phoneNumber
        }

        val chatHeader = ChatHeader(
            forUserId = otherUserId,
            otherUserId = forUserId,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = contactModel?.id ?: "",
                name = userName ?: "",
                profilePic = fullPath?.toString() ?: "",
                type = "user"
            )
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
        documentDirectoryRef: File,
        fileName: String?,
        uri: Uri
    ) = GlobalScope.launch {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val message = Message(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = fileName
            )
            _sendingMessage.postValue(ChatMessage.fromMessage(message))

            val newFileName = if (fileName.isNullOrBlank()) {
                "$uid-${DateHelper.getFullDateTimeStamp()}.mp4"//Fix it
            } else {
                "$uid-${DateHelper.getFullDateTimeStamp()}-$fileName"
            }
            val documentFile = File(documentDirectoryRef, newFileName)
            FileUtils.copyFile(context.applicationContext, fileName!!, uri, documentFile)

            val pathOnServer = uploadChatAttachment(newFileName, uri)
            message.attachmentPath = pathOnServer
            getReference(headerId).document(message.id).setOrThrow(message)
        } catch (e: Exception) {
            //handle error
        }
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

            val file = uri.toFile()
            val message = Message(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                thumbnailBitmap = thumbnail?.copy(thumbnail.config, true)
            )
            _sendingMessage.postValue(ChatMessage.fromMessage(message))

            val thumbnailPathOnServer = if (thumbnail != null) {
                val imageInBytes = ImageUtils.convertToByteArray(thumbnail)
                uploadChatAttachment("thumb-${file.name}", imageInBytes)
            } else {
                null
            }
            val pathOnServer = uploadChatAttachment(file.name, uri)

            message.thumbnail = thumbnailPathOnServer
            message.attachmentPath = pathOnServer

            getReference(headerId).document(message.id).setOrThrow(message)
        } catch (e: Exception) {
            //handle error
        }
    }

    @Suppress("DEPRECATION")
    fun sendNewVideoMessage(
        context: Context,
        videosDirectoryRef: File,
        text: String = "",
        videoInfo: VideoInfo,
        uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {

            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val thumbnailForUi = videoInfo.thumbnail?.copy(videoInfo.thumbnail.config,videoInfo.thumbnail.isMutable)
            val message = Message(
                id = UUID.randomUUID().toString(),
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT_WITH_VIDEO,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = videoInfo.name,
                videoLength = videoInfo.duration,
                thumbnailBitmap = thumbnailForUi
            )

            _sendingMessage.postValue(ChatMessage.fromMessage(message))

            if (!videosDirectoryRef.exists())
                videosDirectoryRef.mkdirs()

            val newFileName = if (videoInfo.name.isBlank()) {
                "$uid-${DateHelper.getFullDateTimeStamp()}.mp4"
            } else {

                if (videoInfo.name.endsWith(".mp4", true)) {
                    "$uid-${DateHelper.getFullDateTimeStamp()}-${videoInfo.name}"
                } else {
                    "$uid-${DateHelper.getFullDateTimeStamp()}-${videoInfo.name}.mp4"
                }
            }

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
                FileUtils.copyFile(context,newFileName,uri,file)
                file.toUri()
            }

            val thumbnailPathOnServer = if (videoInfo.thumbnail != null) {
                val imageInBytes = ImageUtils.convertToByteArray(videoInfo.thumbnail)
                val fileNameAtServer = prepareUniqueImageName("thumb.png")
                uploadChatAttachment(fileNameAtServer, imageInBytes)
            } else {
                null
            }
            val pathOnServer = uploadChatAttachment(newFileName, compressedFileUri)

            message.thumbnail = thumbnailPathOnServer
            message.attachmentPath = pathOnServer

            getReference(headerId).document(message.id).setOrThrow(message)
        } catch (e: Exception) {
            //handle error
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


    suspend fun transcodeVideo(context: Context, uri: Uri, dest: File) =
        suspendCancellableCoroutine<File> { cont ->

            val transcodeJob = Transcoder.into(dest.path)
                .addDataSource(context, uri)
                .setListener(object : TranscoderListener {
                    override fun onTranscodeCompleted(successCode: Int) {
                        cont.resume(dest)
                    }

                    override fun onTranscodeProgress(progress: Double) {}

                    override fun onTranscodeCanceled() {
                        cont.resumeWithException(CancellationException("Video Compresssion Cancelled"))
                    }

                    override fun onTranscodeFailed(exception: Throwable) {
                        cont.resumeWithException(exception)
                    }
                }).transcode()

            cont.invokeOnCancellation {
                if (!transcodeJob.isCancelled) {
                    transcodeJob.cancel(true)
                }
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
        return "$forUserId$timeStamp$extension"
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
            )
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

    fun downloadAndSaveFile(appDirectoryFileRef: File, position: Int, message: Message) =
        viewModelScope.launch {
            val downloadLink = message.attachmentPath ?: return@launch
            if (!appDirectoryFileRef.exists())
                appDirectoryFileRef.mkdirs()

            _chatAttachmentDownloadState.value = DownloadStarted(position)

            try {

                val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
                val fileRef = if (message.type == Message.MESSAGE_TYPE_TEXT_WITH_IMAGE) {
                    val imagesDirectoryRef =
                        File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)

                    if (!imagesDirectoryRef.exists())
                        imagesDirectoryRef.mkdirs()

                    File(imagesDirectoryRef, fileName)
                } else if (message.type == Message.MESSAGE_TYPE_TEXT_WITH_VIDEO) {
                    val videosDirectoryRef =
                        File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
                    if (!videosDirectoryRef.exists())
                        videosDirectoryRef.mkdirs()

                    File(videosDirectoryRef, fileName)
                } else if (message.type == Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT) {
                    val imagesDirectoryRef =
                        File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)
                    File(imagesDirectoryRef, fileName)
                } else {
                    throw IllegalArgumentException("other types not supperted yet")
                }

                val response = paySlipService.downloadPaySlip(downloadLink)
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