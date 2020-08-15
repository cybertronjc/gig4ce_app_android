package com.gigforce.app.modules.landingscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.R
import com.gigforce.app.modules.landingscreen.models.Tip
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.AboutExpandedFragment
import com.gigforce.app.modules.profile.EducationExpandedFragment
import com.gigforce.app.modules.profile.ExperienceExpandedFragment
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class LandingScreenViewModel constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepository()
) : ViewModel() {

    companion object {

        //TIPS
        private val ADD_EDUCATION_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Your education details help build your profile.",
            whereToRedirect = R.id.educationExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to EducationExpandedFragment.ACTION_OPEN_EDIT_EDUCATION_BOTTOM_SHEET
            )
        )

        private val ADD_WORK_EXP_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Your work experience helps find similar gigs for you.",
            whereToRedirect = R.id.experienceExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to ExperienceExpandedFragment.ACTION_OPEN_EDIT_EXPERIENCE_BOTTOM_SHEET
            )
        )

        private val ADD_SKILLS_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Adding your skills helps recommend suitable gigs.",
            whereToRedirect = R.id.educationExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to EducationExpandedFragment.ACTION_OPEN_EDIT_SKILLS_BOTTOM_SHEET
            )
        )

        private val ADD_ACHIEVEMENTS_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Sharing your past achievements highlights your profile.",
            whereToRedirect = R.id.educationExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to EducationExpandedFragment.ACTION_OPEN_EDIT_ACHIEVEMENTS_BOTTOM_SHEET
            )
        )


        private val ADD_PROFILE_PHOTO_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Adding a profile photo shows off your personality.",
            whereToRedirect = R.id.profileFragment
        )

        private val ADD_LANGUAGE_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "How many languages can you speak in?",
            whereToRedirect = R.id.aboutExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to AboutExpandedFragment.ACTION_OPEN_EDIT_LANGUAGE_BOTTOM_SHEET
            )
        )

        private val ADD_ABOUT_ME_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Tell me 2 lines that best describe your.",
            whereToRedirect = R.id.aboutExpandedFragment,
            intentExtraMap = mapOf(
                LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                LandingPageConstants.INTENT_EXTRA_ACTION to AboutExpandedFragment.ACTION_OPEN_EDIT_ABOUT_ME_BOTTOM_SHEET
            )
        )


        //Pref Tips from here

        private val ADD_PERMANENT_ADD_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Add your permanent address to complete verification?",
            whereToRedirect = R.id.permanentAddressViewFragment
        )


        private val ADD_PREFERRED_DISTANCE_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "How far are you willing to travel for work daily?",
            whereToRedirect = R.id.arrountCurrentAddress
        )

        private val ADD_DAILY_EARNING_EXPECTATION_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "What is your daily earning expectation?",
            whereToRedirect = R.id.earningFragment
        )


        private val ADD_WEEKDAY_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "How many days during the week are you willing to work?",
            whereToRedirect = R.id.weekDayFragment
        )

        private val ADD_WEEKEND_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Are you willing to work during the weekends?",
            whereToRedirect = R.id.weekEndFragment
        )

        private val ADD_WFH_TIP = Tip(
            title = "Gigforce Tip ",
            subTitle = "Would you want to work from home?",
            whereToRedirect = R.id.locationFragment
        )
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

            profileHelpTips.add(ADD_EDUCATION_TIP)
            profileHelpTips.add(ADD_WORK_EXP_TIP)
            profileHelpTips.add(ADD_SKILLS_TIP)
            profileHelpTips.add(ADD_ACHIEVEMENTS_TIP)

            profileHelpTips.add(ADD_PROFILE_PHOTO_TIP)
            profileHelpTips.add(ADD_LANGUAGE_TIP)
            profileHelpTips.add(ADD_ABOUT_ME_TIP)
            profileHelpTips.add(ADD_PERMANENT_ADD_TIP)
        } else {

            if (profileData.educations.isNullOrEmpty())
                profileHelpTips.add(ADD_EDUCATION_TIP)

            if (profileData.experiences.isNullOrEmpty())
                profileHelpTips.add(ADD_WORK_EXP_TIP)

            if (profileData.skills.isNullOrEmpty())
                profileHelpTips.add(ADD_SKILLS_TIP)

            if (profileData.achievements.isNullOrEmpty())
                profileHelpTips.add(ADD_ACHIEVEMENTS_TIP)

            if (profileData.profileAvatarName == "avatar.jpg")
                profileHelpTips.add(ADD_PROFILE_PHOTO_TIP)

            if (profileData.languages.isNullOrEmpty())
                profileHelpTips.add(ADD_LANGUAGE_TIP)

            if (profileData.aboutMe.isBlank())
                profileHelpTips.add(ADD_ABOUT_ME_TIP)

            if (profileData.address.home.isEmpty())
                profileHelpTips.add(ADD_PERMANENT_ADD_TIP)

            if (!profileData.address.current.isEmpty() && !profileData.address.current.preferredDistanceActive)
                profileHelpTips.add(ADD_PREFERRED_DISTANCE_TIP)
        }


        if (preferencesData == null) {
            //Add All

            profileHelpTips.add(ADD_DAILY_EARNING_EXPECTATION_TIP)
            profileHelpTips.add(ADD_WEEKDAY_TIP)
            profileHelpTips.add(ADD_WEEKEND_TIP)
            profileHelpTips.add(ADD_WFH_TIP)
        } else {

            if (preferencesData.earning.perDayGoal <= 0)
                profileHelpTips.add(ADD_DAILY_EARNING_EXPECTATION_TIP)

            if (!preferencesData.isweekdaysenabled)
                profileHelpTips.add(ADD_WEEKDAY_TIP)

            if (!preferencesData.isweekendenabled)
                profileHelpTips.add(ADD_WEEKEND_TIP)

            if (!preferencesData.isWorkFromHome)
                profileHelpTips.add(ADD_WFH_TIP)
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


    override fun onCleared() {
        super.onCleared()
        prefListenerRegistration?.remove()
        profileListenerRegistration?.remove()
    }
}