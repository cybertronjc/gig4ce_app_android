package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.profile.Education
import com.gigforce.core.SingleLiveEvent

class AddEducationViewModel : ViewModel() {
    val educationRepository = AddEducationRepository()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addEducation(list: MutableList<Education>) {
        educationRepository.getCollectionReference().document(educationRepository.getUID())
            .update("educations", list).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }

    fun getUid(): String {
        return educationRepository.getUID()
    }

}