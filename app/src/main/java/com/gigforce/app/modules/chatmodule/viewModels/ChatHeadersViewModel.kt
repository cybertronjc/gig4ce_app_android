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

    // private var chatHeaderRepository = ChatHeaderFirebaseRepository()
    private var chatHeadersSnapshotListener: ListenerRegistration? = null

    private val _chatHeaders: MutableLiveData<ArrayList<ChatHeader>> =
        MutableLiveData(ArrayList<ChatHeader>())
    val chatHeaders: LiveData<ArrayList<ChatHeader>> get() = _chatHeaders

    private val _unreadMessageCount: MutableLiveData<Int> = MutableLiveData()
    val unreadMessageCount: LiveData<Int> = _unreadMessageCount

    fun startWatchingChatHeaders() {
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
                                chatType = doc.getString("chatType") ?: "",
                                lastMessageType = doc.getString("lastMessageType") ?: "",
                                lastMsgTimestamp = doc.getTimestamp("lastMsgTimestamp"),
                                groupId = doc.getString("groupId") ?: "",
                                groupName = doc.getString("groupName") ?: "",
                                groupAvatar = doc.getString("groupAvatar") ?: "",
                                otherUser = UserInfo(
                                    name = doc.getString("otherUser.name") ?: "",
                                    type = doc.getString("otherUser.type") ?: "",
                                    profilePic = doc.getString("otherUser.profilePic") ?: ""
                                )
                            )
                        )
                    }
                    _chatHeaders.postValue(tempChatHeaders)

                    var unreadMessageCount = 0
                    tempChatHeaders.forEach {
                        unreadMessageCount += it.unseenCount
                    }

                    _unreadMessageCount.postValue(unreadMessageCount)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        chatHeadersSnapshotListener?.remove()
    }
}