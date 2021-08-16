package com.gigforce.client_activation.client_activation.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.client_activation.client_activation.repository.ClientActiExploreRepository
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

                var toObjects = ArrayList<JobProfile>()
                items.documents.forEach {
                        try {
                            var obj = it.toObject(JobProfile::class.java)
                            obj?.let { data ->
                                toObjects.add(data)
                            }
                        } catch (e: Exception) {
                            Log.e("ClientActivation",e.toString())
                            FirebaseCrashlytics.getInstance().log("Exception : Job profile data type format issues. Method : getJobProfiles  ${e.message} $e")
                            CrashlyticsLogger.e(
                                "Client Activation",
                                "Parsing data from DB. Method : getJobProfiles ",
                                e
                            )
                        }
                    }


//                val toObjects = items.toObjects(JobProfile::class.java)
                for (i in 0 until toObjects.size){
                    val obj = toObjects[i]
                    var jobProfileId = items.documents[i].id
                    obj.id = toObjects[i].profileId
                    if (obj.id != null){
                        Log.d("profileId", obj.id!!)
                        val jpObject = getJPApplication(obj.id!!)
                        Log.d("object", jpObject.toString())
                        val jpExplore = JpExplore(jobProfileId,jpId = jpObject.id, profileId = obj.profileId,
                            profileName = obj.profileName,
                            title = obj.cardTitle, image = obj.cardImage,
                            status = jpObject.status, jobProfileTitle = obj.title
                        )
                        allClientActivations.add(jpExplore)
                    }
                }
                _observableJobProfile.value = allClientActivations

            }catch (e: Exception){
                _observableError.value = e.message
                _observableJobProfile.value = ArrayList()
            }

        }

    }

    suspend fun getJPApplication(jobProfileId: String): JpApplication {
        var jpApplication = JpApplication()
        try {
            val items =
                    clientActiExploreRepository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileId)
                            .whereEqualTo("gigerId", clientActiExploreRepository.getUID()).get()
                            .await()

            if (items.documents.isNullOrEmpty()) {
                jpApplication = JpApplication(JPId = jobProfileId, gigerId = clientActiExploreRepository.getUID())
            } else {
                val toObject = items.toObjects(JpApplication::class.java).get(0)
                toObject.id = items.documents[0].id
                Log.d("status", toObject.toString())
                jpApplication = toObject
            }

        } catch (e: Exception) {
            _observableError.value = e.message
        }

        return jpApplication
    }

}