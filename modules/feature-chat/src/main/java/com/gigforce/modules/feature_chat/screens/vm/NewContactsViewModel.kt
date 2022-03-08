package com.gigforce.modules.feature_chat.screens.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.useCases.ForwardMessagesUseCase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ContactAndGroupViewEffects {

    object ForwardingMessages : ContactAndGroupViewEffects()

    object MessagesForwarded : ContactAndGroupViewEffects()

    data class ErrorWhileForwardingMessage(
        val error: String
    ) : ContactAndGroupViewEffects()
}

@HiltViewModel
class NewContactsViewModel @Inject constructor(
    private val chatContactsRepository: ChatContactsRepository,
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val forwardMessagesUseCase: ForwardMessagesUseCase,
    private val logger: GigforceLogger
) : ViewModel() {

    private val _contacts: MutableLiveData<List<ContactModel>> = MutableLiveData()
    val contacts: LiveData<List<ContactModel>> = _contacts

    private var contactsChangeListener: ListenerRegistration? = null

    private val currentUser: FirebaseUser
        get() {
            return firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
        }

    private val _viewEffects = MutableLiveData<ContactAndGroupViewEffects>()
    val viewEffects = _viewEffects.asFlow()


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

    fun forwardMessage(
        forwardChat: ChatMessage,
        contactsList: List<ContactModel>
    ) = GlobalScope.launch {

        if (contactsList.isEmpty()) {
            logger.d(TAG, "forwardMessage() : contactList found empty, no-op")
            return@launch
        }


        try {
            _viewEffects.postValue(ContactAndGroupViewEffects.ForwardingMessages)
            logger.d(TAG, "forwarding messages , message : $forwardChat, contacts : $contactsList")

            forwardMessagesUseCase.forwardMessages(
                listOf(forwardChat),
                contactsList
            )
            _viewEffects.postValue(ContactAndGroupViewEffects.MessagesForwarded)
        } catch (e: Exception) {
            _viewEffects.postValue(
                ContactAndGroupViewEffects.ErrorWhileForwardingMessage(
                    "unable to forward messages"
                )
            )
            logger.e(TAG, "error while forwarding messages", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        contactsChangeListener?.remove()
        contactsChangeListener = null
    }

    companion object {

        private val TAG: String = "vm/chats/newcontact"
    }

}