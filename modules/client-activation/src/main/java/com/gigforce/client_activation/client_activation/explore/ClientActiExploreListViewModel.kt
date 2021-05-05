package com.gigforce.client_activation.client_activation.explore

import androidx.lifecycle.ViewModel
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.client_activation.client_activation.repository.ClientActiExploreRepository
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.di.repo.IProfileFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot


class ClientActiExploreListViewModel constructor(
    private val clientActiExploreRepository: ClientActiExploreRepository = ClientActiExploreRepository()
) : ViewModel(), ClientActiCallbacks.ResponseCallbacks {

    private var callbacks: ClientActiCallbacks? = null
    private val _observableJobProfile: SingleLiveEvent<ArrayList<JobProfile>> by lazy {
        SingleLiveEvent<ArrayList<JobProfile>>()
    }
    val observableJobProfile: SingleLiveEvent<ArrayList<JobProfile>> get() = _observableJobProfile


    init {
        callbacks = ClientActiExploreRepository()
    }

    fun getJobProfiles(){
        callbacks?.getJobProfiles(this)
    }

     override fun getJobProfilesResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error == null) {
            if (querySnapshot?.documents?.isNotEmpty() == true) {
                var allClientActivations = ArrayList<JobProfile>()
                for (clientActi in querySnapshot.documents) {
                    val jobProfileData = clientActi.toObject(JobProfile::class.java)
                    jobProfileData?.let {
                        jobProfileData.id = clientActi.id
                        allClientActivations.add(jobProfileData)
                    }
                }
                _observableJobProfile.value = allClientActivations
            }

        }
    }
}