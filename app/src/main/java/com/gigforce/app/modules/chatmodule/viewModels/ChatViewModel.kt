package com.gigforce.app.modules.chatmodule.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.core.toDate
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.chatmodule.repository.ChatFirebaseRepository
import com.gigforce.app.modules.chatmodule.repository.ChatHeaderFirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentChange
import java.time.LocalDateTime

class ChatViewModel: ViewModel() {

    companion object {
        fun newInstance() = ChatViewModel()
    }

    var chatHeaderRepository = ChatHeaderFirebaseRepository()
    var chatFirebaseRepository = ChatFirebaseRepository()

    var chatHeaders: MutableLiveData<ArrayList<ChatHeader>> = MutableLiveData(ArrayList<ChatHeader>())
    var chatMsgs: MutableLiveData<ArrayList<Message>> = MutableLiveData(ArrayList<Message>())

    val uid = FirebaseAuth.getInstance().currentUser?.uid!!

    fun sendNewMsg(
        chatHeaderId: String, message: String, time: LocalDateTime, otherUserId: String) {
        val newMsg = Message(
            headerId = chatHeaderId,
            forUserId = uid,
            flowType = "out",
            timestamp = Timestamp(time.toDate),
            type = "text",
            content = message,
            otherUserId = otherUserId
        )

        chatFirebaseRepository.addChatMsg(chatHeaderId, newMsg)
    }

    fun getChatHeaders() {
        chatHeaderRepository.getChatHeaders()
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                querySnapshot?.documentChanges?.forEach {
                    when (it.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            chatHeaders.value!!.add(it.document.toObject(ChatHeader::class.java))
                            chatHeaders.value = chatHeaders.value
                        }
                        // TODO: See how to handled modified document
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {

                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            chatHeaders.value!!.remove(it.document.toObject(ChatHeader::class.java))
                            chatHeaders.value = chatHeaders.value
                        }
                    }
                }
        }
    }

    fun markChatMsgsRead(chatMsgs: ArrayList<Message>) {
        chatMsgs.filter {
            it.status == 1 && it.flowType == "in"
        }.forEach {
            it.status = 2
            chatFirebaseRepository.updateMsg(it.headerId, it)
        }
    }

    fun getChatMsgs(chatHeaderId: String) {
        chatMsgs.value = ArrayList()
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

//        val presenceRef = Firebase.database.getReference(uid)
//        presenceRef.onDisconnect().setValue("Offline")

        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatViewModel", "Listener cancelled")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("ChatViewModel", "User connected")
                } else {
                    Log.d("ChatViewModel", "User disconnected")
                }
            }
        })
    }
}