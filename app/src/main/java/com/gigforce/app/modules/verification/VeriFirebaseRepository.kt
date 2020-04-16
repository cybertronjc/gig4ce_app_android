package com.gigforce.app.modules.verification

import android.util.Log
import com.gigforce.app.modules.verification.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class VeriFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    var collection = "Verification"

    fun setCardAvatar(cardAvatarName: String) {
        firebaseDB.collection(collection)
            .document(uid).update("cardAvatarName",cardAvatarName)
    }

    fun getVerificationData(): DocumentReference {
        return firebaseDB.collection(collection).document(uid)
    }

    fun setVeriContact(contacts: ArrayList<Address>) {
        for (contact in contacts) {
            firebaseDB.collection(collection)
                .document(uid).update("Contact", FieldValue.arrayUnion(contact))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener{
                        exception ->  Log.d("Repository", exception.toString())
                }
        }
    }

    fun setVeriDL(dls: ArrayList<DL>) {
        for (dl in dls) {
            firebaseDB.collection(collection)
                .document(uid).update("Contact", FieldValue.arrayUnion(dl))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener{
                        exception ->  Log.d("Repository", exception.toString())
                }
        }
    }

    fun setVeriVoterID(voterids: ArrayList<VoterID>) {
        for (voterid in voterids) {
            firebaseDB.collection(collection)
                .document(uid).update("Contact", FieldValue.arrayUnion(voterid))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener{
                        exception ->  Log.d("Repository", exception.toString())
                }
        }
    }

    fun setVeriPassport(passports: ArrayList<Passport>) {
        for (passport in passports) {
            firebaseDB.collection(collection)
                .document(uid).update("Contact", FieldValue.arrayUnion(passport))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener{
                        exception ->  Log.d("Repository", exception.toString())
                }
        }
    }

    fun setBank(banks: ArrayList<Bank>) {
        for (bank in banks) {
            firebaseDB.collection(collection)
                .document(uid).update("Bank", FieldValue.arrayUnion(bank))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener { exception ->
                    Log.d("Repository", exception.toString())
                }
        }
    }
}
