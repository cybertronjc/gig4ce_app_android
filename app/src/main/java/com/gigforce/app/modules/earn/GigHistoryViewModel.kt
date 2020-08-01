package com.gigforce.app.modules.earn

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.earn.models.GigsResponse
import com.gigforce.app.modules.gigPage.models.Gig
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class GigHistoryViewModel(private val repositoryCallbacks: DataCallbacks) :
    ViewModel(), DataCallbacks.ResponseCallbacks {

    private val _observableOnGoingGigs: MutableLiveData<GigsResponse> by lazy {
        MutableLiveData<GigsResponse>();
    }
    val observableOnGoingGigs: MutableLiveData<GigsResponse> get() = _observableOnGoingGigs
    private val _observableScheduledGigs: MutableLiveData<GigsResponse> by lazy {
        MutableLiveData<GigsResponse>();
    }
    val observableScheduledGigs: MutableLiveData<GigsResponse> get() = _observableScheduledGigs
    fun getData() {
        repositoryCallbacks.getOnGoingGigs(this)
        repositoryCallbacks.getPastGigs(this)
    }

    override fun onGoingGigsResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        observableOnGoingGigs.value = if (querySnapshot != null) GigsResponse(
            true,
            "On Going Gigs Loaded Successfully",
            querySnapshot.toObjects(Gig::class.java)
        ) else
            GigsResponse(false, error?.message!!)
    }

    override fun pastGigsResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        observableScheduledGigs.value = if (querySnapshot != null) GigsResponse(
            true,
            "Past Gigs Loaded Successfully",
            querySnapshot.toObjects(Gig::class.java)
        ) else
            GigsResponse(false, error?.message!!)

    }

    override fun upcomingGigsResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {

    }


}