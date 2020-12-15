package com.gigforce.app.modules.chatmodule.repository

import com.gigforce.app.BuildConfig
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.chatmodule.SyncPref
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.remote.SyncContactsService
import com.gigforce.app.utils.commitOrThrow
import com.gigforce.app.utils.getOrThrow
import com.gigforce.app.utils.network.RetrofitFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ChatContactsRepository constructor(
    private val syncPref: SyncPref,
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val syncContactsService: SyncContactsService = RetrofitFactory.generateSyncContactsService()
) : BaseFirestoreDBRepository() {

    private val currentUser: FirebaseUser by lazy {
        FirebaseAuth.getInstance().currentUser!!
    }

    private val userChatCollectionRef: DocumentReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHATS)
            .document(getUID())
    }

    private val userChatContactsCollectionRef: CollectionReference by lazy {
        userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }

    private val userChatHeadersCollectionRef: CollectionReference by lazy {
        userChatCollectionRef
            .collection(COLLECTION_HEADERS)
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    fun getUserContacts(): Query {
        return userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
            .whereEqualTo("isGigForceUser", true)
    }


    suspend fun updateContacts(newContacts: List<ContactModel>) {

        val querySnap = getUserContacts().getOrThrow()
        val oldContacts = querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }

        val batch = db.batch()
        for (contact in oldContacts) {
            val contactMatch = newContacts.find { it.mobile == contact.mobile }

            if (contactMatch == null) {
                //user has removed that contact add to remove batch
                val contactRef = userChatContactsCollectionRef.document(contact.mobile)
                batch.delete(contactRef)

                //Updating names with nos in headers
                if (contact.uid != null) {
                    val userChatHeaders = getChatHeadersForUser(contact.uid!!)
                    userChatHeaders.forEach {
                        val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
                        batch.update(chatHeaderRef, "otherUser.name", contact.mobile)
                    }
                }
            } else {
                //Check if user has renamed the contact
                if (contactMatch.name != contact.name) {
                    val contactRef = userChatContactsCollectionRef.document(contact.mobile)
                    batch.update(contactRef, "name", contactMatch.name)

                    //Updating names in headers
                    if (contact.uid != null) {
                        val userChatHeaders = getChatHeadersForUser(contact.uid!!)
                        userChatHeaders.forEach {
                            val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
                            batch.update(chatHeaderRef, "otherUser.name", contactMatch.name)
                        }
                    }
                }
            }
        }

        val newAddedContacts =
            newContacts.filter { newContact -> oldContacts.find { it.mobile == newContact.mobile } == null }
        if (newAddedContacts.isNotEmpty()) {

            val newAddedContactsFiltered = newAddedContacts.filter {

                if (it.mobile.length == 12) {
                    if (currentUser.phoneNumber == null) {
                        true
                    } else {
                        //remove user if user has no equal to current user
                        !currentUser.phoneNumber!!.contains(it.mobile)
                    }
                } else {
                    true
                }
            }

            if (newAddedContactsFiltered.isNotEmpty()) {

                val currentUserChatHeaders = getChatHeadersForUser(currentUser.uid)
                newAddedContactsFiltered.forEach { pickedContact ->
                    val matchedHeader = currentUserChatHeaders.find { header ->
                        header.otherUser?.name?.contains(pickedContact.mobile) ?: false
                    }
                    if (matchedHeader != null) {
                        //There's header wihtout name
                        val chatHeaderRef = userChatHeadersCollectionRef.document(matchedHeader.id)
                        batch.update(chatHeaderRef, "otherUser.name", pickedContact.name)
                    }

                    val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
                    batch.set(contactRef, pickedContact)
                }
            }
        }

        batch.commitOrThrow()
        val response = syncContactsService.startSyncingUploadedContactsWithGigforceUsers(
            apiUrl = BuildConfig.SYNC_CHAT_CONTACTS_URL,
            uid = getUID()
        )

        if (!response.isSuccessful) {
            throw Exception("Unable to sync contacts, ${response.message()}")
        }

        syncPref.setContactsAsSynced()
    }

    private suspend fun getChatHeadersForUser(uid: String): List<ChatHeader> {
        val userChatHeaders = userChatHeadersCollectionRef
            .whereEqualTo("otherUserId", uid)
            .getOrThrow()

        if (userChatHeaders.isEmpty)
            return emptyList()
        else {
            val headers = userChatHeaders.documents.map { docSnap ->
                docSnap.toObject(ChatHeader::class.java)!!.apply {
                    id = docSnap.id
                }
            }

            return headers
        }
    }

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_HEADERS = "headers"
    }

}