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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val mutex = Mutex()
    private var currentBatchSize = 0
    private var batch = db.batch()

    suspend fun updateContacts(newContacts: List<ContactModel>) = mutex.withLock {
        Log.d(TAG, "Sync Started")
        batch = db.batch()

        val oldContacts = getUsersAlreadyUploadedContacts()
        for (i in oldContacts.indices) {
            val contact = oldContacts[i]
            val contactMatch = newContacts.find { it.mobile == contact.mobile }

            if (contactMatch == null) {
                //user has removed that phone contacts add to remove batch
                userHasDeletedContactFromPhoneRemoveFromDB(contact)

                //Updating names with phone numbers in headers
                if (contact.uid != null) {
                    updateHeadersForDeletedContact(contact)
                }
            } else {
                if (contactMatch.name != contact.name) {
                    //user has renamed the contact
                    updateContactsName(contact, contactMatch)

                    //Updating names in headers
                    if (contact.uid != null) {
                        updateContactNameInHeader(contact, contactMatch)
                    }
                }
            }
        }

        val newAddedContacts = newContacts.filter { newContact ->
            oldContacts.find { it.mobile == newContact.mobile } == null
        }

        if (newAddedContacts.isNotEmpty()) {

            val newAddedContactsFiltered = filterNewAddedContacts(newAddedContacts)
            if (newAddedContactsFiltered.isNotEmpty()) {

                val currentUserChatHeaders = getChatHeadersOfCurrentUser()
                for (i in newAddedContactsFiltered.indices) {
                    val pickedContact = newAddedContactsFiltered[i]

                    //Search current headers and update if user is in headers
                    updateChatHeaderIfUserIsInHeaders(currentUserChatHeaders, pickedContact)
                    addContactToUsersContactList(pickedContact)
                }
            }
        }

        if (currentBatchSize != 0) {
            batch.commitOrThrow()
        }

        Log.d(TAG, "Final Commit batch")
        val response = syncContactsService.startSyncingUploadedContactsWithGigforceUsers(
            apiUrl = BuildConfig.SYNC_CHAT_CONTACTS_URL,
            uid = getUID()
        )

        if (!response.isSuccessful) {
            //todo: Badgateway Error, Sync took time or something.
            // todo: Progress bar still running after error came.
            // todo: syncing is taking a lot of time.
            throw Exception("Unable to sync contacts, ${response.message()}")
        }
        syncPref.setContactsAsSynced()
    }

    private suspend fun addContactToUsersContactList(
        pickedContact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
        batch.set(contactRef, pickedContact)
        Log.d(TAG, "set Query Added")
        checkBatchForOverFlowAndCommit()
    }

    private suspend fun updateChatHeaderIfUserIsInHeaders(
        currentUserChatHeaders: List<ChatHeader>,
        pickedContact: ContactModel
    ) {
        val matchedHeader = currentUserChatHeaders.find { header ->
            header.otherUser?.name?.contains(pickedContact.mobile) ?: false
        }
        if (matchedHeader != null) {
            //There's header without name
            val chatHeaderRef = userChatHeadersCollectionRef.document(matchedHeader.id)
            batch.update(chatHeaderRef, "otherUser.name", pickedContact.name)
            Log.d(TAG, "update Query Added")

            checkBatchForOverFlowAndCommit()
        }
    }

    private fun filterNewAddedContacts(newAddedContacts: List<ContactModel>): List<ContactModel> {
        return newAddedContacts.filter {

            if (it.mobile.length == 12) {
                if (currentUser.phoneNumber == null) {
                    true
                } else {
                    //remove user if user has phone-number equal to current user's phone number
                    !currentUser.phoneNumber!!.contains(it.mobile)
                }
            } else {
                true
            }
        }
    }

    private suspend fun updateContactNameInHeader(
        contact: ContactModel,
        contactMatch: ContactModel
    ) {
        val userChatHeaders = getChatHeadersForUser(contact.uid!!)
        userChatHeaders.forEach {
            val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
            batch.update(chatHeaderRef, "otherUser.name", contactMatch.name)
            Log.d(TAG, "update Query Added")

            checkBatchForOverFlowAndCommit()
        }
    }

    private suspend fun updateContactsName(
        contact: ContactModel,
        contactMatch: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(contact.mobile)
        batch.update(contactRef, "name", contactMatch.name)
        Log.d(TAG, "update Query Added")

        checkBatchForOverFlowAndCommit()
    }

    private suspend fun updateHeadersForDeletedContact(
        contact: ContactModel
    ) {
        val userChatHeaders = getChatHeadersForUser(contact.uid!!)
        userChatHeaders.forEach {
            val chatHeaderRef = userChatHeadersCollectionRef.document(it.id)
            batch.update(chatHeaderRef, "otherUser.name", contact.mobile)
            Log.d(TAG, "update Query Added")

            checkBatchForOverFlowAndCommit()
        }
    }

    private suspend fun userHasDeletedContactFromPhoneRemoveFromDB(
        contact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(contact.mobile)
        batch.delete(contactRef)
        Log.d(TAG, "Del Query Added")

        checkBatchForOverFlowAndCommit()
    }

    private suspend fun getUsersAlreadyUploadedContacts(): List<ContactModel> {
        val querySnap = getUserContacts().getOrThrow()
        return querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }
    }

    private suspend fun checkBatchForOverFlowAndCommit() {
        currentBatchSize++
        Log.d(TAG, "Size updated to $currentBatchSize")

        if (currentBatchSize > 480) {
            //   batchArray.add(batch)
            batch.commitOrThrow()
            currentBatchSize = 0

            batch = db.batch()
            Log.d(TAG, "New Batch $batch")
        }
    }

    private suspend fun getChatHeadersForUser(uid: String): List<ChatHeader> {
        val userChatHeaders = userChatHeadersCollectionRef
            .whereEqualTo("otherUserId", uid)
            .getOrThrow()

        return if (userChatHeaders.isEmpty)
            emptyList()
        else {
            val headers = userChatHeaders.documents.map { docSnap ->
                docSnap.toObject(ChatHeader::class.java)!!.apply {
                    id = docSnap.id
                }
            }

            headers
        }
    }

    private suspend fun getChatHeadersOfCurrentUser(): List<ChatHeader> {
        val userChatHeaders = userChatHeadersCollectionRef
            .getOrThrow()

        return if (userChatHeaders.isEmpty)
            emptyList()
        else {
            val headers = userChatHeaders.documents.map { docSnap ->
                docSnap.toObject(ChatHeader::class.java)!!.apply {
                    id = docSnap.id
                }
            }

            headers
        }
    }


    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_HEADERS = "headers"
        const val TAG = "ChatContactsBatch"
    }

}