package com.gigforce.giger_app.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.GigInfoCardDVM
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.utils.Lce
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

interface IUpcomingGigInfoRepository {
    fun loadData()
    fun getData(): LiveData<List<Gig>>
}

class UpcomingGigInfoRepository @Inject constructor() : IUpcomingGigInfoRepository,
    BaseFirestoreDBRepository() {
    private var data: MutableLiveData<List<Gig>> = MutableLiveData()
    private var mWatchUpcomingRepoRegistration: ListenerRegistration? = null

    fun watchUpcomingGigs() {
        mWatchUpcomingRepoRegistration = getCurrentUserGigs()
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (querySnapshot != null) {
                    extractUpcomingGigs(querySnapshot)
                } else {
                }
            }
    }

    open fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    companion object {
        val COLLECTION_NAME = "Gigs"
    }


    init {
        loadData()
    }


    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot) {
        val userGigs: MutableList<Gig> = extractGigs(querySnapshot)

        val upcomingGigs = userGigs.filter {
            val gigStatus = GigStatus.fromGig(it)
            gigStatus == GigStatus.UPCOMING || gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW
        }.sortedBy {
            it.startDateTime.seconds
        }
        data.value = upcomingGigs
    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        return querySnapshot.documents.map { t ->
            t.toObject(Gig::class.java)!!
        }.toMutableList()
    }

    override fun loadData() {
        watchUpcomingGigs()
    }

    override fun getData(): LiveData<List<Gig>> {
        return data
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}