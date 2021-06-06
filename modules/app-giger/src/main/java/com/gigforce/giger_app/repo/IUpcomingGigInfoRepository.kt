package com.gigforce.giger_app.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.GigInfoCardDVM
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.MyGig
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.utils.Lce
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

interface IUpcomingGigInfoRepository {
    fun loadData()
    fun getData(): LiveData<List<MyGig>>
}

class UpcomingGigInfoRepository @Inject constructor() : IUpcomingGigInfoRepository,
    BaseFirestoreDBRepository() {
    private var data: MutableLiveData<List<MyGig>> = MutableLiveData()
    private val _upcomingGigs = MutableLiveData<List<MyGig>>()
    val upcomingGigs: LiveData<List<MyGig>> get() = _upcomingGigs
    private var dataNew: MutableLiveData<List<Any>> = MutableLiveData()
    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    companion object {
        val COLLECTION_NAME = "Gigs"
    }

    fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    init {
        loadData()
    }

     fun getUpcomingGigs(date : LocalDate){
        val activeDateTime = currentDateTime.value!!
        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

//         getCurrentUserGigs()
//             .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//
//                 if (querySnapshot != null) {
//                     extractUpcomingGigs(querySnapshot)
//                 } else {
//                     Log.d("errorHere", firebaseFirestoreException.toString())
//                 }
//             }

       getCollectionReference().whereEqualTo("gigerId", getUID()).whereLessThanOrEqualTo(
            "startDateTime",
            dateFull
        ).addSnapshotListener { value, error ->
            val tomorrow = date.plusDays(1)
           error?.let { 
               Log.d("errorData", it.toString())
           }
            value?.let {
                Log.d("errorData", it.documents?.toString())
                //var _data : List<Gig> = emptyList()
                var _data = ArrayList<MyGig>()
                val userGigs: MutableList<MyGig> = it?.documents?.map {
                    it.toObject(MyGig::class.java)!!
                }.toMutableList()

                Log.d("listData", userGigs.toString())

//                val upcomingGigs = userGigs.filter {
//                    val gigStatus = GigStatus.fromGig(it)
//                    gigStatus == GigStatus.UPCOMING || gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW
//                }.sortedBy {
//                    it.startDateTime.seconds
//                }


//                it.forEach { doc ->
//                    Log.d("document", doc.getString("gigStatus"))
//                    doc.toObject(Gig::class.java)?.let { data ->
//                        data.gigId = doc.id
////                        _data.add(data)
//                        Log.d("data", data.toString())
//                    }
//                }

                data.value = userGigs

//                _data = extractGigs(it)
//                    .filter {
//                        it.startDateTime > Timestamp.now()
//                                &&  it.endDateTime.toLocalDate().isBefore(tomorrow)
//                    }
//                Log.d("dataUpcoming", _data.toString())
//                data.value = _data
            }
        }


//        return extractGigs(querySnap)
//            .filter {
//                it.startDateTime > Timestamp.now()
//                        &&  it.endDateTime.toLocalDate().isBefore(tomorrow)
//            }
//
//
//
    }

//     fun getFirebaseReference() {
//        var calendar: Calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        getCollectionReference().whereEqualTo("gigerId", getUID()).whereGreaterThan(
//            "startDateTime",
//            calendar.time
//        ).orderBy("startDateTime")
//            .addSnapshotListener { value, error ->
//                value?.documents?.let { it ->
//
//                    val _data = ArrayList<Gig>()
//                    it.forEach { doc ->
//                        doc.toObject(Gig::class.java)?.let { data ->
//                            data.gigId = doc.id
//                            _data.add(data)
//                            Log.d("data", data.toString())
//                        }
//                    }
//                    data.value = _data
//                    Log.d("data", data.toString())
//                }
//            }
//    }

//    private fun extractUpcomingGigs(querySnapshot: QuerySnapshot) {
//        val userGigs: MutableList<Gig> = extractGigs(querySnapshot)
//
//        val upcomingGigs = userGigs.filter {
//            val gigStatus = GigStatus.fromGig(it)
//            gigStatus == GigStatus.UPCOMING || gigStatus == GigStatus.ONGOING || gigStatus == GigStatus.PENDING || gigStatus == GigStatus.NO_SHOW
//        }.sortedBy {
//            it.startDateTime.seconds
//        }
//        Log.d("upcomingHere", upcomingGigs.toString())
//        _upcomingGigs.value = upcomingGigs
//    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        return querySnapshot.documents.map { t ->
            t.toObject(Gig::class.java)!!
        }.toMutableList()
    }

    override fun loadData() {
        getUpcomingGigs(currentDateTime.value!!.toLocalDate())
    }

    override fun getData(): LiveData<List<MyGig>> {
        return data
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}