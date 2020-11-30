package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.GigActivation
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.StringConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GigActivationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var initialized: Boolean = false
    val repository = GigActivationRepository()

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>();
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication


    private val _observableGigActivation: MutableLiveData<GigActivation>
        get() = savedStateHandle.getLiveData(
                StringConstants.SAVED_STATE.value,
                null
        )
    val observableGigActivation: MutableLiveData<GigActivation> = _observableGigActivation

    fun getActivationData(workOrderId: String, type: String) {

        repository.getCollectionReference().whereEqualTo("workOrderId", workOrderId).whereEqualTo("type", type).addSnapshotListener { success, err ->
            if (err == null) {
                if (success?.documents?.isNotEmpty() == true) {
                    observableGigActivation.value = success?.toObjects(GigActivation::class.java)?.get(0)

                }
            } else {
                observableError.value = err.message
            }
        }

    }

    fun getApplication(
            mWorkOrderID: String
    ) = viewModelScope.launch {
        observableJpApplication.value = getJPApplication(mWorkOrderID)

    }

    suspend fun getJPApplication(workOrderID: String): JpApplication {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }


}