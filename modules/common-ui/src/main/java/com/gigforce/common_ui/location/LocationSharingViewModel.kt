package com.gigforce.common_ui.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.chat.ChatGroupRepository
import com.gigforce.common_ui.chat.ChatRepository
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.viewdatamodels.chat.ChatHeader
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
    private val groupChatRepository: ChatGroupRepository,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : ViewModel() {

    companion object{
        const val TAG: String = "ViewModelLocationSharing"
    }

    var headerId: String = ""
    var groupId: String = ""
    private var chatMessageListener: ListenerRegistration? = null
    private var headerInfoChangeListener: ListenerRegistration? = null
    private var groupDetailsListener: ListenerRegistration? = null

    private var _headerInfo = MutableLiveData<ChatHeader>()
    val headerInfo: LiveData<ChatHeader> = _headerInfo

    private var groupDetails: ChatGroup? = null

    private val _groupInfo: MutableLiveData<ChatGroup> = MutableLiveData()
    val groupInfo: LiveData<ChatGroup> = _groupInfo

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

     fun startListeningForHeaderChanges(header: String) {
         this.headerId = header
        if (headerId.isBlank()) {
            return
        }

        headerInfoChangeListener = getHeaderReference(headerId)
            .addSnapshotListener { snapshot, error ->
                Log.d(TAG,"header info changed/subscribed, $headerId")

                snapshot?.let {
                    val chatHeader = it.toObject(ChatHeader::class.java)!!.apply {
                        id = it.id
                    }
                    _headerInfo.value = chatHeader

                }
            }
    }

    fun startWatchingGroupDetails(groupId: String) {
        this.groupId = groupId
        if (groupDetailsListener != null) {
            Log.d(TAG, "already a listener attached,no-op")
            return
        }

        groupDetailsListener = groupChatRepository.getGroupDetailsRef(groupId)
            .addSnapshotListener { data, error ->
                Log.d(TAG, "group details changed/subscribed, groupId - $groupId")

                error?.let {
                    CrashlyticsLogger.e(TAG, "In startWatchingGroupDetails()", it)
                }

                if (data != null) {
                    groupDetails = data.toObject(ChatGroup::class.java)?.apply {
                        this.id = data.id
                    }
                    _groupInfo.value = groupDetails
                }
            }

    }

    fun addSnapShotToGroupLiveLocationMessage(groupId: String, messageId: String){
        this.groupId = groupId

        if (!groupId.isBlank()) {
            //header is not blank
            Log.d(TAG, "groupId: $groupId")

            chatMessageListener = getGroupChatReference(groupId).document(messageId).addSnapshotListener { value, error ->

                Log.d(TAG, "group live location changed/subscribed, messageId - $messageId")

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

    private fun getGroupChatReference(groupId: String): CollectionReference {
        return firebaseFirestore.collection("chat_groups").document(groupId).collection("group_messages")
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

    fun stopSharingGroupLocation(header: String, messageId: String) = GlobalScope.launch {
        if (header.isNotEmpty() && messageId.isNotEmpty()){
            try {
                groupChatRepository.stopSharingLocation(header, messageId)

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