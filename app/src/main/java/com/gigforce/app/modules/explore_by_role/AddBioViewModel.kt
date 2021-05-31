package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

class AddBioViewModel(private val addBioViewModelCallbacks: AddBioViewModelCallbacks) : ViewModel(),
        AddBioViewModelCallbacks.ResponseCallbacks {
    private val _observableProfileData: SingleLiveEvent<ProfileData> by lazy {
        SingleLiveEvent<ProfileData>();
    }
    val observableProfileData: SingleLiveEvent<ProfileData> get() = _observableProfileData

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

    fun getProfileData() = viewModelScope.launch {
        addBioViewModelCallbacks.getProfileData(this@AddBioViewModel)
    }

    override fun profileDate(docReference: DocumentSnapshot?, exception: Exception?) {
        if (exception != null) {
            observableError.value = exception.message
        } else {
            if (docReference?.data != null) {
                _observableProfileData.value = docReference.toObject(ProfileData::class.java)
            }
        }


    }

    fun saveBio(bio: String) {
        addBioViewModelCallbacks.saveBio(bio, this)
    }


}