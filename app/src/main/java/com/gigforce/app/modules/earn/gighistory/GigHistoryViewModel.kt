package com.gigforce.app.modules.earn.gighistory

import android.view.View
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.earn.gighistory.models.GigsResponse
import com.gigforce.app.modules.gigPage.models.DocChange
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class GigHistoryViewModel(private val repositoryCallbacks: DataCallbacks) :
    ViewModel(), DataCallbacks.ResponseCallbacks {
    var eventState: Int = AdapterGigHistory.EVENT_PAST
    private var lastVisibleItem: DocumentSnapshot? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = true
    var pastGigs: Boolean = true
    var isInitialDataLoaded = false;
    private val limit: Long = 10

    private val _observerProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>();
    }
    val observerShowProgress: SingleLiveEvent<Int> get() = _observerProgress

    private val _observableOnGoingGigs: SingleLiveEvent<GigsResponse> by lazy {
        SingleLiveEvent<GigsResponse>();
    }
    val observableOnGoingGigs: SingleLiveEvent<GigsResponse> get() = _observableOnGoingGigs
    private val _observableScheduledGigs: SingleLiveEvent<GigsResponse> by lazy {
        SingleLiveEvent<GigsResponse>();
    }
    val observableScheduledGigs: SingleLiveEvent<GigsResponse> get() = _observableScheduledGigs
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError
    private val _observableShowExplore: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableShowExplore: SingleLiveEvent<Boolean> get() = _observableShowExplore
    private val _observableDocChange: SingleLiveEvent<DocChange> by lazy {
        SingleLiveEvent<DocChange>();
    }
    val observableDocChange: SingleLiveEvent<DocChange> get() = _observableDocChange

    fun getData() {
        repositoryCallbacks.getOnGoingGigs(this)
        if (!isInitialDataLoaded) {
            showProgress(true)
            repositoryCallbacks.checkGigsCount(this)
            repositoryCallbacks.getPastGigs(this, null, limit)
            isInitialDataLoaded = true
        }

    }

    override fun onGoingGigsResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (querySnapshot != null) observableOnGoingGigs.value = GigsResponse(
            true,
            "On Going Gigs Loaded Successfully",
            getGigsWithId(querySnapshot, false)
        ) else
            error?.message?.let {
                observableError.value = it
            }
        repositoryCallbacks.removeOnGoingGigsListener()
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
                getGigsWithId(querySnapshot, true)
            )
            repositoryCallbacks.removeListener()
        } else
            error?.message?.let {
                observableError.value = it
            }


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
                getGigsWithId(querySnapshot, false)
            )
            repositoryCallbacks.removeListener()
        } else
            error?.message?.let {
                observableError.value = it
            }
    }

    override fun gigsCountResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (querySnapshot != null) {
            if (querySnapshot.isEmpty) {
                observableShowExplore.value = querySnapshot.isEmpty
            }
        } else {
            error?.message?.let {
                observableError.value = it
            }

        }
    }

    override fun docChange(docChangeType: DocumentChange.Type, change: DocumentChange) {
        val obj = change.document.toObject(Gig::class.java)
        obj.gigId = change.document.id
        observableDocChange.value = DocChange(docChangeType, obj)
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

    private fun getGigsWithId(
        querySnapshot: QuerySnapshot,
        checkForCompletedGigs: Boolean
    ): ArrayList<Gig> {
        var userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }
        if (checkForCompletedGigs) {
            userGigs.retainAll { element ->
                element.isGigOfPastDay() || element.isGigOfToday() && element.isCheckInAndCheckOutMarked()
            }
        }
        return userGigs as ArrayList<Gig>
    }

    fun observeDocChanges() {
        repositoryCallbacks.observeDocumentChanges(this)
    }


}