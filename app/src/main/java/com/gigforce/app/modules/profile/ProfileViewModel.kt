package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.profile.models.Skill
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

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

            Log.d("ProfileViewModel", userProfileData.toString())
        })
        return userProfileData
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        profileFirebaseRepository.setProfileEducation(education)
    }

    fun setProfileSkill(skills: ArrayList<Skill>) {
        profileFirebaseRepository.setProfileSkill(skills)
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