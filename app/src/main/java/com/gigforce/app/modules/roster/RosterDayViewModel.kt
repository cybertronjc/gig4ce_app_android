package com.gigforce.app.modules.roster

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel: ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var isDayAvailable: MutableLiveData<Boolean> = MutableLiveData(true)

    var gigsQuery: MutableLiveData<ArrayList<Gig>> = MutableLiveData<ArrayList<Gig>>()
    var userPref: MutableLiveData<PreferencesDataModel> = MutableLiveData<PreferencesDataModel>()
    var preferencesRepository = PreferencesRepository()

    var userGigs = HashMap<String, ArrayList<Gig>>()

    lateinit var bsBehavior: BottomSheetBehavior<View>
    lateinit var UnavailableBS: View

    lateinit var topBar: RosterTopBar

    fun queryGigs() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val collection = "Gigs"

        db.collection(collection).whereEqualTo("gigerId", uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                val userGigs = ArrayList<Gig>()
                querySnapshot?.documents?.forEach { t ->
                    Log.d("RosterViewModel", t.toString())
                    t.toObject(Gig::class.java)?.let { userGigs.add(it) }
                }
                gigsQuery.value = userGigs
            }
    }

    fun preferenceListener() {
        preferencesRepository.getDBCollection().addSnapshotListener { document, firebaseFirestoreException ->
            document?.let {
                userPref.postValue(it.toObject(PreferencesDataModel::class.java))
            }
        }
    }

    fun checkDayAvailable(date: LocalDateTime) {
        isDayAvailable.postValue(false)
        userPref.value ?.let {
            Log.d("RDVM", date.dayOfWeek.toString())
            var weekDays = it.selecteddays.map { item -> item.toUpperCase() }
            var weekEnds = it.selectedweekends.map { item -> item.toUpperCase() }
            Log.d("RDVM", weekDays.toString())
            isDayAvailable.postValue(
                weekDays.contains(date.dayOfWeek.toString()) || weekEnds.contains(date.dayOfWeek.toString()))
        }
    }

    companion object {
        fun newInstance() = RosterDayViewModel()
    }

    init {
        queryGigs()
        preferenceListener()
    }

    fun getUpcomingGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val format = SimpleDateFormat("yyyyMdd")
        for (gig in gigsQuery) {
            val idx = format.format(gig.startDateTime!!.toDate())
            if (dayTag == idx && gig.gigStatus == "upcoming")
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

    fun getCompletedGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val format = SimpleDateFormat("yyyyMdd")
        for (gig in gigsQuery) {
            val idx = format.format(gig.startDateTime!!.toDate())
            if (dayTag == idx && gig.gigStatus == "completed" )
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

}