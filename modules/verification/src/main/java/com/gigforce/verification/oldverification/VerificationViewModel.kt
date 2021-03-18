package com.gigforce.verification.oldverification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class VerificationViewModel: ViewModel() {

    var veriFirebaseRepository =
        VeriFirebaseRepository()
    var veriData: MutableLiveData<KYCdata> = MutableLiveData<KYCdata>()
    val uid: String

    fun setCardAvatarName(cardAvatarName: String) {
        veriFirebaseRepository.setCardAvatar(cardAvatarName)
    }

    fun getVerificationData(): MutableLiveData<KYCdata> {
        veriFirebaseRepository.getVerificationData().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->

            if (e != null) {
                Log.w("VerificationViewModel", "Listen failed", e)
                return@EventListener
            }

            Log.d("VerificationViewModel", value.toString())

            veriData.postValue(
                value!!.toObject(KYCdata::class.java)
            )

            Log.d("VerificationViewModel", veriData.toString())
        })
        return veriData
    }

    fun setVerificationContact(contacts: ArrayList<Address>) {
        veriFirebaseRepository.setVeriContact(contacts)
    }

    fun setVerificationDL(dls: ArrayList<DL>) {
        veriFirebaseRepository.setVeriDL(dls)
    }

    fun setVerificationVoterID(voterids: ArrayList<VoterID>) {
        veriFirebaseRepository.setVeriVoterID(voterids)
    }

    fun setVerificationPassport(passports: ArrayList<Passport>) {
        veriFirebaseRepository.setVeriPassport(passports)
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
        Log.d("VerificationViewModel", "verification View model destroying")
    }
}