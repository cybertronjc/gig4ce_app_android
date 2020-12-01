package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class ScheduleDrivingTestViewModel : ViewModel() {
    val repository = ScheduleDrivingTestRepository()

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>();
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication
    fun getApplication(mWorkOrderID: String) = viewModelScope.launch {
        val model = getJPApplication(mWorkOrderID)
        _observableJpApplication.value = model

    }

    suspend fun getJPApplication(workOrderID: String): JpApplication? {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        if (items.documents.isNullOrEmpty()) {
            return null
        }
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }




}