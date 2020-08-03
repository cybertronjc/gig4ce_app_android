package com.gigforce.app.modules.earn

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.earn.models.GigsResponse
import com.gigforce.app.modules.gigPage.models.Gig
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class GigHistoryViewModel(private val repositoryCallbacks: DataCallbacks) :
    ViewModel(), DataCallbacks.ResponseCallbacks {
    private var lastVisibleItem: DocumentSnapshot? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = true
    var pastGigs: Boolean = true
    private val limit: Long = 10

    private val _observerProgress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>();
    }
    val observerShowProgress: MutableLiveData<Int> get() = _observerProgress

    private val _observableOnGoingGigs: MutableLiveData<GigsResponse> by lazy {
        MutableLiveData<GigsResponse>();
    }
    val observableOnGoingGigs: MutableLiveData<GigsResponse> get() = _observableOnGoingGigs
    private val _observableScheduledGigs: MutableLiveData<GigsResponse> by lazy {
        MutableLiveData<GigsResponse>();
    }

    val observableScheduledGigs: MutableLiveData<GigsResponse> get() = _observableScheduledGigs


    private val _observableShowExplore: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    val observableShowExplore: MutableLiveData<Boolean> get() = _observableShowExplore
    fun getData() {
        showProgress(true)
        repositoryCallbacks.checkGigsCount(this)
        repositoryCallbacks.getOnGoingGigs(this)
        repositoryCallbacks.getPastGigs(this, null, limit)
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
        if (querySnapshot != null) {
            if (querySnapshot.documents.isNotEmpty())
                lastVisibleItem = querySnapshot.documents[querySnapshot.size() - 1]
            isLastPage = querySnapshot.documents.size < limit
            observableScheduledGigs.value = GigsResponse(
                true,
                "Past Gigs Loaded Successfully",
                querySnapshot.toObjects(Gig::class.java)
            )
        } else
            observableScheduledGigs.value = GigsResponse(false, error?.message!!)

    }

    override fun upcomingGigsResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (querySnapshot != null) {
            if (querySnapshot.documents.isNotEmpty())
                lastVisibleItem = querySnapshot.documents[querySnapshot.size() - 1]
            isLastPage = querySnapshot.documents.size < limit
            observableScheduledGigs.value = GigsResponse(
                true,
                "Upcoming Gigs Loaded Successfully",
                querySnapshot.toObjects(Gig::class.java)
            )
        } else
            observableScheduledGigs.value = GigsResponse(false, error?.message!!)
    }

    override fun gigsCountResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (querySnapshot != null && querySnapshot.isEmpty) {
            observableShowExplore.value = querySnapshot.isEmpty
        }
    }


    fun getGigs(pastGigs: Boolean, resetPageCount: Boolean) {
        showProgress(true)
        this.pastGigs = pastGigs;
        if (resetPageCount) {
            isLastPage = false
            isLoading = true
            lastVisibleItem = null

        }
        if (pastGigs) {
            repositoryCallbacks.getPastGigs(this, lastVisibleItem, limit)
        } else {
            repositoryCallbacks.getUpComingGigs(this, lastVisibleItem, limit)
        }


    }

    fun showProgress(show: Boolean) {
        observerShowProgress.value = if (show) View.VISIBLE else View.GONE
    }


}