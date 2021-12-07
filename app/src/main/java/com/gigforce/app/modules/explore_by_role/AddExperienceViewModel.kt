package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.profile.Experience
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp

class AddExperienceViewModel :ViewModel() {
    val experienceRepo = AddExperienceRepository()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addExperience(list: MutableList<Experience>) {
        var map = mapOf("experiences" to list, "updatedAt" to Timestamp.now(), "updatedBy" to experienceRepo.getUID())
        experienceRepo.getCollectionReference().document(experienceRepo.getUID())
            .update(map).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }
}