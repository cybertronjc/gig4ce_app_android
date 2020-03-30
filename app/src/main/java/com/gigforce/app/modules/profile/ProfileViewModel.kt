package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class ProfileViewModel: ViewModel() {

    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var Tags: MutableLiveData<TagData> = MutableLiveData<TagData>()
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

    fun getAllTags() {
        lateinit var tags:Array<String>
        FirebaseFirestore.getInstance().collection("Tags").limit(1).get()
            .addOnSuccessListener {
                if (it.isEmpty) {

                }
                else {
                    Tags.postValue(
                        it.documents[0].toObject(TagData::class.java)
                    )
                }
            }
    }

    fun addNewTag(tag:String) {
        profileFirebaseRepository.addNewTag(tag)
    }

    fun setProfileTag(tag: String) {
        profileFirebaseRepository.setProfileTags(tag)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        profileFirebaseRepository.setProfileEducation(education)
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        profileFirebaseRepository.setProfileSkill(skills)
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        profileFirebaseRepository.setProfileAchievement(achievements)
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        profileFirebaseRepository.setProfileContact(contacts)
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        profileFirebaseRepository.setProfileLanguage(languages)
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        profileFirebaseRepository.setProfileExperience(experiences)
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        profileFirebaseRepository.setProfileAvatarName(profileAvatarName)
    }


    init {
        uid = FirebaseAuth.getInstance().currentUser?.uid!!
        Log.d("ProfileViewModel", uid)
        //uid = "UeXaZV3KctuZ8xXLCKGF" // Test user
        getProfileData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "Profile View model destroying")
    }
}