package com.gigforce.modules.feature_chat.repositories

import android.util.Log
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.commitOrThrow
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.modules.feature_chat.service.SyncPref
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.regex.Pattern

class ChatContactsRepository constructor(
    private val syncPref: SyncPref
) : BaseFirestoreDBRepository() {

    private var chatContactsRemoteService: DownloadChatAttachmentService =
        RetrofitFactory.createService(
            DownloadChatAttachmentService::class.java
        )

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

    private val profileDocRefCollectionRef: DocumentReference by lazy {
        userChatCollectionRef
            .collection(COLLECTION_PROFILE)
            .document(getUID())
    }

    private var profileDocSnap: DocumentSnapshot? = null
    private suspend fun checkIfUserTl(): Boolean {

        return if (profileDocSnap != null) {
            profileDocSnap!!.getBoolean("isUserTl") ?: false
        } else {
            try {
                profileDocSnap = profileDocRefCollectionRef.getOrThrow()
                profileDocSnap!!.getBoolean("isUserTl") ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    fun getUserGigforceContacts(): Query {
        return userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
            .whereEqualTo("isGigForceUser", true)
    }

    fun getUserAllContacts(): Query {
        return userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }

    private val mutex = Mutex()
    private var currentBatchSize = 0
    private var batch = db.batch()
    private var numbersOnlyRegEx = "^[0-9]*$"

    suspend fun updateContacts(
        contacts: List<ContactModel>,
        shouldCallSyncApiWhenDoneUploadingApiToDB: Boolean
    ) = mutex.withLock {
        if (!syncPref.shouldSyncContacts()) {
            Log.d(TAG, "Avoiding contact update last sync was less than 30 secs ago...")

            if(shouldCallSyncApiWhenDoneUploadingApiToDB)
                callSyncContactsApi()

            return
        }

        Log.d(TAG, "Sync Started...")
        syncPref.addContactSyncStartedPoint()

        val isUserTl = checkIfUserTl()
        Log.d(TAG, "Is UserTl : $isUserTl")

        val newContacts = filterContactsForIllegalMobileNos(contacts)
        batch = db.batch()

        val oldContacts = getUsersAlreadyUploadedContacts()
        Log.d(TAG, "Got ${oldContacts.size} old Contacts From Server")

        for (i in oldContacts.indices) {
            val oldContact = oldContacts[i]
            val contactMatchInNewList = newContacts.find { it.mobile == oldContact.mobile }

            if (contactMatchInNewList == null) {
                //user has removed that phone contacts add to remove batch

                if (!isUserTl) {

                    //Wont Delete Contacts in case of TL
                    userHasDeletedContactFromPhoneRemoveFromDB(oldContact)
                }
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

        if(shouldCallSyncApiWhenDoneUploadingApiToDB) {
            callSyncContactsApi()
        }
    }

    private suspend fun callSyncContactsApi() {
            try {
                chatContactsRemoteService.trySyncingContacts(currentUser.uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        if (pickedContact.mobile.isBlank()) return

        val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
        batch.set(contactRef, pickedContact)
        Log.d(TAG, "${pickedContact.mobile} - Added")

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
        batch.update(contactRef, mapOf("name" to newContact.name, "updatedAt" to Timestamp.now(), "updatedBy" to StringConstants.APP.value))
        Log.d(TAG, "${oldContact.mobile} - Updating, new contact-name : ${newContact.name}")

        checkBatchForOverFlowAndCommit()
    }

    private suspend fun userHasDeletedContactFromPhoneRemoveFromDB(
        contact: ContactModel
    ) {
        val contactRef = userChatContactsCollectionRef.document(contact.mobile)
        batch.delete(contactRef)
        Log.d(TAG, "${contact.mobile} : Deleted")

        checkBatchForOverFlowAndCommit()
    }

    private suspend fun getUsersAlreadyUploadedContacts(): List<ContactModel> {
        val querySnap = getUserAllContacts().getOrThrow()
        return querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }
    }

    private suspend fun checkBatchForOverFlowAndCommit() {
        currentBatchSize++

        if (currentBatchSize > 480) {
            //   batchArray.add(batch)
            batch.commitOrThrow()
            currentBatchSize = 0

            batch = db.batch()
        }
    }

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
        const val COLLECTION_HEADERS = "headers"
        const val COLLECTION_PROFILE = "Profiles"
        const val TAG = "ChatContactsBatch"
    }
}