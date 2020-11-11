package com.gigforce.app.modules.chatmodule.repository

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.service.ContactsDiffUtilCallback
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

class ChatContactsRepository : BaseFirestoreDBRepository() {

    private val userChatCollectionRef : DocumentReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHATS)
            .document(getUID())
    }

    private val userChatContactsCollectionRef : CollectionReference by lazy {
        userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    fun getUserContacts(): CollectionReference {
        return userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }

    suspend fun updateContacts(contacts: List<ContactModel>) {

        val batch = db.batch()

        contacts.forEach {
            val contactRef = userChatContactsCollectionRef.document(it.mobile)
            batch.set(contactRef, it)
        }

        batch.commit()
    }


    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
    }

}