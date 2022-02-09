package com.gigforce.common_ui.chat

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.models.ChatHeader
import com.gigforce.common_ui.chat.models.ChatListItemDataObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatHeadersViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatGroupRepository: ChatGroupRepository
) : ViewModel() {

    private var _chatHeaders: MutableLiveData<List<ChatHeader>> = MutableLiveData()
    val chatHeaders: LiveData<List<ChatHeader>> = _chatHeaders

    private val _selectedChats : MutableLiveData<ArrayList<ChatListItemDataObject>> = MutableLiveData()
    val selectedChats: LiveData<ArrayList<ChatListItemDataObject>>
        get() = _selectedChats

    private val _isMultiSelectEnable: MutableLiveData<Boolean> = MutableLiveData()
    val isMultiSelectEnable: LiveData<Boolean> = _isMultiSelectEnable

    private val _unreadMessageCount: MutableLiveData<Int> = MutableLiveData()
    val unreadMessageCount: LiveData<Int> = _unreadMessageCount

    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()
    private var chatHeadersSnapshotListener: ListenerRegistration? = null
    var sharedFiles: Bundle? = null
    private var chatHeadersList: List<ChatHeader> = emptyList()
    private var currentSearchTerm: String? = null

    init {
        startWatchingChatHeaders()
    }

    fun startWatchingChatHeaders() {
        try {
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

                        chatHeadersList = messages
                        filterChatHeadersAndEmit()

                        var unreadMessageCount = 0
                        messages.forEach { chatHeader ->
                            unreadMessageCount += chatHeader.unseenCount

                            if (chatHeader.unseenCount != 0) {
                                Log.d("ChatHeaderViewModel","setting useencount")

                                if (chatHeader.chatType == ChatConstants.CHAT_TYPE_USER) {
                                    setMessagesAsDeliveredForChat(
                                        chatHeader.id,
                                        chatHeader.otherUserId
                                    )
                                } else if (chatHeader.chatType == ChatConstants.CHAT_TYPE_GROUP) {
                                    //set messages as delivered for group chat
                                    setMessagesAsDeliveredForGroupChat(chatHeader.groupId)
                                }
                            }
                        }
                        _unreadMessageCount.postValue(unreadMessageCount)
                    }
                }
        } catch (e: Exception){
            e.printStackTrace()
        }


    }
    private fun setMessagesAsDeliveredForGroupChat(
        groupId: String
    ) = GlobalScope.launch{
        try {
            Log.d("ChatHeaderViewModel","setting message as delivered of group :$groupId")

            val objs = chatGroupRepository.getGroupMessages(groupId)
            val messageWithNotDeliveredStatus = arrayListOf<String>()
            objs.forEach { it1 ->
                val chatMessageDeliveredTo = chatGroupRepository.getMessageDeliveredInfo(groupId, it1.id)
                if (chatMessageDeliveredTo.isEmpty()){
                    messageWithNotDeliveredStatus.add(it1.id)
                }
            }
            if (messageWithNotDeliveredStatus.isNotEmpty()){
                chatGroupRepository.markAsDelivered(groupId, messageWithNotDeliveredStatus)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setMessagesAsDeliveredForChat(
        chatHeader: String,
        otherUserId: String
    ) = GlobalScope.launch {

        try {
            Log.d("ChatHeaderViewModel","setting message as delivered of header :$chatHeader")
            chatRepository.sentMessagesSentMessageAsDelivered(chatHeader, otherUserId)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setHeadersMarkAsRead(
        chatHeaders: List<String>
    ) = GlobalScope.launch {
        try {
            chatRepository.setHeadersAsRead(chatHeaders, uid)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun setHeadersMuteNotifications(
        chatHeaders: List<String>,
        enable: Boolean
    ) = GlobalScope.launch {
        try {
            chatRepository.setHeaderMuteNotifications(chatHeaders, enable)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun filterChatList(newText: String) {
        currentSearchTerm = newText
        filterChatHeadersAndEmit()
    }

    private fun filterChatHeadersAndEmit() {
        if (currentSearchTerm.isNullOrBlank()) {
            _chatHeaders.postValue(chatHeadersList)
            return
        }

        val finalHeadersToShownOnView = chatHeadersList.filter {
            it.groupName.contains(
                currentSearchTerm!!, true
            )
                    || it.otherUser?.name?.contains(
                currentSearchTerm!!, true
            ) ?: false
                    || it.lastMsgText.contains(
                currentSearchTerm!!, true
            )
        }

        _chatHeaders.postValue(finalHeadersToShownOnView)
    }

    fun addOrRemoveChatFromSelectedList(chatHeader: ChatListItemDataObject){
        var list = _selectedChats.value
        if(list == null){
            list = ArrayList()
        }
        if (list.contains(chatHeader)){
            list.remove(chatHeader)
        }
        else{
            list.add(chatHeader)
        }
        _selectedChats.value = list
        filterChatHeadersAndEmit()
    }

    fun isChatSelected(chatHeader: ChatListItemDataObject): Boolean{
        return _selectedChats.value?.contains(chatHeader)?: false
    }

    fun isMultiSelectEnable(): Boolean? {
        return _isMultiSelectEnable.value
    }

    fun setMultiSelectEnable(enable: Boolean){
        _isMultiSelectEnable.value = enable
        filterChatHeadersAndEmit()
    }

    fun clearSelectedChats(){
        _selectedChats.value = null
    }

    fun getSelectedChats():ArrayList<ChatListItemDataObject> = _selectedChats.value?: ArrayList()
}