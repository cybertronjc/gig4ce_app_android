package com.gigforce.app.modules.verification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.verification.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class VerificationViewModel: ViewModel() {

    var veriFirebaseRepository = VeriFirebaseRepository()
    var veriData: MutableLiveData<Address> = MutableLiveData<Address>()
    val uid: String

    fun setCardAvatarName(cardAvatarName: String) {
        veriFirebaseRepository.setCardAvatar(cardAvatarName)
    }

    fun getVerificationData(): MutableLiveData<Address> {
        veriFirebaseRepository.getVerificationData().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->

            if (e != null) {
                Log.w("VerificationViewModel", "Listen failed", e)
                return@EventListener
            }

            Log.d("VerificationViewModel", value.toString())

            veriData.postValue(
                value!!.toObject(Address::class.java)
            )

            Log.d("VerificationViewModel", veriData.toString())
        })
        return veriData
    }

    fun setVerificationContact(contacts: ArrayList<Address>) {
        veriFirebaseRepository.setVeriContact(contacts)
    }

    fun setBank(banks: ArrayList<Bank>) {
        veriFirebaseRepository.setBank(banks)
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