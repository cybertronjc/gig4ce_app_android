package com.gigforce.modules.feature_chat.useCases

import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
import com.gigforce.core.extensions.commitOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwardMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val chatContactsRepository: ChatContactsRepository,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) {

    suspend fun forwardMessages(
        messagesToForward: List<ChatMessage>,
        forwardTo: List<ContactModel>
    ) {

        val messages = messagesToForward.map {
            it.cloneForForwarding()
        }

        checkForExistingHeaderElseCreateHeadersForUser(
            forwardTo
        )

        val forwardMessageBatch = firebaseFirestore.batch()
        forwardTo.forEach { contactInfo ->

            messages.onEach {
                it.headerId = it.headerId
                it.receiverInfo = UserInfo(
                    id = contactInfo.uid!!,
                    mobileNo = contactInfo.mobile
                )
            }

            addForwardMessageToBatch(
                messages,
                contactInfo,
                forwardMessageBatch
            )
        }
        forwardMessageBatch.commitOrThrow()
    }

    private suspend fun checkForExistingHeaderElseCreateHeadersForUser(
        contactsWithOutHeaderId: List<ContactModel>
    ) {
        contactsWithOutHeaderId.onEach {
            if(it.headerId != null) return@onEach

            val headerId = chatRepository.checkAndReturnIfHeaderIsPresentInchat(
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                it.uid!!
            )
            it.headerId = headerId
        }

        val createHeaderBatch = firebaseFirestore.batch()
        contactsWithOutHeaderId.onEach {
            if (it.headerId != null) return@onEach

            addCreateSenderHeaderInWriteBatch(
                createHeaderBatch,
                it
            )
            addCreateReceiverHeaderInWriteBatch(
                createHeaderBatch,
                it
            )
            it.headerId = it.uid!!
        }
        createHeaderBatch.commitOrThrow()
    }

    private suspend fun addCreateReceiverHeaderInWriteBatch(
        batch: WriteBatch,
        contact: ContactModel
    ) {
        val currentUser = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
        val currentUserProfile = profileFirebaseRepository.getProfileDataIfExist(currentUser.uid)
        val currentUserInReceiverChatContacts = chatContactsRepository.getContactDetailsFromChatContacts(
            contact.uid!!,
            currentUser.uid
        )

        val chatHeader = ChatHeader(
            forUserId = contact.uid!!,
            otherUserId = currentUser.uid,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = currentUser.uid,
                name = currentUserInReceiverChatContacts?.name ?: currentUserProfile?.name ?: "",
                profilePic = currentUserProfile?.getFullProfilePicPathThumbnail() ?: "",
                type = "user",
                mobileNo = currentUserProfile?.loginMobile ?: ""
            ),
            lastMsgFlowType = ""
        )

        val docRef = firebaseFirestore
            .collection(ChatRepository.COLLECTION_CHATS)
            .document(contact.uid!!)
            .collection(ChatRepository.COLLECTION_CHAT_HEADERS)
            .document(currentUser.uid)

        batch.set(docRef,chatHeader)
    }


    private fun addCreateSenderHeaderInWriteBatch(
        batch: WriteBatch,
        contact : ContactModel
    ) {
        val currentUser = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()

        val chatHeader = ChatHeader(
            forUserId = currentUser.uid,
            otherUserId = contact.uid!!,
            lastMsgTimestamp = null,
            chatType = ChatConstants.CHAT_TYPE_USER,
            unseenCount = 0,
            otherUser = UserInfo(
                id = "",
                name = contact.name ?: contact.profileName ?: "",
                profilePic = contact.getUserProfileImageUrlOrPath() ?: "",
                type = "user",
                mobileNo = contact.mobile ?: ""
            ),
            lastMsgFlowType = ""
        )

        val docRef = firebaseFirestore
            .collection(ChatRepository.COLLECTION_CHATS)
            .document(currentUser.uid)
            .collection(ChatRepository.COLLECTION_CHAT_HEADERS)
            .document(contact.uid!!)

        batch.set(docRef,chatHeader)
    }

    private fun addForwardMessageToBatch(
        messages: List<ChatMessage>,
        forwardTo: ContactModel,
        batch: WriteBatch
    ) {
        messages.forEach {
            val messageId = UUID.randomUUID().toString()
            it.id = messageId
            val newDocumentRef = chatRepository.getChatMessagesCollectionRef(forwardTo.headerId!!).document(messageId)
            batch.set(newDocumentRef, it)
        }
    }

}