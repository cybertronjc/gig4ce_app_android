package com.gigforce.app.modules.gigPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class GigViewModel constructor(
        private val gigsRepository: GigsRepository = GigsRepository()
) : ViewModel() {

    private var mWatchUpcomingRepoRegistration: ListenerRegistration? = null
    private var mWatchSingleGigRegistration: ListenerRegistration? = null

    private val _upcomingGigs = MutableLiveData<Lce<List<Gig>>>()
    val upcomingGigs: LiveData<Lce<List<Gig>>> get() = _upcomingGigs

    fun watchUpcomingGigs() {
        _upcomingGigs.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
                .getCurrentUserGigs()
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if (querySnapshot != null) {
                        extractUpcomingGigs(querySnapshot)
                    } else {
                        _upcomingGigs.value = Lce.error(firebaseFirestoreException!!.message!!)
                    }
                }
    }

    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot) {
        val userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }

        val currentDate = Date()
        val upcomingGigs = userGigs.filter {
            it.startDateTime!!.toDate().time > currentDate.time
        }
        _upcomingGigs.value = Lce.content(upcomingGigs)
    }


    /**
     * Specific Gig
     */

    private val _gigDetails = MutableLiveData<Lce<Gig>>()
    val gigDetails: LiveData<Lce<Gig>> get() = _gigDetails

    fun watchGig(gigId: String) {
        _gigDetails.value = Lce.loading()
        mWatchUpcomingRepoRegistration = gigsRepository
                .getCollectionReference()
                .document(gigId)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                    if (documentSnapshot != null) {
                        runCatching {
                            documentSnapshot.toObject(Gig::class.java)
                        }.onSuccess {
                            _gigDetails.value = Lce.content(it!!)
                        }.onFailure {
                            _gigDetails.value = Lce.error(it.message!!)
                        }
                    } else {
                        _gigDetails.value = Lce.error(firebaseFirestoreException!!.message!!)
                    }
                }
    }


    override fun onCleared() {
        super.onCleared()
        mWatchUpcomingRepoRegistration?.remove()
        mWatchSingleGigRegistration?.remove()
    }
}