package com.gigforce.giger_gigs.gighistory

//import com.gigforce.app.modules.earn.gighistory.models.GigsResponse
//import com.gigforce.core.datamodels.gigpage.DocChange
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.DocChange
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.giger_gigs.gighistory.models.GigsResponse
import com.gigforce.core.SingleLiveEvent
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class GigHistoryViewModel  @Inject constructor(
    private val repositoryCallbacks: DataCallbacks
    ) : ViewModel(), DataCallbacks.ResponseCallbacks {
    var eventState: Int = AdapterGigHistory.EVENT_PAST
    private var lastVisibleItem: DocumentSnapshot? = null
    private var lastVisibleItemIndex = 0
    var isLastPage: Boolean = false
    var isLoading: Boolean = true
    var pastGigs: Boolean = true
    var isInitialDataLoaded = false
    private val limit: Long = 10

    private val _observerProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>()
    }
    val observerShowProgress: SingleLiveEvent<Int> get() = _observerProgress

    private val _observableOnGoingGigs: SingleLiveEvent<GigsResponse> by lazy {
        SingleLiveEvent<GigsResponse>()
    }
    val observableOnGoingGigs: SingleLiveEvent<GigsResponse> get() = _observableOnGoingGigs
    private val _observableScheduledGigs: SingleLiveEvent<GigsResponse> by lazy {
        SingleLiveEvent<GigsResponse>()
    }
    val observableScheduledGigs: SingleLiveEvent<GigsResponse> get() = _observableScheduledGigs
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>()
    }
    val observableError: SingleLiveEvent<String> get() = _observableError
    private val _observableShowExplore: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>()
    }
    val observableShowExplore: SingleLiveEvent<Boolean> get() = _observableShowExplore
    private val _observableDocChange: SingleLiveEvent<DocChange> by lazy {
        SingleLiveEvent<DocChange>()
    }
    val observableDocChange: SingleLiveEvent<DocChange> get() = _observableDocChange

    fun getData() = viewModelScope.launch {
        repositoryCallbacks.getOnGoingGigs(
            this@GigHistoryViewModel,
            observableOnGoingGigs.value == null || observableOnGoingGigs.value!!.data == null || observableOnGoingGigs.value?.data?.isEmpty()!!
        )
        if (!isInitialDataLoaded) {
            showProgress(true)
            repositoryCallbacks.checkGigsCount(this@GigHistoryViewModel)
            repositoryCallbacks.getPastGigs(this@GigHistoryViewModel,
                0,
                limit
            )
            isInitialDataLoaded = true
        }

    }

    override fun onGoingGigsResponse(
        gigs: List<Gig>?,
        error: Exception?,
        initialLoading: Boolean
    ) {
        if (gigs != null) observableOnGoingGigs.value = GigsResponse(
            true,
            "On Going Gigs Loaded Successfully",
            ArrayList(gigs)
        ) else {
            error?.message?.let {
                observableError.value = it
            }
            error?.printStackTrace()
        }
    }

    override fun pastGigsResponse(
        gigs: List<Gig>?,
        error: Exception?
    ) {

        if (gigs != null) {
            if (gigs.isNotEmpty()) {
                lastVisibleItemIndex += gigs.size
            }
            isLastPage = gigs.size < limit

            observableScheduledGigs.value = GigsResponse(
                true,
                "Past Gigs Loaded Successfully",
                ArrayList( gigs)
            )
        } else {
            error?.printStackTrace()
            error?.message?.let {
                observableError.value = it
            }
        }
    }



    override fun upcomingGigsResponse(
        gigs: List<Gig>?,
        error: Exception?
    ) {

        if (gigs != null) {
            if (gigs.isNotEmpty()){
                lastVisibleItemIndex += gigs.size
            }

            isLastPage = gigs.size < limit
            observableScheduledGigs.value = GigsResponse(
                true,
                "Upcoming Gigs Loaded Successfully",
                ArrayList(gigs)
            )
        } else {
            error?.message?.let {
                observableError.value = it
            }
            error?.printStackTrace()
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
            error?.printStackTrace()

        }
    }

    override fun docChange(docChangeType: DocumentChange.Type, change: DocumentChange) {
        val obj = change.document.toObject(Gig::class.java)
        obj.gigId = change.document.id
        observableDocChange.value =
                DocChange(docChangeType, obj)
    }


    fun getGigs(pastGigs: Boolean, resetPageCount: Boolean)  = viewModelScope.launch{
        showProgress(true)
        this@GigHistoryViewModel.pastGigs = pastGigs
        if (resetPageCount) {
            isLastPage = false
            isLoading = true
            lastVisibleItem = null
            lastVisibleItemIndex = 0
        }
        if (pastGigs) {
            repositoryCallbacks.getPastGigs(
                this@GigHistoryViewModel,
                lastVisibleItemIndex /limit,
                limit
            )
        } else {
            repositoryCallbacks.getUpComingGigs(
                this@GigHistoryViewModel,
                lastVisibleItemIndex /limit ,
                limit
            )
        }
    }

    fun showProgress(show: Boolean) {
        observerShowProgress.value = if (show) View.VISIBLE else View.GONE
    }

    private fun getGigsWithId(
        querySnapshot: QuerySnapshot,
        checkForCompletedGigs: Boolean,
        fetchOnGoing: Boolean,
        getUpcomingGig: Boolean
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
                val gigStatus = GigStatus.fromGig(element)
                gigStatus == GigStatus.COMPLETED || gigStatus == GigStatus.MISSED
            }
        } else if (fetchOnGoing) {
            userGigs.retainAll { element ->

                val gigStatus = GigStatus.fromGig(element)
                gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW

            }
        } else if (getUpcomingGig) {
            userGigs.retainAll { element ->

                val gigStatus = GigStatus.fromGig(element)
                gigStatus == GigStatus.UPCOMING
            }
        }
        return userGigs as ArrayList<Gig>
    }

    fun observeDocChanges() {
        repositoryCallbacks.observeDocumentChanges(this)
    }


}