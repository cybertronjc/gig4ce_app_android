package com.gigforce.app.modules.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.onboarding.models.OnboardingChatLog
import com.gigforce.app.modules.onboarding.models.Profile
import com.gigforce.app.modules.onboarding.utils.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateInitProfileViewModel(): ViewModel() {

    lateinit var profileManager:ProfileManager

    val profile:LiveData<Profile>
        get() = profileManager.profileDoc

    val _logs:MutableLiveData<OnboardingChatLog> = MutableLiveData()
    val logs:LiveData<OnboardingChatLog>
        get() = _logs

    init {
        initForGigerId(FirebaseAuth.getInstance().currentUser?.uid!!)
    }

    fun initForGigerId(gigerId:String) {
        profileManager = ProfileManager(gigerId)
        addLogsListener(gigerId)
    }

    private fun addLogsListener(gigerId:String) {
        FirebaseFirestore.getInstance()
            .collection("OnboardingChatLogs")
            .whereEqualTo("userid", gigerId)
            .addSnapshotListener { snapshot, exception ->
                snapshot?.documents?.toMutableList()
            }
    }

}