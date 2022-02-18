package com.gigforce.modules.feature_chat.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewLiveLocationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : ViewModel() {

    companion object{
        const val TAG: String = "ViewLiveLocationViewModel"
    }

    var headerId: String = ""
    private var chatMessageListener: ListenerRegistration? = null

    private var _liveLocationMessage = MutableLiveData<ChatMessage?>()
    val liveLocationMessage: LiveData<ChatMessage?> = _liveLocationMessage

    fun addSnapshotToLiveLocationMessage(header: String, messageId: String){
        this.headerId = header

        if (!headerId.isBlank()) {
            //header is not blank
            Log.d(TAG, "header: $headerId")

            chatMessageListener = getReference(headerId).document(messageId).addSnapshotListener { value, error ->

                Log.d(GroupChatViewModel.TAG, "live location changed/subscribed, messageId - $messageId")

                error?.let {
                    CrashlyticsLogger.e(GroupChatViewModel.TAG, "In startWatchingLiveLocation()", it)
                }

                if (value != null){
                    val chatMessage = value.toObject(ChatMessage::class.java)
                    _liveLocationMessage.postValue(chatMessage)
                }
            }

        }
    }

    private fun getReference(headerId: String): CollectionReference {
        return getHeaderReference(headerId)
            .collection("chat_messages")
    }

    private fun getHeaderReference(headerId: String): DocumentReference {
        return firebaseFirestore.collection("chats")
            .document(firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid)
            .collection("headers")
            .document(headerId)
    }


    override fun onCleared() {
        super.onCleared()

        chatMessageListener?.remove()
        chatMessageListener = null

    }
}