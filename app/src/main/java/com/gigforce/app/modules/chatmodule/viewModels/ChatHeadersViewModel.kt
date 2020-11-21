package com.gigforce.app.modules.chatmodule.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatHeadersViewModel : ViewModel() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!

    private var firebaseDB = FirebaseFirestore.getInstance()

    private val chatHeaders: MutableLiveData<ArrayList<ChatHeader>> =
        MutableLiveData(ArrayList<ChatHeader>())

    // private var chatHeaderRepository = ChatHeaderFirebaseRepository()
    private var chatHeadersSnapshotListener: ListenerRegistration? = null

    val ChatHeaders: LiveData<ArrayList<ChatHeader>> get() = chatHeaders

    init {
        val reference = firebaseDB
            .collection("chats")
            .document(uid)
            .collection("headers")
            .orderBy("lastMsgTimestamp", Query.Direction.DESCENDING)

        chatHeadersSnapshotListener = reference
            .addSnapshotListener { querySnapshot, exception ->
                exception?.let {
                    Log.e("chatheaders/viewmodel", exception.message!!)
                    return@addSnapshotListener
                }

                // extract chatHeaders from querySnapshot
                querySnapshot?.let {
                    val tempChatHeaders: ArrayList<ChatHeader> = ArrayList<ChatHeader>()
                    for (doc in querySnapshot.documents) {
                        tempChatHeaders.add(
                            ChatHeader(
                                doc.id,
                                forUserId = doc.getString("forUserId") ?: "",
                                otherUserId = doc.getString("otherUserId") ?: "",
                                unseenCount = doc.getDouble("unseenCount")?.toInt() ?: 0,
                                lastMsgText = doc.getString("lastMsgText") ?: "",
                                lastMessageType = doc.getString("lastMessageType") ?: "",
                                lastMsgTimestamp = doc.getTimestamp("lastMsgTimestamp"),
                                otherUser = UserInfo(
                                    name = doc.getString("otherUser.name") ?: "",
                                    type = doc.getString("otherUser.type") ?: "",
                                    profilePic = doc.getString("otherUser.profilePic") ?: ""
                                )
                            )
                        )
                    }
                    chatHeaders.postValue(tempChatHeaders)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        chatHeadersSnapshotListener?.remove()
    }
}