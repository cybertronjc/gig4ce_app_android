package com.gigforce.app.modules.roster

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel: ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var isDayAvailable: MutableLiveData<Boolean> = MutableLiveData(true)

    var gigsQuery: MutableLiveData<ArrayList<Gig>> = MutableLiveData<ArrayList<Gig>>()

    var userGigs = HashMap<String, ArrayList<Gig>>()

    lateinit var bsBehavior: BottomSheetBehavior<View>
    lateinit var UnavailableBS: View

    lateinit var topBar: RosterTopBar

    fun queryGigs() {
        var db = FirebaseFirestore.getInstance()
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var collection = "Gigs"

        db.collection(collection).whereEqualTo("gigerId", uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                var userGigs = ArrayList<Gig>()
                if (querySnapshot != null) {
//                    gigsQuery.postValue(querySnapshot.documents.forEach { t -> t.data })
                    //Log.d("RosterViewModel", querySnapshot.documentstoString())
                    querySnapshot.documents.forEach { t ->
                        Log.d("RosterViewModel", t.toString())
                        t.toObject(Gig::class.java)?.let { userGigs.add(it) }
                    }

                }
                gigsQuery.value = userGigs
            }
    }

    companion object {
        fun newInstance() = RosterDayViewModel()
    }

    init {
        queryGigs()
//        userGigs["20200522"] = ArrayList<Gig>(listOf(
//            Gig(gigStatus = "upcoming", startHour = 9, startMinute = 30, duration = 3.5F),
//            Gig(gigStatus = "completed", startHour = 4, startMinute = 0, duration = 4.0F)
//        ))
//
//        userGigs["20200520"] = ArrayList<Gig>(listOf(
//            Gig(gigStatus = "completed", startHour = 9, startMinute = 30, duration = 3.5F),
//            Gig(gigStatus = "completed", startHour = 4, startMinute = 0, duration = 4.0F)
//        ))
//
//        userGigs["20200524"] = ArrayList<Gig>(listOf(
//            Gig(gigStatus = "upcoming", startHour = 13, startMinute = 30, duration = 4.5F),
//            Gig(gigStatus = "upcoming", startHour = 4, startMinute = 0, duration = 4.0F)
//        ))
//
//        userGigs["20200525"] = ArrayList<Gig>(listOf(
//                Gig(gigStatus = "completed", startHour = 11, startMinute = 45, duration = 2.5F),
//                Gig(gigStatus = "upcoming", startHour = 15, startMinute = 0, duration = 5.6F)
//        ))
    }

    fun getUpcomingGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val pattern = SimpleDateFormat("yyyyMdd")
        for (gig in gigsQuery) {
            val idx = pattern.format(gig.startDateTime!!.toDate())
//            Log.d("RosterViewModel", "gettign tag $idx")
//            Log.d("RosterViewModel", "dayTag $dayTag")
//            Log.d("RosterViewModel", "gig ${gig.toString()}")
            if (dayTag == idx && gig.gigStatus == "upcoming")
                filteredGigs.add(gig)
        }
//        for (dayWiseGigs in userGigs) {
//            if (dayWiseGigs.key == dayTag) {
//                Log.d("HourView", "Adding Gigs")
//                for (gig in dayWiseGigs.value) {
//                    if (gig.gigStatus == "upcoming") {
//                        gig.tag = "${gig.startHour}${gig.startMinute}${gig.gigStatus}"
//                        Log.d("HourView", "Upcoming gig called")
//                        filteredGigs.add(gig)
//                    }
//                }
//            }
//        }
        return filteredGigs
    }

    fun getCompletedGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val pattern = SimpleDateFormat("yyyyMdd")
        for (gig in gigsQuery) {
            val idx = pattern.format(gig.startDateTime!!.toDate())
//            Log.d("RosterViewModel", "gettign tag $idx")
//            Log.d("RosterViewModel", "dayTag $dayTag")
//            Log.d("RosterViewModel", "gig ${gig.toString()}")
            if (dayTag == idx && gig.gigStatus == "completed" )
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

}