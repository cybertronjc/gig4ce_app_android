package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.SingleLiveEvent

class AddLanguageViewModel : ViewModel() {
    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess
    val addLanguageRepo = AddLanguageRepository()

    fun addLanguages(list: MutableList<Language>) {
        addLanguageRepo.getCollectionReference().document(addLanguageRepo.getUID())
            .update("languages", list).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }

}