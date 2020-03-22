package com.gigforce.app.modules.verification

import com.gigforce.app.modules.verification.VeriFirebaseRepository
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.verification.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class VerificationViewModel: ViewModel() {

    var veriFirebaseRepository = VeriFirebaseRepository()
    var veriData: MutableLiveData<Contact_Verification> = MutableLiveData<Contact_Verification>()
    val uid: String

    fun getVerificationData(): MutableLiveData<Contact_Verification> {
        veriFirebaseRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->

            if (e != null) {
                Log.w("VerificationViewModel", "Listen failed", e)
                return@EventListener
            }

            Log.d("VerificationViewModel", value.toString())

            veriData.postValue(
                value!!.toObject(Contact_Verification::class.java)
            )

            Log.d("VerificationViewModel", veriData.toString())
        })
        return veriData
    }

    fun setVerificationContact(contacts: ArrayList<Contact_Verification>) {
        veriFirebaseRepository.setVeriContact(contacts)
    }

    init {
        uid = FirebaseAuth.getInstance().currentUser?.uid!!
        //uid = "UeXaZV3KctuZ8xXLCKGF" // Test user
        getVerificationData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("VerificationViewModel", "Profile View model destroying")
    }
}