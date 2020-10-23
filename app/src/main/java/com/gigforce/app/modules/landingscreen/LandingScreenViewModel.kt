package com.gigforce.app.modules.landingscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.landingscreen.models.Role
import com.gigforce.app.modules.landingscreen.models.Tip
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch

class LandingScreenViewModel constructor(

    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepository()
) : ViewModel(), LandingScreenCallbacks.ResponseCallbacks {
    private var allTips: List<Tip>? = arrayListOf()
    private var callbacks: LandingScreenCallbacks? = null
    private val _observerRole: SingleLiveEvent<Role> by lazy {
        SingleLiveEvent<Role>();
    }
    val observerRole: SingleLiveEvent<Role> get() = _observerRole

    companion object {

        //TIPS

    }

    private val _tips = MutableLiveData<List<Tip>>()
    val tips: LiveData<List<Tip>> = _tips

    private var mPreferencesData: PreferencesDataModel? = null
    private var mProfileData: ProfileData? = null
    private var mPreferencesFinishedLoading: Boolean = false
    private var mProfileFinishedLoading: Boolean = false

    private var prefListenerRegistration: ListenerRegistration? = null
    private var profileListenerRegistration: ListenerRegistration? = null


    init {
        startWatchingProfileAndPreferencesChanges()
        callbacks = LandingScreenRepository()
    }

    private fun startWatchingProfileAndPreferencesChanges() = viewModelScope.launch {
        startWatchingProfileChanges()
        startWatchingPrefChanges()
    }

    private fun prepareTipsCards(
        preferencesData: PreferencesDataModel?,
        profileData: ProfileData?
    ) {
        val profileHelpTips: MutableList<Tip> = mutableListOf()

        if (profileData == null) {
            //Add All

            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_EDUCATION_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WORK_EXP_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_SKILLS_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_ACHIEVEMENTS_TIP"))!!)!!)

            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_PROFILE_PHOTO_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_LANGUAGE_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_ABOUT_ME_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_PERMANENT_ADD_TIP"))!!)!!)
        } else {

            if (profileData.educations.isNullOrEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_EDUCATION_TIP"))!!)!!)

            if (profileData.experiences.isNullOrEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WORK_EXP_TIP"))!!)!!)

            if (profileData.skills.isNullOrEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_SKILLS_TIP"))!!)!!)

            if (profileData.achievements.isNullOrEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_ACHIEVEMENTS_TIP"))!!)!!)

            if (profileData.profileAvatarName == "avatar.jpg")
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_PROFILE_PHOTO_TIP"))!!)!!)

            if (profileData.languages.isNullOrEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_LANGUAGE_TIP"))!!)!!)

            if (profileData.aboutMe.isBlank())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_ABOUT_ME_TIP"))!!)!!)

            if (profileData.address.home.isEmpty())
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_PERMANENT_ADD_TIP"))!!)!!)

            if (!profileData.address.current.isEmpty() && !profileData.address.current.preferredDistanceActive)
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_PREFERRED_DISTANCE_TIP"))!!)!!)
        }


        if (preferencesData == null) {
            //Add All

            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_DAILY_EARNING_EXPECTATION_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WEEKDAY_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WEEKEND_TIP"))!!)!!)
            profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WFH_TIP"))!!)!!)
        } else {

            if (preferencesData.earning.perDayGoal <= 0)
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_DAILY_EARNING_EXPECTATION_TIP"))!!)!!)

            if (!preferencesData.isweekdaysenabled)
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WEEKDAY_TIP"))!!)!!)

            if (!preferencesData.isweekendenabled)
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WEEKEND_TIP"))!!)!!)

            if (!preferencesData.isWorkFromHome)
                profileHelpTips.add(allTips?.get(allTips?.indexOf(Tip("ADD_WFH_TIP"))!!)!!)
        }

        _tips.value = profileHelpTips
    }

    private fun startWatchingPrefChanges() {

        prefListenerRegistration = preferencesRepository
            .getDBCollection()
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                mPreferencesFinishedLoading = true

                if (documentSnapshot?.data != null) {

                    runCatching { documentSnapshot.toObject(PreferencesDataModel::class.java) }
                        .onFailure { exception ->
                            exception.printStackTrace()
                        }
                        .onSuccess { prefData ->
                            this.mPreferencesData = prefData
                            compareAndPublish()
                        }
                } else {
                    compareAndPublish()
                }
            }
    }

    private fun startWatchingProfileChanges() {

        profileFirebaseRepository
            .getDBCollection()
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                mProfileFinishedLoading = true

                if (documentSnapshot?.data != null) {

                    runCatching { documentSnapshot.toObject(ProfileData::class.java) }
                        .onFailure { exception ->
                            exception.printStackTrace()
                        }
                        .onSuccess { profileData ->
                            mProfileData = profileData
                            compareAndPublish()
                        }
                } else {
                    compareAndPublish()
                }
            }
    }

    private fun compareAndPublish() {

        if (mPreferencesFinishedLoading && mProfileFinishedLoading)
            prepareTipsCards(
                preferencesData = mPreferencesData,
                profileData = mProfileData
            )
    }

    fun getRoles() {
        callbacks?.getRoles(true, this)

    }

    override fun onCleared() {
        super.onCleared()
        prefListenerRegistration?.remove()
        profileListenerRegistration?.remove()
    }

    override fun getRolesResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {

        } else {
            try {
                val role = querySnapshot?.toObjects(Role::class.java)?.get(0)
                role?.id = querySnapshot?.documents?.get(0)?.id
                observerRole.value = role
            } catch (e: Exception) {

            }
        }
    }

    fun setTips(tipsList: List<Tip>) {
        this.allTips = tipsList;
    }
}