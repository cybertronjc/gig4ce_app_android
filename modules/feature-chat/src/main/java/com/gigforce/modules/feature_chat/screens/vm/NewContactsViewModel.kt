package com.gigforce.modules.feature_chat.screens.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewContactsViewModel @Inject constructor(
    private val chatContactsRepository: ChatContactsRepository,
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : ViewModel() {

    private val _contacts: MutableLiveData<List<ContactModel>> = MutableLiveData()
    val contacts: LiveData<List<ContactModel>> = _contacts

    private var contactsChangeListener: ListenerRegistration? = null


    private val currentUser : FirebaseUser
        get() {
        return firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
    }
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

//    fun forwardMessage(forwardChat: ChatMessage, contactsList: List<ContactModel>)= viewModelScope.launch {
//        Log.d("forward", "true")
//        try {
//
//            if (contactsList.isNotEmpty()) {
//                //create header if not exists
//                contactsList.forEach { it1 ->
//                    var newHeaderId = ""
//                    if (it1.headerId == null) {
//                        createHeaderWithContactsForBothUsers(
//                            currentUser?.uid,
//                            it1.uid.toString(),
//                            it1.getUserProfileImageUrlOrPath().toString(),
//                            it1.profileName.toString()
//                        )
//                        Log.d("headerId", "new $newHeaderId")
//                        it1.headerId = newHeaderId
//                    }
//
//                    forwardChat?.let { it ->
//                        it.senderInfo = UserInfo(
//                            id = currentUser.uid,
//                            mobileNo = currentUser.phoneNumber!!
//                        )
//                        it.receiverInfo = UserInfo(
//                            id = otherUserId
//                        )
//                        it.flowType = "out"
//                        it.timestamp = Timestamp.now()
//                    }
//                }
//
//                chatRepository.forwardChatMessage(contactsList, forwardChat)
//            }
//        } catch (e: Exception){
//            Log.d("forward", "error: ${e.message}")
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        contactsChangeListener?.remove()
        contactsChangeListener = null
    }

    companion object{

        private val TAG: String = "vm/chats/newcontact"
    }

}