package com.gigforce.modules.feature_chat.screens.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class NewContactsViewModel constructor(
    private val chatContactsRepository: ChatContactsRepository
) : ViewModel() {

    private val _contacts: MutableLiveData<List<ContactModel>> = MutableLiveData()
    val contacts: LiveData<List<ContactModel>> = _contacts

    private var contactsChangeListener: ListenerRegistration? = null



    fun startListeningForContactChanges() {

        contactsChangeListener = chatContactsRepository
            .getUserGigforceContacts()
            .addSnapshotListener { value, error ->
                error?.printStackTrace()

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

    companion object{

        private val TAG: String = "vm/chats/newcontact"
    }

}