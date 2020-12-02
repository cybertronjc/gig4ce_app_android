package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.GigActivation
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.utils.SingleLiveEvent
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

    private val _observableInitApplication: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableInitApplication: SingleLiveEvent<Boolean> get() = _observableInitApplication


    private val _observableGigActivation = MutableLiveData<GigActivation>()
    val observableGigActivation: MutableLiveData<GigActivation> = _observableGigActivation

    fun getActivationData(workOrderId: String) {

        repository.getCollectionReference().whereEqualTo("jobProfileId", workOrderId)
            .whereEqualTo("type", "documents").addSnapshotListener { success, err ->
                if (err == null) {
                    if (success?.documents?.isNotEmpty() == true) {
                        observableGigActivation.value =
                            success?.toObjects(GigActivation::class.java)?.get(0)

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
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
            .whereEqualTo("gigerId", repository.getUID()).get()
            .await()
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }

    fun updateDraftJpApplication(mWorkOrderID: String, dependency: List<Dependency>) =
        viewModelScope.launch {


            val model = getJPApplication(mWorkOrderID)

            if (model.process.isNullOrEmpty()) {
                model.process = dependency.toMutableList()

            }
            model.process.forEach {
                if (!it.isDone) {
                    when (it.type) {
                        "driving_certificate" -> {

                            it.isDone = checkForDrivingCertificate(
                                mWorkOrderID,
                                it.type ?: "",
                                it.title ?: ""
                            )

                        }

                    }
                }


                if (model.id.isEmpty()) {
                    repository.db.collection("JP_Applications").document().set(model)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {

                                observableJpApplication.value = model
                                observableInitApplication.value = true

                            }
                        }
                } else {
                    repository.db.collection("JP_Applications").document(model.id).set(model)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                observableJpApplication.value = model
                                observableInitApplication.value = true

                            }
                        }
                }


            }


        }

    suspend fun checkForDrivingCertificate(
        workOrderID: String,
        type: String,
        title: String
    ): Boolean {

        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
            .whereEqualTo("gigerId", repository.getUID()).get()
            .await()
        if (items.documents.isNullOrEmpty()) {
            return false
        }
        val documentSnapshot = items.documents[0]
        return !repository.db.collection("JP_Applications").document(documentSnapshot.id)
            .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                "title", title
            ).whereEqualTo("type", type).get().await().documents.isNullOrEmpty()


    }
}