package com.gigforce.client_activation.client_activation.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.client_activation.client_activation.repository.ClientActiExploreRepository
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.di.repo.IProfileFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ClientActiExploreListViewModel constructor(
    private val clientActiExploreRepository: ClientActiExploreRepository = ClientActiExploreRepository()
) : ViewModel() {

    private val _observableJobProfile: SingleLiveEvent<ArrayList<JpExplore>> by lazy {
        SingleLiveEvent<ArrayList<JpExplore>>()
    }
    val observableJobProfile: SingleLiveEvent<ArrayList<JpExplore>> get() = _observableJobProfile

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>()
    }
    val observableError: SingleLiveEvent<String> get() = _observableError


     fun getJobProfiles(){
         viewModelScope.launch {
             try {
                 val allClientActivations = ArrayList<JpExplore>()
                 val items = clientActiExploreRepository.db.collection("Job_Profiles")
                     .whereEqualTo("isActive", true).get()
                     .await()
                 if (items.documents.isNullOrEmpty()){
                     _observableJobProfile.value = allClientActivations
                 }
                 val toObjects = items.toObjects(JobProfile::class.java)
                 for (i in 0..items.size() - 1 ){
                     val obj = toObjects[i]
                     obj.id = items.documents[i].id
                     if (obj.id != null){
                             Log.d("id", obj.id!!)
                         Log.d("uid", clientActiExploreRepository.getUID())

                             val jpObject = getJPApplication(obj.id!!)
                             val jpExplore = JpExplore(obj.id!!,jpId = jpObject.id, profileId = obj.profileId, obj.profileName,  obj.cardTitle, obj.cardImage, jpObject.status)
                             allClientActivations.add(jpExplore)
                                     }

                     }
                 _observableJobProfile.value = allClientActivations
             }catch (e: Exception){
                 _observableError.value = e.message
             }
         }
    }
    suspend fun getJPApplication(jobProfileId: String): JpApplication {
        try {
            val items =
                    clientActiExploreRepository.db.collection("JP_Applications")
                        .whereEqualTo("gigerId", clientActiExploreRepository.getUID())
                        .whereEqualTo("jpid", jobProfileId)
                        .get().await()
            Log.d("uid", clientActiExploreRepository.getUID())

            if (items.documents.isNullOrEmpty()) {
                return JpApplication(JPId = jobProfileId, gigerId = clientActiExploreRepository.getUID())
            }
            val toObject = items.toObjects(JpApplication::class.java).get(0)
            toObject.id = items.documents[0].id
            Log.d("value", toObject?.toString())
            return toObject
        } catch (e: Exception) {
            Log.d("error", e.toString())
            _observableError.value = e.message
        }
        return JpApplication()


    }
}