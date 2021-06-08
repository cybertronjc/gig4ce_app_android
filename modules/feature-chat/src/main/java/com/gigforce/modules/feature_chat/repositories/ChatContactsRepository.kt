package com.gigforce.modules.feature_chat.repositories

import android.util.Log
import com.gigforce.core.extensions.commitOrThrow
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.modules.feature_chat.service.SyncPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.regex.Pattern

class ChatContactsRepository constructor(
        private val syncPref: SyncPref
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
    private var numbersOnlyRegEx = "^[0-9]*$"

    suspend fun updateContacts(contacts: List<ContactModel>) = mutex.withLock {
        Log.d(TAG, "Sync Started...")
        val newContacts = filterContactsForIllegalMobileNos(contacts)
        batch = db.batch()

        val oldContacts = getUsersAlreadyUploadedContacts()
        Log.d(TAG, "Got ${oldContacts.size} old Contacts From Server")

        for (i in oldContacts.indices) {
            val oldContact = oldContacts[i]
            val contactMatchInNewList = newContacts.find { it.mobile == oldContact.mobile }

            if (contactMatchInNewList == null) {
                //user has removed that phone contacts add to remove batch
                userHasDeletedContactFromPhoneRemoveFromDB(oldContact)
            } else {
                if (contactMatchInNewList.name != oldContact.name) {
                    //user has renamed the contact
                    updateContactsName(oldContact, contactMatchInNewList)
                }
            }
        }

        val newAddedContacts = newContacts.filter { newContact ->
            oldContacts.find { it.mobile == newContact.mobile } == null
        }

        val newAddedContactsFiltered = filterNewAddedContacts(newAddedContacts)
        Log.d(TAG, "User has added  ${newAddedContactsFiltered.size} contacts from last sync.")

        newAddedContactsFiltered.forEach {
            addContactToUsersContactList(it)
        }

        if (currentBatchSize != 0) {
            batch.commitOrThrow()
        }

        syncPref.setContactsAsSynced()
    }

    private fun filterContactsForIllegalMobileNos(contacts: List<ContactModel>): List<ContactModel> {
        val patterns = Pattern.compile(numbersOnlyRegEx)

        return contacts.filter {
            patterns.matcher(it.mobile).matches()
        }
    }

    private suspend fun addContactToUsersContactList(
            pickedContact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
        batch.set(contactRef, pickedContact)

        checkBatchForOverFlowAndCommit()
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

    private suspend fun updateContactsName(
            oldContact: ContactModel,
            newContact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(oldContact.mobile)
        batch.update(contactRef, "name", newContact.name)
        Log.d(TAG, "Updating User Contact : ${oldContact.mobile}, to ${newContact.name}")

        checkBatchForOverFlowAndCommit()
    }

    private suspend fun userHasDeletedContactFromPhoneRemoveFromDB(
            contact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(contact.mobile)
        batch.delete(contactRef)
        Log.d(TAG, "User Deleted Contact : ${contact.mobile}")

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

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_HEADERS = "headers"
        const val TAG = "ChatContactsBatch"
    }
}