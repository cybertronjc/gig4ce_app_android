package com.gigforce.app.modules.chatmodule.repository

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatHeaderFirebaseRepository: BaseFirestoreDBRepository() {

    // private var firebaseDB = FirebaseFirestore.getInstance()
    private var COLLECTION_NAME = "chat_headers"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    fun getChatHeaders(uid:String): Query {
        return getCollectionReference().whereEqualTo("forUserId", uid)
    }
}