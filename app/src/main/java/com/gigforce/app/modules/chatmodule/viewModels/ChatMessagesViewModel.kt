package com.gigforce.app.modules.chatmodule.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.models.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class ChatMessagesViewModel: ViewModel() {

    private val TAG:String = "chats/viewmodel"
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var firebaseDB = FirebaseFirestore.getInstance()

    private var mapMessages: HashMap<String, MutableLiveData<ArrayList<Message>>> = hashMapOf()

    fun getChatMessagesLiveData(headerId: String):LiveData<ArrayList<Message>>
    {
        if(!mapMessages.containsKey(headerId)){
            Log.v(TAG, "headerId ${headerId} Not Found. Need to Initiate")
            initForHeader(headerId)
        }
        return mapMessages.get(headerId) as LiveData<ArrayList<Message>>
    }

    private fun getReference(headerId: String): CollectionReference
    {
        return firebaseDB.collection("chats")
            .document(uid)
            .collection("headers")
            .document(headerId)
            .collection("chat_messages")
    }

    private fun initForHeader(headerId:String) {
        Log.v(TAG, "InitForHeader: ${headerId}")
        if(!mapMessages.containsKey(headerId)) {
            mapMessages.set(headerId, MutableLiveData(ArrayList<Message>()))
        }

        /*
                Things to handle in this:
                - Status Change in Message
                - Lazy / Paginated Load
                - Performance Optimization for Change in Message (loop over Documents should not be everytime)
         */

        getReference(headerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                Log.v(TAG, "new Snapshot Received")
                Log.v(TAG, "${snapshot?.documents?.size} Documents")
                val newArray = ArrayList<Message>()
                snapshot?.let {
                    for(doc in it.documents) {
                        doc.toObject(Message::class.java)?.let {
                            newArray.add(it)
                        }
                    }
                }
                mapMessages[headerId]?.postValue(newArray)
            }
    }

    fun sendNewText(headerId:String?,
                    forUserId:String,
                    otherUserId: String,
                    text:String)
    {
        if(headerId.isNullOrEmpty()) {
            createHeader(forUserId, otherUserId)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        sendNewText(it.result?.id, forUserId, otherUserId, text)
                    }else{
                        Log.w("chats/headers", it.exception?.message?:"Some Error")
                    }
                }
        }else {

            val message = Message(
                headerId = headerId,
                forUserId = forUserId,
                otherUserId = otherUserId,
                flowType = "out",
                type = "text",
                content = text,
                timestamp = Timestamp.now()
            )

            getReference(headerId).add(message)
        }
    }

    private fun createHeader(forUserId: String, otherUserId: String): Task<DocumentReference> {

        val headerDoc = hashMapOf(
            "lastMsgText" to "",
            "forUserId" to forUserId,
            "otherUserId" to otherUserId,
            "lastMsgTimestamp" to null,
            "type" to "user",
            "unseenCount" to 0,
            "otherUser" to hashMapOf(
                "name" to "",
                "profilePic" to null,
                "type" to "user",
                "unseenCount" to 0
            )
        )

        return firebaseDB.collection("chats").document("uid")
            .collection("headers")
            .add(headerDoc)
    }
}