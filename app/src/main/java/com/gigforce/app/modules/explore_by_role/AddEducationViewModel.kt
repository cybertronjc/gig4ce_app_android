package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.profile.Education
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp

class AddEducationViewModel : ViewModel() {
    val educationRepository = AddEducationRepository()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addEducation(list: MutableList<Education>) {
        val map = mapOf("educations" to list, "updatedAt" to Timestamp.now(), "updatedBy" to StringConstants.APP.value)
        educationRepository.getCollectionReference().document(educationRepository.getUID())
            .update(map).addOnCompleteListener {
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