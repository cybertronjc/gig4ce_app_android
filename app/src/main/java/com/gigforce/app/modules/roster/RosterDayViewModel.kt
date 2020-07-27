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
import com.gigforce.app.R
import com.gigforce.app.core.toDate
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.riningan.widget.ExtendedBottomSheetBehavior
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel: ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var isDayAvailable: MutableLiveData<Boolean> = MutableLiveData(true)

    private var userPref: MutableLiveData<PreferencesDataModel> = MutableLiveData<PreferencesDataModel>()
    var preferencesRepository = PreferencesRepository()

    var userGigs = HashMap<String, ArrayList<Gig>>()

    //lateinit var bsBehavior: BottomSheetBehavior<View>
    lateinit var bsBehavior: ExtendedBottomSheetBehavior<View>
    lateinit var UnavailableBS: View

    lateinit var topBar: RosterTopBar

    var allGigs: MutableLiveData<HashMap<String, ArrayList<Gig>>> = MutableLiveData(hashMapOf())

    fun getGigs(datetime: Date) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val collection = "Gigs"

        val c = Calendar.getInstance()
        c.time = datetime
        c.add(Calendar.DAY_OF_MONTH, 1)

        // if date already has snapshot listener, return
        if (getTagFromDate(datetime) in allGigs.value!!.keys)
            return
        else
            allGigs.value!!.put(
                getTagFromDate(datetime),
                ArrayList<Gig>())

        db.collection(collection)
            .whereEqualTo("gigerId", uid)
            .whereGreaterThanOrEqualTo("startDateTime", datetime)
            .whereLessThanOrEqualTo("startDateTime", c.time )
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                querySnapshot?.documentChanges?.forEach {
                    when (it.type) {
                        DocumentChange.Type.ADDED -> {
                            var gig = it.document.toObject(Gig::class.java)
                            allGigs.value!![getTagFromDate(gig.startDateTime!!.toDate())]!!.add(gig)
                            allGigs.value = allGigs.value
                        }
                        DocumentChange.Type.REMOVED -> {
                            var gig = it.document.toObject(Gig::class.java)
                            allGigs.value!![getTagFromDate(gig.startDateTime!!.toDate())]!!.remove(gig)
                            allGigs.value = allGigs.value
                        }
                        DocumentChange.Type.MODIFIED -> {
                            // TODO: See if needed to implement
                        }
                    }
                }
            }
    }


    fun preferenceListener() {
        preferencesRepository.getDBCollection().addSnapshotListener { document, firebaseFirestoreException ->
            document?.let {
                userPref.postValue(it.toObject(PreferencesDataModel::class.java))
            }
        }
    }

    fun resetDayTimeAvailability(
        viewModelCustomPreference: CustomPreferencesViewModel, parentView: ConstraintLayout) {
        val date = currentDateTime.value!!

        try {
            viewModelCustomPreference.customPreferencesDataModel
        } catch (e:UninitializedPropertyAccessException) {
            Log.d("DEBUG", "Returning from day time availability reset without performing action")
            return
        }

        setDayAvailability(date, viewModelCustomPreference)
        setHourAvailability(parentView, viewModelCustomPreference)
    }

    private fun setDayAvailability(
        date: LocalDateTime, viewModelCustomPreference: CustomPreferencesViewModel) {
        // check from preferences
        userPref.value ?.let {
            val weekDays = it.selecteddays.map { item -> item.toUpperCase(Locale.ROOT) }
            val weekEnds = it.selectedweekends.map { item -> item.toUpperCase(Locale.ROOT) }

            // set availability if day present in preferences
            isDayAvailable.postValue(
                weekDays.contains(date.dayOfWeek.toString()) ||
                        weekEnds.contains(date.dayOfWeek.toString()))
        }

        // check from custom preferences
        for (unavailable in viewModelCustomPreference.customPreferencesDataModel.unavailable) {
            if (date.toDate == unavailable.date)
                isDayAvailable.postValue(!unavailable.dayUnavailable)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setHourAvailability(
        parentView: ConstraintLayout, viewModelCustomPreference: CustomPreferencesViewModel) {
        val activeDateTime = currentDateTime.value!!
        val actualDateTime = LocalDateTime.now()
        if (isSameDate(activeDateTime, actualDateTime)) {
            todayHourActive(parentView, actualDateTime)
        }
        else if (isLessDate(activeDateTime, actualDateTime)) {
            allHourInactive(parentView)
        }
        else if (isMoreDate(activeDateTime, actualDateTime)) {
            allHourActive(parentView)
        }

        switchHourAvailability(activeDateTime, parentView, viewModelCustomPreference)
    }

     private fun switchHourAvailability(
        activeDateTime: LocalDateTime, parentView: ConstraintLayout,
        viewModelCustomPreference: CustomPreferencesViewModel) {
        viewModelCustomPreference.customPreferencesDataModel.unavailable.filter {
            it.date == activeDateTime.toDate
        }.forEach {
            it.timeSlots.forEach {
                selectedHourInactive(parentView, it.startTime, it.endTime)
            }
        }
    }

    fun switchDayAvailability(
        context: Context, parentView: ConstraintLayout, upcomingGigs: ArrayList<Gig>,
        currentDayAvailability: Boolean, viewModelCustomPreference: CustomPreferencesViewModel) {
        try {
            viewModelCustomPreference.customPreferencesDataModel
        } catch (e:UninitializedPropertyAccessException) {
            Toast.makeText(context, "UNINITIALIZED", Toast.LENGTH_SHORT).show()
            return
        }

        val activeDateTime = currentDateTime.value!!

        if (currentDayAvailability) {
            // today is active
            // make inactive
            val confirmCancellation = if (upcomingGigs.size > 0) showGigsTodayWarning(
                context, upcomingGigs, parentView) else true

            if (confirmCancellation) {
                isDayAvailable.postValue(false)

                val unavailable = UnavailableDataModel(activeDateTime.toDate)
                unavailable.dayUnavailable = true

                viewModelCustomPreference.updateCustomPreference(unavailable)
            }
        } else {
            // today is inactive
            // make active
            isDayAvailable.value = true

            val available = UnavailableDataModel(activeDateTime.toDate)
            available.dayUnavailable = false

            viewModelCustomPreference.updateCustomPreference(available)
        }
    }

    fun toggleHourUnavailable(
        context: Context, parentView: ConstraintLayout, upcomingGigs: ArrayList<Gig>,
        startDateTime: LocalDateTime, endDateTime: LocalDateTime,
            viewModelCustomPreference: CustomPreferencesViewModel) {
        viewModelCustomPreference.markUnavaialbleTimeSlots(
            UnavailableDataModel(startDateTime.toDate, endDateTime.toDate))
    }


    private fun selectedHourInactive(parentView: ConstraintLayout, startDateTime: Date, endDateTime: Date) {
        for (idx in 1..24) {
            val widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            if (widget.hour <= endDateTime.hours && widget.hour >= startDateTime.hours)
                widget.isDisabled = true
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
            "You have " + upcomingGigs.size.toString() +
                    " Gig(s) active on the day. Please cancel them individually."
        )

        dialog.cancel.setOnClickListener {
            flag = false
            dialog .dismiss()
        }

        dialog.yes.setOnClickListener {
            //flag = if (upcomingGigs.size > 0) showReasonForGigCancel(context, upcomingGigs, gigParentView) else true
            flag = true
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
    fun todayHourActive(parentView: ConstraintLayout, activeDateTime: LocalDateTime) {
        for (idx in 1..24) {
            val widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            widget.isDisabled = widget.hour <= activeDateTime.hour
        }
    }

    fun allHourActive(parentView: ConstraintLayout) {
        for (idx in 1..24) {
            val widget = parentView.findViewWithTag<HourRow>("hour_$idx")
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
        preferenceListener()
    }

    fun getTagFromDate(date: Date): String {
        val format = SimpleDateFormat("yyyyMMdd")
        return format.format(date)
    }

    fun getFilteredGigs(date: Date, filter: String): ArrayList<Gig> {
        val tag = getTagFromDate(date)
        val result = ArrayList<Gig>()

        if (tag in allGigs.value!!.keys) {
            allGigs.value!![tag]?.forEach {
                if (it.isUpcomingGig() && filter == "upcoming")
                    result.add(it)
                if (it.isPresentGig() && filter == "current")
                    result.add(it)
                if (it.isPastGig() && filter == "completed")
                    result.add(it)
                if (it.isFullDay && filter == "fullday")
                    result.add(it)
            }
        }
        return result
    }


    fun setFullDayGigs(context: Context) {
        val currentDate = currentDateTime.value!!
        val fullDayGig = getFilteredGigs(currentDate.toDate, "fullday")

        if (fullDayGig.size == 0)
            topBar.fullDayGigCard = null

        fullDayGig.forEach {
            if(it.isPastGig()) {
                val widget = CompletedGigCard(context)
                widget.isFullDay = true
                topBar.fullDayGigCard = widget
            } else if (it.isPresentGig()) {
                // TODO: Implement current day gig card
            } else if (it.isUpcomingGig()) {
                val widget = UpcomingGigCard(context)
                widget.isFullDay = true
                topBar.fullDayGigCard = widget
            } else {
                // TODO: Raise Error
            }
        }
    }
}