package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.utils.SingleLiveEvent
import com.google.android.gms.tasks.Task

class AddBioViewModel(private val addBioViewModelCallbacks: AddBioViewModelCallbacks) : ViewModel(),
    AddBioViewModelCallbacks.ResponseCallbacks {


    private val _observableAddBioResponse: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableAddBioResponse: SingleLiveEvent<Boolean> get() = _observableAddBioResponse
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    override fun saveBioResponse(
        task: Task<Void>
    ) {
        if (task.isSuccessful) {
            _observableAddBioResponse.value = true

        } else {
            if (task.exception != null) {
                _observableError.value = task.exception?.message
            }

        }

    }

    fun saveBio(bio: String) {
        addBioViewModelCallbacks.saveBio(bio,this)
    }
}