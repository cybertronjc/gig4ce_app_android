package com.gigforce.app.utils.dbrepository.test

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.*
import com.gigforce.app.utils.dbrepository.test.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class TestViewModel: ViewModel() {
    companion object {
        var EDUCATION = "Education"
        var SKILL = "Skill"
        var ACHIEVEMENT = "Achievement"
        var LANGUAGE = "Language"
        var EXPERIENCE = "Experience"
        var PROFILETAGS = "Tags"
        var PROFILEAVATARNAME = "profileAvatarName"
        var CONTACT = "Contact"
    }

    var profileFirebaseRepository = TestFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()

    fun getProfileData(): MutableLiveData<ProfileData> {
        profileFirebaseRepository.getDBCollection().addSnapshotListener(EventListener<DocumentSnapshot> {
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

    fun removeProfileTag(tag: String) {
        profileFirebaseRepository.removeData(PROFILETAGS,tag)
    }

    fun setProfileTag(tag: String) {
        profileFirebaseRepository.setData(PROFILETAGS,tag)
    }

    fun setProfileEducation(education: ArrayList<EducationDataModel>) {
        profileFirebaseRepository.setData(education)
    }

    fun removeProfileEducation(education: EducationDataModel) {
        profileFirebaseRepository.setData(education)
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        profileFirebaseRepository.setData(SKILL,skills)
    }

    fun removeProfileSkill(skill: String) {
        profileFirebaseRepository.removeData(SKILL,skill)
    }

    fun setProfileAchievement(achievements: ArrayList<AchievementDataModel>) {
        profileFirebaseRepository.setData(achievements)
    }



    fun removeProfileAchievement(achievement: AchievementDataModel) {
        profileFirebaseRepository.setData(achievement)
    }

    fun setProfileContact(contacts: ArrayList<ContactDataModel>) {
        profileFirebaseRepository.setData(contacts)
    }

    fun setProfileLanguage(languages: ArrayList<LanguageDataModel>) {
        profileFirebaseRepository.setData(languages)
    }

    fun setProfileExperience(experiences: ArrayList<ExperienceDataModel>) {
        profileFirebaseRepository.setData(experiences)
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        profileFirebaseRepository.setData(PROFILEAVATARNAME,profileAvatarName)
    }

    fun removeProfileExperience(experience: ExperienceDataModel) {
        profileFirebaseRepository.removeData(experience)
    }

    init {
        getProfileData()
    }

    override fun onCleared() {
        super.onCleared()
    }
}