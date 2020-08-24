package com.gigforce.app.modules.chatmodule.repository

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.chatmodule.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class ChatFirebaseRepository: BaseFirestoreDBRepository() {

    private var firebaseDB = FirebaseFirestore.getInstance()
    private var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    override fun getCollectionName(): String {
        return "chat_msgs"
    }

    fun getChatMsgs(chatHeaderId: String): CollectionReference {
        return firebaseDB.collection("chat_headers")
            .document(chatHeaderId).collection("chat_msgs")
    }

    fun addChatMsg(chatHeaderId: String, message: Message) {
        firebaseDB.collection("chat_headers")
            .document(chatHeaderId)
            .collection("chat_msgs")
            .add(message)
    }

    fun updateMsg(chatHeaderId: String, message: Message) {
        firebaseDB.collection("chat_headers")
            .document(chatHeaderId)
            .collection("chat_msgs")
            .document(message.id)
            .update(mapOf(
                "id" to message.id,
                "headerId" to message.headerId,
                "forUserId" to message.forUserId,
                "otherUserId" to message.otherUserId,
                "flowType" to message.flowType,
                "timestamp" to message.timestamp,
                "status" to message.status,
                "type" to message.type,
                "content" to message.content
            ))
    }
}