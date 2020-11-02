package com.gigforce.app.modules.chatmodule.viewModels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.profile.models.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatNewContactViewModel:ViewModel() {

    private val TAG: String = "vm/chats/newcontact"
    private val uid = FirebaseAuth.getInstance().uid

    val fbContactsActive: MutableLiveData<ArrayList<ContactModel>> = MutableLiveData()
    val fbContactsAll: MutableLiveData<ArrayList<ContactModel>> = MutableLiveData()

    init {
        subscribe()
    }

    private fun subscribe() {
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(uid)
            .collection("contacts")
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.documents?.let {
                        val list: ArrayList<ContactModel> = ArrayList()
                        it.forEach { doc ->
                            val contact = doc.toObject(ContactModel::class.java)
                            contact?.let {
                                list.add(contact)
                            }
                        }
                        fbContactsAll.postValue(list)
                    }
                }
            }
        // Complete!
    }

    fun uploadContacts(list:ArrayList<ContactModel>) {

    }
}