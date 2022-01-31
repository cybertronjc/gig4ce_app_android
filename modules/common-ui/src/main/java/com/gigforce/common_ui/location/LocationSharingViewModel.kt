package com.gigforce.common_ui.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSharingViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : ViewModel() {

    companion object{
        const val TAG: String = "ViewModelLocationSharing"
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

                Log.d(TAG, "live location changed/subscribed, messageId - $messageId")

                error?.let {
                    CrashlyticsLogger.e(TAG, "In startWatchingLiveLocation()", it)
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

    fun stopSharingLocation(header: String, messageId: String, receiverId: String) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                chatRepository.stopSharingLocation(header, messageId)
                chatRepository.stopReceiverSharingLocation(header, messageId, receiverId)

            } catch (e: Exception){
                Log.d(TAG, e.message.toString())
            }
        }
    }


    override fun onCleared() {
        super.onCleared()

        chatMessageListener?.remove()
        chatMessageListener = null

    }
}