package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.SingleLiveEvent

class AddLanguageViewModel : ViewModel() {
    private val _observableSuccess: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableSuccess: SingleLiveEvent<Boolean> get() = _observableSuccess
    val addLanguageRepo = AddLanguageRepository()

    fun addLanguages(list: MutableList<Language>) {
        addLanguageRepo.getCollectionReference().document(addLanguageRepo.getUID())
            .update("languages", list).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = true
                }
            }
    }

}