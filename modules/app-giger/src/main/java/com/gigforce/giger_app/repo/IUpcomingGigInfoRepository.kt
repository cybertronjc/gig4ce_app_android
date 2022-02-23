package com.gigforce.giger_app.repo

import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface IUpcomingGigInfoRepository {
    fun getData(): Flow<List<Gig>>
}

class UpcomingGigInfoRepository @Inject constructor() : IUpcomingGigInfoRepository,
    BaseFirestoreDBRepository() {

    companion object {
        const val COLLECTION_NAME = "Gigs"
    }


    override fun getData(): Flow<List<Gig>>{
        return callbackFlow {
            getCurrentUserGigs()
                .addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot != null) {
                        sendBlocking(extractUpcomingGigs(querySnapshot))
                    }
                }
            awaitClose{ }
        }
    }


    private fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot):List<Gig> {
        val userGigs: MutableList<Gig> = extractGigs(querySnapshot)

        val upcomingGigs = userGigs.filter {
            val gigStatus = GigStatus.fromGig(it)
            gigStatus == GigStatus.UPCOMING || gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW
        }.sortedBy {
            it.startDateTime.seconds
        }
        return upcomingGigs
    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        return querySnapshot.documents.map { t ->
            t.toObject(Gig::class.java)!!
        }.toMutableList()
    }



    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}