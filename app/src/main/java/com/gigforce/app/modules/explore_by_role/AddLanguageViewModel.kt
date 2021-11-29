package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.profile.Language
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp

class AddLanguageViewModel : ViewModel() {
    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess
    val addLanguageRepo = AddLanguageRepository()

    fun addLanguages(list: MutableList<Language>) {
        val map = mapOf("languages" to list, "updatedAt" to Timestamp.now(), "updatedBy" to addLanguageRepo.getUID())
        addLanguageRepo.getCollectionReference().document(addLanguageRepo.getUID())
            .update(map).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }

}