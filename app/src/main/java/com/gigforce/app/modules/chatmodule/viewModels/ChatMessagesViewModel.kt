package com.gigforce.app.modules.chatmodule.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.utils.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ChatMessagesViewModel constructor(
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    private val TAG: String = "chats/viewmodel"
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()

    var headerId: String = ""
    var otherUserName: String? = null
    var otherUserProfilePicture: String? = null
    lateinit var forUserId: String
    lateinit var otherUserId: String

    private var mapMessages: HashMap<String, MutableLiveData<ArrayList<Message>>> = hashMapOf()

    fun getChatMessagesLiveData(): LiveData<ArrayList<Message>> {

        if (!mapMessages.containsKey(headerId)) {
            Log.v(TAG, "headerId ${headerId} Not Found. Need to Initiate")
            initForHeader()

            Log.v(TAG, "getChatMessagesLiveData: ${headerId}")
            if (!mapMessages.containsKey(headerId)) {
                mapMessages.set(headerId, MutableLiveData(ArrayList<Message>()))
            }
        }
        return mapMessages.get(headerId) as LiveData<ArrayList<Message>>
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
                    val newArray = ArrayList<Message>()

                    snapshot?.let {
                        for (doc in it.documents) {
                            doc.toObject(Message::class.java)?.let {
                                newArray.add(it)
                            }
                        }
                    }
                    mapMessages[headerId]?.postValue(newArray)
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
    ) = viewModelScope.launch {

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

    fun sendNewImageMessage(
            text: String = "",
            uri: Uri
    ) = viewModelScope.launch {

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

            val docReference = getReference(headerId).addOrThrow(message)
            val pathOnServer = uploadChatAttachment("image.jpg", uri)
            updatePathInMessage(docReference.id, pathOnServer)
            updatePathInOtherUserMessageToo(docReference.id, pathOnServer)
        } catch (e: Exception) {
            //handle error
        }
    }

    private suspend fun updatePathInOtherUserMessageToo(id: String, pathOnServer : String) {
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
                    .updateOrThrow("attachmentPath", pathOnServer)
        }
    }

    fun sendNewVideoMessage(
            text: String = "",
            uri: Uri
    ) = viewModelScope.launch {

        try {
            if (headerId.isEmpty()) {
                headerId = createHeader(
                        forUserId,
                        otherUserId,
                        otherUserName,
                        otherUserProfilePicture
                )
                saveHeaderIdToContact(otherUserId, headerId)
                initForHeader()
            }

            val message = Message(
                    headerId = headerId,
                    forUserId = forUserId,
                    otherUserId = otherUserId,
                    flowType = "out",
                    type = Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT,
                    content = text,
                    timestamp = Timestamp.now(),
                    attachmentPath = null
            )

            val docReference = getReference(headerId).addOrThrow(message)
//            val pathOnServer = uploadChatAttachment(fileName, uri)
//            updatePathInMessage(docReference.id, pathOnServer)
        } catch (e: Exception) {
            //handle error
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

    private fun prepareUniqueImageName(fileNameWithExtension: String): String {

        val extension = fileNameWithExtension.substringBeforeLast('.')
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        return "$forUserId$timeStamp$extension"
    }

    private suspend fun updatePathInMessage(id: String, pathOnServer: String) {
        getReference(headerId).document(id).updateOrThrow("attachmentPath", pathOnServer)
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


}