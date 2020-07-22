package com.gigforce.app.modules.gigPage

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.utils.Lce
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.suspendCoroutine

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


    fun markAttendance(markAttendance: GigAttendance, gigId: String) {
        gigsRepository.markAttendance(markAttendance, gigId)
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

            if (it.endDateTime != null) {
                it.endDateTime!!.toDate().time > currentDate.time
            } else {

                it.startDateTime!!.toDate().time > currentDate.time
            }
        }.sortedBy {
            it.startDateTime!!.seconds
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
                        val gig = documentSnapshot.toObject(Gig::class.java)
                        gig?.gigId = documentSnapshot.id
                        gig!!
                    }.onSuccess {
                        _gigDetails.value = Lce.content(it)
                    }.onFailure {
                        _gigDetails.value = Lce.error(it.message!!)
                    }
                } else {
                    _gigDetails.value = Lce.error(firebaseFirestoreException!!.message!!)
                }
            }
    }

    fun favoriteGig(gigId: String) {
        gigsRepository.getCollectionReference()
            .document(gigId)
            .update("isFavourite", true)
    }

    fun unFavoriteGig(gigId: String) {
        gigsRepository.getCollectionReference()
            .document(gigId)
            .update("isFavourite", false)

    }

    override fun onCleared() {
        super.onCleared()
        mWatchUpcomingRepoRegistration?.remove()
        mWatchSingleGigRegistration?.remove()
    }

    fun updateWhatRatingYourReceived(gig: Gig, rating: Float) {

        gig.ratingUserReceived = rating
        gigsRepository.getCollectionReference()
            .document(gig.gigId)
            .set(gig)
    }

    fun submitGigFeedback(
        gigId: String,
        rating: Float,
        feedback: String,
        files: List<Uri>
    ) = viewModelScope.launch {

        try {
            val gig = gigsRepository.getGig(gigId)
            gig.gigRating = rating
            gig.gigUserFeedback = feedback

            //TODO upload gig feedback files
            gigsRepository.getCollectionReference()
                .document(gigId)
                .set(gig)
        } catch (e: Exception) {
            //Error while submitting feedback
        }
    }
}