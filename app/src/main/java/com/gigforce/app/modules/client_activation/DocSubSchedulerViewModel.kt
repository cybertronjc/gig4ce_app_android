package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.JpApplication
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DocSubSchedulerViewModel : ViewModel() {
    val repository: DocSubSchedulerRepository = DocSubSchedulerRepository()
    private val _observableJpApplication: MutableLiveData<JpApplication> = MutableLiveData()
    val observableJpApplication: MutableLiveData<JpApplication> = _observableJpApplication


    fun getApplication(mWorkOrderID: String) = viewModelScope.launch {
        val model = getJPApplication(mWorkOrderID)
        _observableJpApplication.value = model

    }

    suspend fun getJPApplication(workOrderID: String): JpApplication {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        if (items.documents.isNullOrEmpty()) {
            return JpApplication(JPId = workOrderID, gigerId = repository.getUID())
        }
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }

}