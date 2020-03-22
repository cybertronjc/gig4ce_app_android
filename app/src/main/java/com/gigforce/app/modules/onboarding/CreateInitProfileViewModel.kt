package com.gigforce.app.modules.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.onboarding.models.OnboardingChatLog
import com.gigforce.app.modules.onboarding.models.Profile
import com.gigforce.app.modules.onboarding.utils.OnboardingController
import com.gigforce.app.modules.onboarding.utils.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateInitProfileViewModel(): ViewModel() {

    lateinit var profileManager:ProfileManager
    lateinit var onboardingController: OnboardingController

    val logs:LiveData<List<OnboardingChatLog?>>
        get() = onboardingController.logs

    val activeQuestion:LiveData<OnboardingChatLog>
        get() = onboardingController.activeQuestion

    init {
        initForGigerId(FirebaseAuth.getInstance().currentUser?.uid!!)
    }

    fun initForGigerId(gigerId:String) {
        profileManager = ProfileManager(gigerId)
        onboardingController = OnboardingController(gigerId, profileManager)
    }

}