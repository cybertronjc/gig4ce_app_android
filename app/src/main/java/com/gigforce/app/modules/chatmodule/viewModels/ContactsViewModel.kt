package com.gigforce.app.modules.chatmodule.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.repository.ChatContactsRepository
import com.gigforce.app.utils.Lse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch

class ContactsViewModel constructor(
    private val chatContactsRepository: ChatContactsRepository
) : ViewModel() {

    private val TAG: String = "vm/chats/newcontact"
    var contactsSynced = false

    private val _contacts: MutableLiveData<List<ContactModel>> = MutableLiveData()
    val contacts: LiveData<List<ContactModel>> = _contacts

    private var contactsChangeListener: ListenerRegistration? = null

    init {
        subscribe()
    }

    private val _syncContacts: MutableLiveData<Lse> = MutableLiveData()
    val syncContacts: LiveData<Lse> = _syncContacts

    fun syncContacts(contacts: List<ContactModel>) = viewModelScope.launch {
        _syncContacts.value = Lse.loading()

        try {
            Log.d(TAG, "syncing Contacts...")
            chatContactsRepository.updateContacts(contacts)
            contactsSynced = true

            _syncContacts.value = Lse.success()
            _syncContacts.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error while syncing contacts", e)
            _syncContacts.value = Lse.error(e.message ?: "Unable to sync contacts")
            _syncContacts.value = null
        }
    }

    private fun subscribe() {
        contactsChangeListener = chatContactsRepository
            .getUserContacts()
            .addSnapshotListener { value, error ->

                if (value != null)
                    convertToContactsAndEmit(value)
            }
    }

    private fun convertToContactsAndEmit(value: QuerySnapshot?) {
        val querySnap = value ?: return

        val contacts = querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }.sortedBy { it.name }

        Log.d(TAG, "emitting values...")
        _contacts.postValue(contacts)
    }

    override fun onCleared() {
        super.onCleared()
        contactsChangeListener?.remove()
        contactsChangeListener = null
    }

}