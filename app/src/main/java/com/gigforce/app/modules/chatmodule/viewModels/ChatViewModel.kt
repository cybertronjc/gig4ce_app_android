package com.gigforce.app.modules.chatmodule.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.chatmodule.repository.ChatFirebaseRepository
import com.gigforce.app.modules.chatmodule.repository.ChatHeaderFirebaseRepository
import com.google.firestore.v1.DocumentChange

class ChatViewModel: ViewModel() {

    companion object {
        fun newInstance() = ChatViewModel()
    }

    var chatHeaderRepository = ChatHeaderFirebaseRepository()
    var chatFirebaseRepository = ChatFirebaseRepository()

    var chatHeaders: MutableLiveData<ArrayList<ChatHeader>> = MutableLiveData(ArrayList<ChatHeader>())
    var chatMsgs: MutableLiveData<ArrayList<Message>> = MutableLiveData(ArrayList<Message>())

    fun getChatHeaders() {
        chatHeaderRepository.getChatHeaders()
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                querySnapshot?.documentChanges?.forEach {
                    when (it.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            chatHeaders.value!!.add(it.document.toObject(ChatHeader::class.java))
                        }
                        // TODO: See how to handled modified document
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {

                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            chatHeaders.value!!.remove(it.document.toObject(ChatHeader::class.java))
                        }
                    }
                }
        }
    }

    fun getChatMsgs(chatHeaderId: String) {
        chatFirebaseRepository.getChatMsgs(chatHeaderId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                querySnapshot?.documentChanges?.forEach {
                    when (it.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            chatMsgs.value!!.add(it.document.toObject(Message::class.java))
                        }

                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            // TODO implement this
                        }

                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            chatMsgs.value!!.remove(it.document.toObject(Message::class.java))
                        }
                    }
                }

            }
    }
    init {
        getChatHeaders()
    }
}