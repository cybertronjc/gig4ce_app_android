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
import java.time.format.DateTimeFormatter

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
        //queryGigs()
        gigsQuery.value = ArrayList(
            listOf(Gig(
                date = 30,
                month = 6,
                year = 2020,
                startHour = 10,
                startMinute = 0,
                duration = 8F,
                title = "Retail Sale executive",
                gigAmount = 0,
                gigStatus = "upcoming",
                isGigCompleted = false,
                isPaymentDone = false,
                gigRating = 0F
            ),
                Gig(
                    date = 29,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 0,
                    gigStatus = "upcoming",
                    isGigCompleted = false,
                    isPaymentDone = false,
                    gigRating = 0F
                ),
                Gig(
                    date = 28,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 1200,
                    gigStatus = "completed",
                    isGigCompleted = false,
                    isPaymentDone = true,
                    gigRating = 4.8F
                ),
                Gig(
                    date = 27,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 1200,
                    gigStatus = "completed",
                    isGigCompleted = false,
                    isPaymentDone = true,
                    gigRating = 4.0F
                ),
                Gig(
                    date = 26,
                    month = 6,
                    year = 2020,
                    startHour = 16,
                    startMinute = 0,
                    duration = 3F,
                    title = "Retail Sale executive",
                    gigAmount = 400,
                    gigStatus = "completed",
                    isGigCompleted = false,
                    isPaymentDone = true,
                    gigRating = 5.0F
                ),
                Gig(
                    date = 25,
                    month = 6,
                    year = 2020,
                    startHour = 16,
                    startMinute = 0,
                    duration = 3F,
                    title = "Retail Sale executive",
                    gigAmount = 400,
                    gigStatus = "completed",
                    isGigCompleted = false,
                    isPaymentDone = true,
                    gigRating = 5.0F
                ),
                Gig(
                    date = 24,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 0,
                    gigStatus = "upcoming",
                    isGigCompleted = false,
                    isPaymentDone = false,
                    gigRating = 0F
                ),
                Gig(
                    date = 23,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 0,
                    gigStatus = "completed",
                    isGigCompleted = true,
                    isPaymentDone = true,
                    gigRating = 4.8F
                ),
                Gig(
                    date = 22,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 1200,
                    gigStatus = "completed",
                    isGigCompleted = true,
                    isPaymentDone = true,
                    gigRating = 4.2F
                ),
                Gig(
                    date = 21,
                    month = 6,
                    year = 2020,
                    startHour = 10,
                    startMinute = 0,
                    duration = 8F,
                    title = "Retail Sale executive",
                    gigAmount = 1200,
                    gigStatus = "completed",
                    isGigCompleted = true,
                    isPaymentDone = true,
                    gigRating = 4.0F
                ),
                Gig(
                    date = 20,
                    month = 6,
                    year = 2020,
                    startHour = 16,
                    startMinute = 0,
                    duration = 3F,
                    title = "Retail Sale executive",
                    gigAmount = 400,
                    gigStatus = "completed",
                    isGigCompleted = true,
                    isPaymentDone = true,
                    gigRating = 5.0F
                ),
                Gig(
                    date = 19,
                    month = 6,
                    year = 2020,
                    startHour = 16,
                    startMinute = 0,
                    duration = 3F,
                    title = "Retail Sale executive",
                    gigAmount = 400,
                    gigStatus = "completed",
                    isGigCompleted = true,
                    isPaymentDone = true,
                    gigRating = 5.0F
                )
            )
        )
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
        val pattern = DateTimeFormatter.ofPattern("yyyyMdd")
        for (gig in gigsQuery) {
            //val idx = pattern.format(gig.startDateTime!!)
            val idx = gig.startDateTime!!.toLocalDate().format(pattern)
            if (dayTag == idx && gig.gigStatus == "upcoming")
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

    fun getCompletedGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val pattern = DateTimeFormatter.ofPattern("yyyyMdd")
        for (gig in gigsQuery) {
            //val idx = pattern.format(gig.startDateTime!!)
            val idx = gig.startDateTime!!.toLocalDate().format(pattern)
            if (dayTag == idx && gig.gigStatus == "completed" )
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

}