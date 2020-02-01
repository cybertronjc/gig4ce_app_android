package com.gigforce.app.modules.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    //var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun getProfile(): DocumentReference {
        return firebaseDB.collection("user_profiles").document(uid)
    }
}