package com.gigforce.app.modules.chatmodule.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.wallet.remote.GeneratePaySlipService
import com.gigforce.app.utils.*
import com.gigforce.app.utils.network.RetrofitFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ChatMessagesViewModel constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private var paySlipService: GeneratePaySlipService = RetrofitFactory.generatePaySlipService()
) : ViewModel() {

    private val TAG: String = "chats/viewmodel"
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()

    var headerId: String = ""
    var otherUserName: String? = null
    var otherUserProfilePicture: String? = null
    lateinit var forUserId: String
    lateinit var otherUserId: String

    private var _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun startListeningForNewMessages() {

        if (!headerId.isBlank()) {

            //Header Id will be blank in case there had been no convo between users
            initForHeader()
        }
    }

    private fun getReference(headerId: String): CollectionReference {
        return firebaseDB.collection("chats")
            .document(uid)
            .collection("headers")
            .document(headerId)
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

        getReference(headerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    fun sendNewText(
        text: String
    ) = viewModelScope.launch {

        try {

            if (headerId.isEmpty()) {
                //Check If header is present in my chat
                createHeaderForBothUsers()
            }

            val message = Message(
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT,
                content = text,
                timestamp = Timestamp.now()
            )

            getReference(headerId).addOrThrow(message)

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

        val fullPath = if (profileData.profileAvatarName.isBlank()) {
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
            .whereEqualTo("uid", otherUserId)
            .getOrThrow()

        val contactModel = if (query.isEmpty) {
            null
        } else {
            query.documents[0].toObject(ContactModel::class.java).apply {
                this?.id = this?.mobile
            }
        }

        val userName = contactModel?.name
        if (userName == null) {

        }

        val otherUserHeaderDoc = hashMapOf(
            "lastMsgText" to "",
            "forUserId" to otherUserId,
            "otherUserId" to forUserId,
            "lastMsgTimestamp" to null,
            "type" to "user",
            "unseenCount" to 0,
            "otherUser" to hashMapOf(
                "name" to userName,
                "profilePic" to fullPath.toString(),
                "type" to "user",
                "unseenCount" to 0
            )
        )

        firebaseDB.collection("chats")
            .document(otherUserId)
            .collection("headers")
            .document(headerId)
            .setOrThrow(otherUserHeaderDoc)
    }

    fun sendNewDocumentMessage(
        text: String = "",
        fileName: String,
        uri: Uri
    ) = GlobalScope.launch {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val message = Message(
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

            val docReference = getReference(headerId).addOrThrow(message)
            val pathOnServer = uploadChatAttachment(fileName, uri)
            updatePathInMessage(docReference.id, pathOnServer)
            updatePathInOtherUserMessageToo(docReference.id, pathOnServer)
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

            val message = Message(
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT_WITH_IMAGE,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null
            )

            val thumbnail =
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ThumbnailUtils.createImageThumbnail(File(uri.path), Size(96, 96), null)
                    } else {
                        ThumbnailUtils.createImageThumbnail(
                            uri.path!!,
                            MediaStore.Video.Thumbnails.MICRO_KIND
                        )
                    }
                } catch (e : Exception){
                    null
                }

            val thumbnailPathOnServer = if (thumbnail != null) {
                val imageInBytes = convertToByteArray(thumbnail)
                uploadChatAttachment("thumb.png", imageInBytes)
            } else {
                null
            }

            val docReference = getReference(headerId).addOrThrow(message)
            val pathOnServer = uploadChatAttachment("image.jpg", uri)
            updatePathInMessage(docReference.id, pathOnServer,thumbnailPathOnServer)
            updatePathInOtherUserMessageToo(docReference.id, pathOnServer,thumbnailPathOnServer)
        } catch (e: Exception) {
            //handle error
        }
    }

    private suspend fun updatePathInOtherUserMessageToo(
        id: String,
        pathOnServer: String,
        thumbnailPath: String? = null
    ) {
        val otherMessageId = firebaseDB.collection("chats")
            .document(uid)
            .collection("headers")
            .document(headerId)
            .collection("chat_messages")
            .document(id)
            .getOrThrow()
            .get("otherMessageId") as String?

        if (otherMessageId != null) {

            firebaseDB.collection("chats")
                .document(otherUserId)
                .collection("headers")
                .document(headerId)
                .collection("chat_messages")
                .document(otherMessageId)
                .updateOrThrow(
                    mapOf(
                        "attachmentPath" to pathOnServer,
                        "thumbnail" to thumbnailPath
                    )
                )
        }
    }

    @Suppress("DEPRECATION")
    fun sendNewVideoMessage(
        context: Context,
        text: String = "",
        fileName: String,
        uri: Uri
    ) = GlobalScope.launch(Dispatchers.IO) {

        try {
            if (headerId.isEmpty()) {
                createHeaderForBothUsers()
            }

            val message = Message(
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = Message.MESSAGE_TYPE_TEXT_WITH_VIDEO,
                content = text,
                timestamp = Timestamp.now(),
                attachmentPath = null,
                attachmentName = fileName
            )

            val docReference = getReference(headerId).addOrThrow(message)

            val transcodedFile = File(
                context.filesDir,
                "vid_${DateHelper.getFullDateTimeStamp()}.mp4"
            )
            transcodeVideo(context, uri, transcodedFile)
            val compressedFileUri = transcodedFile.toUri()

            val thumbnail =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    ThumbnailUtils.createVideoThumbnail(transcodedFile, Size(96, 96), null)
                } else {
                    ThumbnailUtils.createVideoThumbnail(
                        transcodedFile.absolutePath,
                        MediaStore.Video.Thumbnails.MICRO_KIND
                    )
                }


            val thumbnailPathOnServer = if (thumbnail != null) {
                val imageInBytes = convertToByteArray(thumbnail)
                uploadChatAttachment("thumb.png", imageInBytes)
            } else {
                null
            }

            val pathOnServer = uploadChatAttachment("video.mp4", compressedFileUri)

            updatePathInMessage(docReference.id, pathOnServer, thumbnailPathOnServer)
            updatePathInOtherUserMessageToo(docReference.id, pathOnServer, thumbnailPathOnServer)
        } catch (e: Exception) {
            //handle error
        }
    }

    private fun convertToByteArray(thumbnail: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        thumbnail.recycle()
        return byteArray
    }

    suspend fun transcodeVideo(context: Context, uri: Uri, dest: File) =
        suspendCancellableCoroutine<File> { cont ->

            val transcodeJob = Transcoder.into(dest.path)
                .addDataSource(context, uri)
                .setListener(object : TranscoderListener {
                    override fun onTranscodeCompleted(successCode: Int) {
                        //  _selfieVideoUploadState.value = "Video Compressed"
                        cont.resume(dest)
                    }

                    override fun onTranscodeProgress(progress: Double) {
//                        val progressInTens = progress * 100
//                        _selfieVideoUploadState.value =
//                            "Compressing Video ${String.format("%.2f", progressInTens)} %"
                    }

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
            val fileNameAtServer = prepareUniqueImageName(fileNameWithExtension)
            val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(fileNameAtServer)

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
            val fileNameAtServer = prepareUniqueImageName(fileNameWithExtension)
            val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(fileNameAtServer)

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

    private suspend fun updatePathInMessage(
        id: String,
        pathOnServer: String,
        thumbnailPath: String? = null
    ) {
        getReference(headerId).document(id)
            .updateOrThrow(mapOf("attachmentPath" to pathOnServer, "thumbnail" to thumbnailPath))
    }


    private suspend fun createHeader(
        forUserId: String,
        otherUserId: String,
        otherUserName: String?,
        otherUserProfilePicture: String?
    ): String {

        val headerDoc = hashMapOf(
            "lastMsgText" to "",
            "forUserId" to forUserId,
            "otherUserId" to otherUserId,
            "lastMsgTimestamp" to null,
            "type" to "user",
            "unseenCount" to 0,
            "otherUser" to hashMapOf(
                "name" to otherUserName,
                "profilePic" to otherUserProfilePicture,
                "type" to "user",
                "unseenCount" to 0
            )
        )

        val docRef = firebaseDB
            .collection("chats")
            .document(uid)
            .collection("headers")
            .addOrThrow(headerDoc)

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


    private val _downloadChatAttachment = MutableLiveData<Lce<File>>()
    val downloadChatAttachment: LiveData<Lce<File>> = _downloadChatAttachment

    fun downloadAttachment(
        fullAttachmentPath: String,
        filesDir: File
    ) = viewModelScope.launch {
        _downloadChatAttachment.value = Lce.loading()

        try {

            val file = downloadAndSaveAttachment(fullAttachmentPath, filesDir)
            _downloadChatAttachment.value = Lce.content(file)
            _downloadChatAttachment.value = null
        } catch (e: Exception) {
            _downloadChatAttachment.value = Lce.error(e.message!!)
            _downloadChatAttachment.value = null

        }
    }

    private suspend fun downloadAndSaveAttachment(pdfDownloadLink: String, filesDir: File): File {
        val fileName: String = pdfDownloadLink.substring(
            pdfDownloadLink.lastIndexOf('/') + 1,
            pdfDownloadLink.length
        )

        val paySlipFile = File(filesDir, fileName)
        if (paySlipFile.exists()) {
            Log.d("PayslipMonthlyViewModel", "File Present in local")
            //File Present in Local
            return paySlipFile
        } else {
            val response = paySlipService.downloadPaySlip(pdfDownloadLink)

            if (response.isSuccessful) {
                val body = response.body()!!
                writeResponseBodyToDisk(body, paySlipFile)
                return paySlipFile
            } else {
                throw Exception("Unable to dowload payslip, ${response.message()}")
            }
        }
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, destFile: File): Boolean {
        return try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("DownloadPath", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }

    fun setMessagesUnseenCountToZero() = GlobalScope.launch{
        if(headerId.isBlank()){
            return@launch
        }

        try {
            firebaseDB.collection("chats")
                .document(uid)
                .collection("headers")
                .document(headerId)
                .updateOrThrow("unseenCount", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to set unseen count to zero",e)
        }
    }


}