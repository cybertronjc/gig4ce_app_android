package com.gigforce.common_ui.chat

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.models.ChatHeader
//import com.gigforce.modules.feature_chat.core.ChatConstants
//import com.gigforce.modules.feature_chat.models.ChatHeader
//import com.gigforce.modules.feature_chat.repositories.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatHeadersViewModel constructor(
        private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private var _chatHeaders: MutableLiveData<List<ChatHeader>> = MutableLiveData()
    val chatHeaders: LiveData<List<ChatHeader>> = _chatHeaders

    private val _unreadMessageCount: MutableLiveData<Int> = MutableLiveData()
    val unreadMessageCount: LiveData<Int> = _unreadMessageCount

    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()
    private var chatHeadersSnapshotListener: ListenerRegistration? = null
    var sharedFiles : Bundle? = null

    init {
        startWatchingChatHeaders()
    }

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
                    querySnapshot?.let {
                        Log.e("chat/header/viewmodel", "Data Loaded from Server")

                        val messages = it.documents.map { docSnap ->
                            docSnap.toObject(ChatHeader::class.java)!!.apply {
                                this.id = docSnap.id
                            }
                        }

                        _chatHeaders.postValue(messages)

                        var unreadMessageCount = 0
                        messages.forEach { chatHeader ->
                            unreadMessageCount += chatHeader.unseenCount

                            if (chatHeader.unseenCount != 0) {

                                if (chatHeader.chatType == ChatConstants.CHAT_TYPE_USER) {
                                    setMessagesAsDeliveredForChat(
                                            chatHeader.id,
                                            chatHeader.otherUserId
                                    )
                                }
                            }
                        }
                        _unreadMessageCount.postValue(unreadMessageCount)
                    }
                }
    }

    private fun setMessagesAsDeliveredForChat(
            chatHeader: String,
            otherUserId: String
    ) = GlobalScope.launch {

        try {

                chatRepository.sentMessagesSentMessageAsDelivered(chatHeader, otherUserId)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}