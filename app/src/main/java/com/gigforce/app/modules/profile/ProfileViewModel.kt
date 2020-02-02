package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson

class ProfileViewModel: ViewModel() {

    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    val uid: String

    fun getProfileData(): MutableLiveData<ProfileData> {
        profileFirebaseRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> {
            value, e ->

            if (e != null) {
                Log.w("ProfileViewModel", "Listen failed", e)
                return@EventListener
            }

            Log.d("ProfileViewModel", value.toString())

            userProfileData.postValue(
                value!!.toObject(ProfileData::class.java)
            )
        })
        return userProfileData
    }

    fun setProfileData(education: ArrayList<Education>) {
        profileFirebaseRepository.setProfile(education)
    }

    init {
        //uid = FirebaseAuth.getInstance().currentUser?.uid!!
        uid = "UeXaZV3KctuZ8xXLCKGF" // Test user
        getProfileData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "Profile View model destroying")
    }
}