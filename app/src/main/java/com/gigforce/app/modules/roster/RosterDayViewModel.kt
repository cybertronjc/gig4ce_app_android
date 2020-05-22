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
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel: ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var userGigs = HashMap<String, ArrayList<Gig>>()

    lateinit var bsBehavior: BottomSheetBehavior<View>
    lateinit var UnavailableBS: View

    companion object {
        fun newInstance() = RosterDayViewModel()
    }

    init {
        userGigs["2020MAY22"] = ArrayList<Gig>(listOf(
            Gig(gigStatus = "upcoming", startHour = 9, startMinute = 30, duration = 3.5F),
            Gig(gigStatus = "completed", startHour = 4, startMinute = 0, duration = 4.0F)
        ))

        userGigs["2020MAY20"] = ArrayList<Gig>(listOf(
            Gig(gigStatus = "completed", startHour = 9, startMinute = 30, duration = 3.5F),
            Gig(gigStatus = "completed", startHour = 4, startMinute = 0, duration = 4.0F)
        ))

        userGigs["2020MAY24"] = ArrayList<Gig>(listOf(
            Gig(gigStatus = "upcoming", startHour = 13, startMinute = 30, duration = 4.5F),
            Gig(gigStatus = "upcoming", startHour = 4, startMinute = 0, duration = 4.0F)
        ))
    }

    fun getUpcomingGigsByDayTag(dayTag: String): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        for (dayWiseGigs in userGigs) {
            if (dayWiseGigs.key == dayTag) {
                Log.d("HourView", "Adding Gigs")
                for (gig in dayWiseGigs.value) {
                    if (gig.gigStatus == "upcoming") {
                        gig.tag = "${gig.startHour}${gig.startMinute}${gig.gigStatus}"
                        Log.d("HourView", "Upcoming gig called")
                        filteredGigs.add(gig)
                    }
                }
            }
        }
        return filteredGigs
    }

    fun getCompletedGigsByDayTag(dayTag: String): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        for (dayWiseGigs in userGigs) {
            if (dayWiseGigs.key == dayTag) {
                Log.d("HourView", "Adding Gigs")
                for (gig in dayWiseGigs.value) {
                    if (gig.gigStatus == "completed") {
                        gig.tag = "${gig.startHour}${gig.startMinute}${gig.gigStatus}"
                        Log.d("HourView", "Completed gig called")
                        filteredGigs.add(gig)
                    }
                }
            }
        }
        return filteredGigs
    }

    fun removeUpcomingGigCards(upcomingGigs: ArrayList<Gig>, parentView: ViewPager2) {
        for (gig in upcomingGigs) {
            var card = parentView.findViewWithTag<UpcomingGigCard>(gig.tag)
            parentView.removeView(card)
            Log.d("RosterViewModel", "gig card removed")

        }
    }
}