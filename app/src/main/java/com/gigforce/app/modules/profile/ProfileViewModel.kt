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
import kotlinx.coroutines.tasks.await

class   ProfileViewModel: ViewModel() {

    companion object {
        fun newInstance() = ProfileViewModel()
    }

    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var Tags: MutableLiveData<TagData> = MutableLiveData<TagData>()
    lateinit var uid: String

    fun getProfileData(): MutableLiveData<ProfileData> {
        profileFirebaseRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> {
            value, e ->

            if (e != null) {
                Log.w("ProfileViewModel", "Listen failed", e)
                return@EventListener
            }

            if (value!!.data == null) {
                profileFirebaseRepository.createEmptyProfile()
            }
            else {

                Log.d("ProfileViewModel", value!!.data.toString())

                userProfileData.postValue(
                    value!!.toObject(ProfileData::class.java)
                )

                Log.d("ProfileViewModel", userProfileData.toString())
            }
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

    fun removeProfileTag(tags: ArrayList<String>) {
        profileFirebaseRepository.removeProfileTag(tags)
    }

    fun setProfileTag(tags: ArrayList<String>) {
        profileFirebaseRepository.setProfileTags(tags)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        profileFirebaseRepository.setProfileEducation(education)
    }

    fun removeProfileEducation(education: Education) {
        profileFirebaseRepository.removeProfileEducation(education)
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        profileFirebaseRepository.setProfileSkill(skills)
    }

    fun removeProfileSkill(skill: String) {
        profileFirebaseRepository.removeProfileSkill(skill)
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        profileFirebaseRepository.setProfileAchievement(achievements)
    }

    fun removeProfileAchievement(achievement: Achievement) {
        profileFirebaseRepository.removeProfileAchievement(achievement)
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        profileFirebaseRepository.setProfileContact(contacts)
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        profileFirebaseRepository.setProfileLanguage(languages)
    }

    fun removeProfileLanguage(language: Language) {
        profileFirebaseRepository.removeProfileLanguage(language)
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        profileFirebaseRepository.setProfileExperience(experiences)
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        profileFirebaseRepository.setProfileAvatarName(profileAvatarName)
    }

    fun removeProfileExperience(experience: Experience) {
        profileFirebaseRepository.removeProfileExperience(experience)
    }

    fun setProfileBio(bio: String) {
        profileFirebaseRepository.setProfileBio(bio)
    }

    fun setProfileAboutMe(aboutMe: String) {
        profileFirebaseRepository.setProfileAboutMe(aboutMe)
    }

    init {
        uid = FirebaseAuth.getInstance().currentUser?.uid!!
        Log.d("ProfileViewModel", uid)
    }

    override fun onCleared() {
        super.onCleared()
    }
}