package com.gigforce.app.modules.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.profile.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    companion object {
        fun newInstance() = ProfileViewModel()
    }

    var profileAppBarExpanded = false
    var listener: ListenerRegistration? = null
    var profileID: String = ""
    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var Tags: MutableLiveData<TagData> = MutableLiveData<TagData>()
    lateinit var uid: String
    var query: Query? = null


    fun getProfileData(): MutableLiveData<ProfileData> {

        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener(fun(
                value: DocumentSnapshot?,
                e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    Log.w("ProfileViewModel", "Listen failed", e)
                    return
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value!!.data.toString())
                    val obj = value!!.toObject(ProfileData::class.java)
                    obj?.id = value.id;
                    userProfileData.value = obj
                    Log.d("ProfileViewModel", userProfileData.toString())
                }
            }))


        return userProfileData
    }

    fun getAllTags() {
        FirebaseFirestore.getInstance().collection("Tags").limit(1).get()
            .addOnSuccessListener {
                if (it.isEmpty) {

                } else {
                    Tags.postValue(
                        it.documents[0].toObject(TagData::class.java)
                    )
                }
            }
    }

    fun addNewTag(tag: String) {
        profileFirebaseRepository.addNewTag(tag)
    }

    fun removeProfileTag(tags: ArrayList<String>) {
        profileFirebaseRepository.removeProfileTag(tags)
    }

    fun setProfileTag(tags: ArrayList<String>) {
        profileFirebaseRepository.setProfileTags(tags)
    }

    fun setProfileEducation(education: Education) {
        profileFirebaseRepository.setData(education)
    }

    fun setProfileExperience(experience: Experience) {
        profileFirebaseRepository.setData(experience)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        profileFirebaseRepository.setProfileEducation(education)
    }

    fun removeProfileEducation(education: Education) {
        profileFirebaseRepository.removeData(education)
    }

    fun setProfileSkill(skill: Skill) {
        profileFirebaseRepository.setData(skill)
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        profileFirebaseRepository.setProfileSkill(skills)
    }

    fun removeProfileSkill(skill: Skill) {
        profileFirebaseRepository.removeData(skill)
    }

    fun setProfileAchievement(achievement: Achievement) {
        profileFirebaseRepository.setData(achievement)
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        profileFirebaseRepository.setProfileAchievement(achievements)
    }

    fun removeProfileAchievement(achievement: Achievement) {
        profileFirebaseRepository.removeData(achievement)
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        profileFirebaseRepository.setProfileContact(contacts)
    }

    fun setProfileLanguage(language: Language) {
        profileFirebaseRepository.setData(language)
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        profileFirebaseRepository.setProfileLanguage(languages)
    }

    fun removeProfileLanguage(language: Language) {
        profileFirebaseRepository.removeData(language)
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        profileFirebaseRepository.setProfileExperience(experiences)
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        profileFirebaseRepository.setProfileAvatarName(profileAvatarName)
    }

    fun removeProfileExperience(experience: Experience) {
        profileFirebaseRepository.removeData(experience)
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

    private val _viewState = MutableLiveData<ProfileViewStates>()
    val viewState: LiveData<ProfileViewStates> = _viewState

    fun setUserAsAmbassador() = viewModelScope.launch {

        try {
            _viewState.postValue(SettingUserAsAmbassador)

            profileFirebaseRepository.setUserAsAmbassador()
            _viewState.postValue(UserSetAsAmbassadorSuccessfully)
        } catch (e: Exception) {
            e.printStackTrace()

            _viewState.postValue(
                ErrorWhileSettingUserAsAmbassador(
                    e.message ?: "Error while setting user as Ambassador"
                )
            )
        }
    }

}