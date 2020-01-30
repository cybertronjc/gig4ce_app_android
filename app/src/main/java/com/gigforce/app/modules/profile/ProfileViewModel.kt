package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel: ViewModel() {

    var profileData:MutableLiveData<Map<String, Any>> = MutableLiveData<Map<String, Any>>()
    var UserProfile: ProfileData

    init {

        //val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

        val db = FirebaseFirestore.getInstance()
        db.document("user_profiles/$uid")
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                firebaseFirestoreException ?.let {
                    Log.e("/profile/viewmodel", it.message, it)
                }

                documentSnapshot ?.let {
                    Log.d("/profile/viewmodel", "profile updated, ${it.data}")
                    profileData.postValue(it.data)
                }
            }

        val docRef = db.collection("user_profiles").document(uid)
        docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("Doc Status", "${document.data}")
                    }
                    else {
                        Log.d("Doc Status", "no document for $uid")
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d("Doc Status", "get failed with ", exception)
                }
        // load user data
        UserProfile = ProfileData(id="ysharma")
        UserProfile.SetRating(2.0F)
    }

}