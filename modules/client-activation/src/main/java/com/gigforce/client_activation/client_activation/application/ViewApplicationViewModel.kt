package com.gigforce.client_activation.client_activation.application

import android.util.Log
import androidx.lifecycle.ViewModel
import com.gigforce.client_activation.client_activation.ClientActivationNavCallbacks
import com.gigforce.client_activation.client_activation.repository.ViewApplicationRepository
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.google.firebase.firestore.QuerySnapshot

 class ViewApplicationViewModel : ViewModel() {

     private val viewApplicationRepository = ViewApplicationRepository()

    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>()
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication

     private val _observableError: SingleLiveEvent<String> by lazy {
         SingleLiveEvent<String>()
     }
     val observableError: SingleLiveEvent<String> get() = _observableError



    fun getApplication(jobProfileID: String): JpApplication {
        var jpApplication = JpApplication()
        try {
        viewApplicationRepository.getJobApplication(jobProfileID).addSnapshotListener { value, error ->
            error?.printStackTrace()

            value.let {
                if (it?.documents!!.isNotEmpty()){
                     jpApplication = it.toObjects(JpApplication::class.java).get(0)
                    Log.d("object", jpApplication.toString())
                    observableJpApplication.value = jpApplication
                    jpApplication.id = it.documents[0]!!.id

                }

            }
        }
            return jpApplication
    }

        catch (e: Exception){
            _observableError.value = e.message
        }
        return jpApplication
    }

}