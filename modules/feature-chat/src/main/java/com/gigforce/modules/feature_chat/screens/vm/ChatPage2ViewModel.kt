package com.gigforce.modules.feature_chat.screens.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.modules.feature_chat.models.ChatMessage
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatPage2ViewModel: ViewModel() {

    var headerId:String = ""
    var uid:String = ""


    private val collectionReference = FirebaseFirestore.getInstance().collection("chats")

    private var _messages:MutableLiveData<ArrayList<ChatMessage>> = MutableLiveData()
    val messages: LiveData<ArrayList<ChatMessage>> = _messages

    var messagesListener:ListenerRegistration? = null

    fun startObservingMessages(){
        messagesListener = collectionReference
                .document(uid)
                .collection("headers")
                .document(headerId)
                .collection("chat_messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    value?.let {
                        val loadedMessages = it.documents.map {
                            it.toObject(ChatMessage::class.java)!!.apply {
                                this.id = it.id
                            }
                        }
                        _messages.value = ArrayList(loadedMessages)
                    }
                }
    }


    override fun onCleared() {
        super.onCleared()
        messagesListener ?.remove()
    }
}