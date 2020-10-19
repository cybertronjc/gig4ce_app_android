package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.SingleLiveEvent

class AddExperienceViewModel :ViewModel() {
    val experienceRepo = AddExperienceRepository()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addExperience(list: MutableList<Experience>) {
        experienceRepo.getCollectionReference().document(experienceRepo.getUID())
            .update("experiences", list).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }
}