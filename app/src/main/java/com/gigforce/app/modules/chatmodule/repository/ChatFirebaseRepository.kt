package com.gigforce.app.modules.chatmodule.repository

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
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
}