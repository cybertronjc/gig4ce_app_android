package com.gigforce.app.modules.roster

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import com.gigforce.app.core.toDate
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.riningan.widget.ExtendedBottomSheetBehavior
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel: ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var isDayAvailable: MutableLiveData<Boolean> = MutableLiveData(true)

    var gigsQuery: MutableLiveData<ArrayList<Gig>> = MutableLiveData<ArrayList<Gig>>()
    var userPref: MutableLiveData<PreferencesDataModel> = MutableLiveData<PreferencesDataModel>()
    var preferencesRepository = PreferencesRepository()

    var userGigs = HashMap<String, ArrayList<Gig>>()

    //lateinit var bsBehavior: BottomSheetBehavior<View>
    lateinit var bsBehavior: ExtendedBottomSheetBehavior<View>
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

    fun checkDayAvailable(date: LocalDateTime, viewModelCustomPreference: CustomPreferencesViewModel) {
        isDayAvailable.postValue(false)
        userPref.value ?.let {
            Log.d("RDVM", date.dayOfWeek.toString())
            val weekDays = it.selecteddays.map { item -> item.toUpperCase() }
            val weekEnds = it.selectedweekends.map { item -> item.toUpperCase() }
            Log.d("RDVM", weekDays.toString())
            isDayAvailable.postValue(
                weekDays.contains(date.dayOfWeek.toString()) || weekEnds.contains(date.dayOfWeek.toString()))

            // check if it was set from custom unavailable
            for (unavailable in viewModelCustomPreference.customPreferencesDataModel.unavailable) {
                if (date.toDate == unavailable.date)
                    isDayAvailable.postValue(unavailable.dayUnavailable)
            }

        }
    }

    fun toggleDayAvailability(
        context: Context, parentView: ConstraintLayout, upcomingGigs: ArrayList<Gig>, currentDayAvailability: Boolean,
            activeDateTime: LocalDateTime, actualDateTime: LocalDateTime, viewModelCustomPreference: CustomPreferencesViewModel) {
        Log.d("RDVM", currentDayAvailability.toString())
        if (currentDayAvailability) {
            // currently available, switch to unavailable
            val confirmCancellation = if (upcomingGigs.size > 0) showGigsTodayWarning(
                context, upcomingGigs, parentView) else true

            if (confirmCancellation) {
                isDayAvailable.value = false
                allHourInactive(parentView)

                viewModelCustomPreference.updateCustomPreference(
                    UnavailableDataModel(
                        Date.from(activeDateTime.atZone(ZoneId.systemDefault()).toInstant())
                    )
                )
            }

        } else {
            // currently unavailable, switch to available
            isDayAvailable.value = true
            setHourVisibility(parentView, activeDateTime, actualDateTime)

            var available =
                    UnavailableDataModel(
                            Date.from(activeDateTime.atZone(ZoneId.systemDefault()).toInstant())
                    )

            available.dayUnavailable = true

            viewModelCustomPreference.updateCustomPreference(
                    available
            )
        }

    }

    fun toggleHourUnavailable(
        context: Context, parentView: ConstraintLayout, upcomingGigs: ArrayList<Gig>,
        startDateTime: LocalDateTime, endDateTime: LocalDateTime,
            viewModelCustomPreference: CustomPreferencesViewModel) {
        viewModelCustomPreference.markUnavaialbleTimeSlots(UnavailableDataModel(startDateTime.toDate, endDateTime.toDate))
        selectedHourInactive(parentView, startDateTime.toDate, endDateTime.toDate)
    }

    fun switchHourAvailability(activeDateTime: LocalDateTime, parentView: ConstraintLayout, viewModelCustomPreference: CustomPreferencesViewModel) {
        viewModelCustomPreference.customPreferencesDataModel.unavailable.filter {
            it.date == activeDateTime.toDate
        }.forEach {
            it.timeSlots.forEach {
                selectedHourInactive(parentView, it.startTime, it.endTime)
            }
        }
    }

    fun selectedHourInactive(parentView: ConstraintLayout, startDateTime: Date, endDateTime: Date) {
        for (idx in 1..24) {
            var widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            widget.isDisabled = widget.hour <= endDateTime.hours && widget.hour >= startDateTime.hours
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showGigsTodayWarning(context: Context, upcomingGigs: ArrayList<Gig>, gigParentView: ConstraintLayout): Boolean {
        var flag = false

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.gigs_today_warning_dialog)

        dialog.dialog_content.setText(
            "You have " + upcomingGigs.size.toString() + " Gig(s) active on the day. Please cancel them individually."
        )

        dialog.cancel.setOnClickListener {
            flag = false
            dialog .dismiss()
        }

        dialog.yes.setOnClickListener {
            Toast.makeText(context, "Clicked on Yes", Toast.LENGTH_SHORT).show()
            flag = if (upcomingGigs.size > 0) showReasonForGigCancel(context, upcomingGigs, gigParentView) else true
            dialog .dismiss()
        }

        dialog.show()
        return flag
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showReasonForGigCancel(context: Context, upcomingGigs: ArrayList<Gig>, gigParentView: ConstraintLayout): Boolean {
        var flag = false
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)

        var selectedText = ""

        dialog.cancel_options.setOnCheckedChangeListener ( RadioGroup.OnCheckedChangeListener { group, checkedId ->
            selectedText = dialog.findViewById<RadioButton>(checkedId).text.toString()
        })

        dialog.submit_button.setOnClickListener {
            Toast.makeText(context, "selected option " + selectedText, Toast.LENGTH_SHORT).show()

            // removing upcoming gigs
            // TODO: Can take this functioni out
//            for (gig in upcomingGigs)
//                gigParentView.removeView(gigParentView.findViewWithTag<UpcomingGigCard>(gig.tag))
//                child.findViewWithTag<ConstraintLayout>("day_times").removeView(child.findViewWithTag<UpcomingGigCard>(gig.tag))
            flag = true
            allHourInactive(gigParentView)
            dialog .dismiss()
        }

        dialog.cancel_button.setOnClickListener {
            flag = false
            dialog .dismiss()
        }

        dialog.show()
        return flag
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setHourVisibility(parentView: ConstraintLayout, activeDateTime: LocalDateTime, actualDateTime: LocalDateTime) {
        if (isSameDate(activeDateTime, actualDateTime)) {
            todayHourActive(parentView, actualDateTime)
        }
        else if (isLessDate(activeDateTime, actualDateTime)) {
            allHourInactive(parentView)
        }
        else if (isMoreDate(activeDateTime, actualDateTime)) {
            allHourActive(parentView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun todayHourActive(parentView: ConstraintLayout, activeDateTime: LocalDateTime) {
        for (idx in 1..24) {
            var widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            widget.isDisabled = widget.hour <= activeDateTime.hour
        }
    }

    fun allHourActive(parentView: ConstraintLayout) {
        for (idx in 1..24) {
            var widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            widget.isDisabled = false
        }
    }

    fun allHourInactive(parentView: ConstraintLayout) {
        for (idx in 1..24) {
            val widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            Log.d("HOURVIEW", "inactive hour")
            widget.isDisabled = true
        }
    }

    companion object {
        fun newInstance() = RosterDayViewModel()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun isSameDate(compareWith: LocalDateTime, compareTo: LocalDateTime): Boolean {
        return (compareWith.year == compareTo.year) &&
                (compareWith.monthValue == compareTo.monthValue) &&
                (compareWith.dayOfMonth == compareTo.dayOfMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isLessDate(compareWith: LocalDateTime, compareTo: LocalDateTime):Boolean {
        return (compareWith.year < compareTo.year) ||
                ((compareWith.year == compareTo.year) && (compareWith.monthValue < compareTo.monthValue)) ||
                ((compareWith.year == compareTo.year) &&
                        (compareWith.monthValue == compareTo.monthValue) &&
                        (compareWith.dayOfMonth < compareTo.dayOfMonth))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isMoreDate(compareWith: LocalDateTime, compareTo: LocalDateTime): Boolean {
        return !isLessDate(compareWith, compareTo) && !isSameDate(compareWith, compareTo)
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
            if (dayTag == idx && gig.gigStatus == "upcoming" && !gig.isFullDay)
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

    fun getCompletedGigsByDayTag(dayTag: String, gigsQuery: ArrayList<Gig>): ArrayList<Gig> {
        val filteredGigs = ArrayList<Gig>()
        val format = SimpleDateFormat("yyyyMdd")
        for (gig in gigsQuery) {
            val idx = format.format(gig.startDateTime!!.toDate())
            if (dayTag == idx && gig.gigStatus == "completed" && !gig.isFullDay )
                filteredGigs.add(gig)
        }
        return filteredGigs
    }

    fun getFullDayGigForDate(date: LocalDateTime, gigsQuery: ArrayList<Gig>): Gig? {
        val format = SimpleDateFormat("yyyyMdd")
        val dayTag = format.format(date.toDate)
        for (gig in gigsQuery) {
            val idx = format.format(gig.startDateTime!!.toDate())
            if (dayTag == idx && gig.isFullDay) {
                return gig
            }
        }
        return null

    }

}