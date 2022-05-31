package com.gigforce.giger_gigs.gighistory

import android.util.Log
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.app.data.repositoriesImpl.gigs.GigService
import com.gigforce.core.fb.BaseFirestoreDBRepository
//import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.DocumentChange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GigHistoryRepository @Inject constructor(
    private val gigsService: GigService
) : BaseFirestoreDBRepository(), DataCallbacks {

    override suspend fun getOnGoingGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        initialLoading: Boolean
    ) {
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

        try {
            val gigs = gigsService.getGigsForDate(
                dateInYYYMMDD = dateFormatter.format(LocalDate.now())
            ).bodyOrThrow().map {
                it.toGig()
            }

            responseCallbacks.onGoingGigsResponse(
                gigs,
                null,
                initialLoading
            )
        } catch (e: Exception) {
            responseCallbacks.onGoingGigsResponse(
                null,
                e,
                initialLoading
            )
        }
    }


    override suspend fun getPastGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        offset: Long,
        limit: Long
    ) {

        Log.d("TAG", "Get Past called ,offset =  $offset")
        try {
            val gigs = gigsService.getPastGigs(
                offset = offset,
                limit = limit
            ).bodyOrThrow().map {
                it.toGig()
            }

            responseCallbacks.pastGigsResponse(
                gigs,
                null
            )
        } catch (e: Exception) {
            responseCallbacks.pastGigsResponse(
                null,
                e
            )
        }

    }

    override suspend fun getUpComingGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        offset: Long,
        limit: Long
    ) {

        Log.d("TAG", "Get Upcoming called ,offset =  $offset")
        try {
            val gigs = gigsService.getUpcomingGigs(
                offset = offset,
                limit = limit
            ).bodyOrThrow().map {
                it.toGig()
            }

            responseCallbacks.upcomingGigsResponse(
                gigs,
                null
            )
        } catch (e: Exception) {
            responseCallbacks.upcomingGigsResponse(
                null,
                e
            )
        }
    }


    override fun observeDocumentChanges(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
//                        DocumentChange.Type.ADDED -> {
//                            responseCallbacks.docChange(DocumentChange.Type.ADDED, dc)
//                        }
                        DocumentChange.Type.MODIFIED -> {
                            responseCallbacks.docChange(DocumentChange.Type.MODIFIED, dc)
                        }
                        DocumentChange.Type.REMOVED -> {
                            responseCallbacks.docChange(DocumentChange.Type.REMOVED, dc)
                        }
                    }
                }
            }
    }

    override fun checkGigsCount(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .limit(1).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.gigsCountResponse(querySnapshot, firebaseFirestoreException);
            }

    }


    override fun getCollectionName() =
        COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Gigs"

    }
}