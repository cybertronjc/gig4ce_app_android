package com.gigforce.app.modules.chatmodule.repository

import android.util.Log
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
import com.google.firebase.firestore.*
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

    private var currentBatchSize = 0
    private var batch = db.batch()
    private var batchArray : MutableList<WriteBatch> = mutableListOf()

    suspend fun updateContacts(newContacts: List<ContactModel>) {
        Log.d(TAG,"Sync Started")
        // Log.d(TAG,"New Batch ${batch}")

        val querySnap = getUserContacts().getOrThrow()
        val oldContacts = querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }

        for (i in oldContacts.indices) {
            val contact = oldContacts[i]
            val contactMatch = newContacts.find { it.mobile == contact.mobile }

            if (contactMatch == null) {
                //user has removed that contact add to remove batch
                val contactRef = userChatContactsCollectionRef.document(contact.mobile)
                batch.delete(contactRef)
       //         Log.d(TAG,"Del Query Added")

                checkBatchForOverFlowAndCommit()

                //Updating names with nos in headers
                if (contact.uid != null) {
                    val userChatHeaders = getChatHeadersForUser(contact.uid!!)
                    userChatHeaders.forEach {
                        val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
                        batch.update(chatHeaderRef, "otherUser.name", contact.mobile)
                        //              Log.d(TAG,"update Query Added")

                        checkBatchForOverFlowAndCommit()
                    }
                }
            } else {
                //Check if user has renamed the contact
                if (contactMatch.name != contact.name) {
                    val contactRef = userChatContactsCollectionRef.document(contact.mobile)
                    batch.update(contactRef, "name", contactMatch.name)
                    //       Log.d(TAG,"update Query Added")

                    checkBatchForOverFlowAndCommit()

                    //Updating names in headers
                    if (contact.uid != null) {
                        val userChatHeaders = getChatHeadersForUser(contact.uid!!)
                        userChatHeaders.forEach {
                            val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
                            batch.update(chatHeaderRef, "otherUser.name", contactMatch.name)
                            //      Log.d(TAG,"update Query Added")

                            checkBatchForOverFlowAndCommit()
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

                val currentUserChatHeaders = getChatHeadersOfCurrentUser()

                for (i in newAddedContactsFiltered.indices) {
                    val pickedContact = newAddedContactsFiltered[i]
                    val matchedHeader = currentUserChatHeaders.find { header ->
                        header.otherUser?.name?.contains(pickedContact.mobile) ?: false
                    }
                    if (matchedHeader != null) {
                        //There's header wihtout name
                        val chatHeaderRef = userChatHeadersCollectionRef.document(matchedHeader.id)
                        batch.update(chatHeaderRef, "otherUser.name", pickedContact.name)
                        //             Log.d(TAG,"update Query Added")

                        checkBatchForOverFlowAndCommit()
                    }

                    val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
                    batch.set(contactRef, pickedContact)
                    Log.d(TAG,"set Query Added")
                    checkBatchForOverFlowAndCommit()
                }
            }
        }

        batchArray.forEach {
            it.commitOrThrow()
        }
        Log.d(TAG,"Final Commit batch")
        val response = syncContactsService.startSyncingUploadedContactsWithGigforceUsers(
            apiUrl = BuildConfig.SYNC_CHAT_CONTACTS_URL,
            uid = getUID()
        )

        if (!response.isSuccessful) {
            throw Exception("Unable to sync contacts, ${response.message()}")
        }

        syncPref.setContactsAsSynced()
    }

    private suspend fun checkBatchForOverFlowAndCommit() {
        currentBatchSize++
        Log.d(TAG,"Size updates to $currentBatchSize")

        if (currentBatchSize > 50) {
            //     Log.d(TAG,"Commiting batch")
            batchArray.add(batch)
            //Reseting batch
            //    Log.d(TAG,"Reset size to 0")
            currentBatchSize = 0

            //      Log.d(TAG,"New Batch")
            batch = db.batch()
            //   Log.d(TAG,"New Batch ${batch}")
        }

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

    private suspend fun getChatHeadersOfCurrentUser(): List<ChatHeader> {
        val userChatHeaders = userChatHeadersCollectionRef
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
        const val TAG = "ChatContactsBatch"
    }

}