package com.gigforce.app.modules.chatmodule.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.repository.ChatContactsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class ContactsViewModel constructor(
    private val chatContactsRepository: ChatContactsRepository = ChatContactsRepository()
) : ViewModel() {

    private val TAG: String = "vm/chats/newcontact"

    private val uid by lazy {
        FirebaseAuth.getInstance().uid
    }

    private val _contacts: MutableLiveData<List<ContactModel>> = MutableLiveData()
    val contacts: LiveData<List<ContactModel>> = _contacts

    private var contactList: List<ContactModel> = emptyList()
    private var contactListLoaded = false
    private var contactsChangeListener: ListenerRegistration? = null

    init {

        if (contactsChangeListener == null)
            subscribe()
        else{
            //
        }
    }

    private fun subscribe() {

        contactsChangeListener = chatContactsRepository
            .getUserContacts()
            .addSnapshotListener { value, error ->

                if (error != null) {

                } else {
                    contactListLoaded = true
                    convertToContactsAndEmit(value)
                }
            }
    }

    private fun convertToContactsAndEmit(value: QuerySnapshot?) {
        val querySnap = value ?: return

        val contacts = querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }.filter { it.isGigForceUser }

        Log.d(TAG,"emitting values...")
        _contacts.postValue(contacts)
    }

    override fun onCleared() {
        super.onCleared()
        contactsChangeListener?.remove()
        contactsChangeListener = null
    }

}