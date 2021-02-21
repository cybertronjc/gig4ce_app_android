package com.gigforce.app.modules.chatmodule.repository

import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.ChatReportedUser
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.core.utils.EventLogs.addOrThrow
import com.gigforce.core.utils.EventLogs.getOrThrow
import com.gigforce.core.utils.EventLogs.updateOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ChatRepository constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : BaseChatRepository() {

    private val userChatCollectionRef: DocumentReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHATS)
            .document(getUID())
    }

    private val userReportedCollectionRef: CollectionReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(ChatGroupRepository.COLLECTION_CHAT_REPORTED_USER)
    }

    suspend fun getChatHeader(chatHeaderId: String): ChatHeader {
        val docRef = userChatCollectionRef
            .collection(COLLECTION_CHAT_HEADERS)
            .document(chatHeaderId)
            .getOrThrow()

        return docRef.toObject(ChatHeader::class.java)!!.apply {
            id = docRef.id
        }
    }

    suspend fun blockOrUnblockUser(
        chatHeaderId: String
    ) {
        val chatHeader = getChatHeader(chatHeaderId)

        userChatCollectionRef
            .collection(COLLECTION_CHAT_HEADERS)
            .document(chatHeaderId)
            .updateOrThrow("isBlocked", !chatHeader.isBlocked)
    }

    suspend fun reportAndBlockUser(
        chatHeaderId: String,
        otherUserId: String,
        reason: String
    ) {
        userChatCollectionRef
            .collection(COLLECTION_CHAT_HEADERS)
            .document(chatHeaderId)
            .updateOrThrow("isBlocked", true)

        userReportedCollectionRef
            .addOrThrow(
                ChatReportedUser(
                    id = null,
                    reportedUserUid = otherUserId,
                    reportedBy = getUID(),
                    reportedOn = Timestamp.now(),
                    reportingReason = reason
                )
            )
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_GROUP_CHATS = "chat_groups"
        const val COLLECTION_GROUP_MESSAGES = "group_messages"
        const val COLLECTION_CHAT_HEADERS = "headers"
    }

}