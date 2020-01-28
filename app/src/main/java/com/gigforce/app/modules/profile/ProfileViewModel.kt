package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    var profileData: MutableLiveData<Map<String, Any>> = MutableLiveData<Map<String, Any>>()

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!

        FirebaseFirestore.getInstance()
            .document("user_profiles/$uid")
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                firebaseFirestoreException ?.let {
                    Log.e("/profile/viewmodel", it.message, it)
                }

                documentSnapshot ?.let {
                    Log.d("/profile/viewmodel", "profile updated, ${it.data}")
                    profileData.postValue(it.data)
                }
            }
    }
}