package com.gigforce.client_activation.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.client_activation.client_activation.models.PartnerSchool
import com.gigforce.core.SingleLiveEvent
import com.gigforce.common_ui.StringConstants

class SelectPartnerSchoolViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val repository = SelectPartnerSchoolRepository()

    private val _observablePartnerSchool: MutableLiveData<PartnerSchool>
        get() = savedStateHandle.getLiveData(
            StringConstants.SAVED_STATE.value,
            null
        )
    val observablePartnerSchool: MutableLiveData<PartnerSchool> = _observablePartnerSchool


    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    fun getPartnerSchoolDetails(type: String, jobProfileID: String) {
        repository.db.collection("JP_Settings").whereEqualTo("type", type)
            .whereEqualTo("jobProfileId", jobProfileID).addSnapshotListener { success, err ->
            if (err == null) {
                _observablePartnerSchool.value =
                    success?.toObjects(PartnerSchool::class.java)?.get(0)
            } else {
                _observableError.value = err.message

            }
        }
    }

}