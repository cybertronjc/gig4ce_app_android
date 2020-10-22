package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.SingleLiveEvent

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

}