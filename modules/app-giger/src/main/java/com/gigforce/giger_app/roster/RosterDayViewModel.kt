package com.gigforce.giger_app.roster

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.configrepository.ConfigDataModel
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.common_ui.repository.prefrepo.PreferencesRepository
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewmodels.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.core.datamodels.custom_gig_preferences.UnavailableDataModel
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.user_preferences.PreferencesDataModel
import com.gigforce.core.extensions.px
import com.gigforce.core.extensions.toDate
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.giger_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.O)
class RosterDayViewModel constructor(
        private val gigsRepository: GigsRepository = GigsRepository()
) : ViewModel() {

    var currentDateTime: MutableLiveData<LocalDateTime> = MutableLiveData(LocalDateTime.now())

    var isDayAvailable: MutableLiveData<Boolean> = MutableLiveData(true)

    var showDeclineGigDialog: MutableLiveData<Boolean> = MutableLiveData()

    private var userPref: MutableLiveData<PreferencesDataModel> =
            MutableLiveData<PreferencesDataModel>()
    var preferencesRepository =
            PreferencesRepository()

//    var dayContext: Context? = null

    var userGigs = HashMap<String, ArrayList<Gig>>()

    var isLoadedFirstTime = true

    var itemHeight = 70
    var nestedScrollView: NestedScrollView? = null


    //lateinit var bsBehavior: BottomSheetBehavior<View>
//    lateinit var bsBehavior: ExtendedBottomSheetBehavior<View>
//    lateinit var UnavailableBS: View

    var topBar: RosterTopBar? = null

    var upcomingGigs = ArrayList<Gig>()
    var completedGigs = ArrayList<Gig>()
    var currentGigs = ArrayList<Gig>()
    var fulldayGigs = ArrayList<Gig>()

    var allGigs: HashMap<String, MutableLiveData<ArrayList<Gig>>> = hashMapOf()

    fun getGigs(datetime: Date) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val collection = "Gigs"

        // we get the given date at 00:00 hours
        // get the next date at 00:00 hours
        // fetch all gigs for user between these date
        var cal = Calendar.getInstance()
        cal.time = datetime
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        var startDate = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 1)

        var endDate = cal.time

        db.collection(collection)
                .whereEqualTo("gigerId", uid)
                .whereGreaterThanOrEqualTo("startDateTime", startDate)
                .whereLessThanOrEqualTo("startDateTime", endDate)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    val tag = getTagFromDate(datetime)
                    var added = ArrayList<Gig>()
                    var removed = ArrayList<Gig>()
                    var modified = ArrayList<Gig>()

                    querySnapshot?.documentChanges?.forEach {
                        when (it.type) {
                            DocumentChange.Type.ADDED -> {
                                val gig = it.document.toObject(Gig::class.java)
                                added.add(gig)
//                            allGigs[tag]!!.value!!.add(gig)
//                            allGigs[tag]!!.value = allGigs[tag]!!.value
                            }
                            DocumentChange.Type.REMOVED -> {
                                val gig = it.document.toObject(Gig::class.java)
                                removed.add(gig)
//                            allGigs[tag]!!.value!!.remove(gig)
//                            allGigs[tag]!!.value = allGigs[tag]!!.value
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val gig = it.document.toObject(Gig::class.java)
                                modified.add(gig)
                            }
                        }
                    }

                    added.retainAll { GigStatus.fromGig(it) != GigStatus.DECLINED && GigStatus.fromGig(it) != GigStatus.CANCELLED }
                    removed.retainAll { GigStatus.fromGig(it) != GigStatus.DECLINED && GigStatus.fromGig(it) != GigStatus.CANCELLED }
                    modified.retainAll { GigStatus.fromGig(it) != GigStatus.DECLINED && GigStatus.fromGig(it) != GigStatus.CANCELLED }

                    allGigs[tag]!!.value!!.addAll(added)
                    allGigs[tag]!!.value!!.removeAll(removed)

                    var modifiedKeys = ArrayList<String>()
                    modified.forEach {
                        modifiedKeys.add(it.gigId)
                    }
                    allGigs[tag]!!.value!!.removeIf {
                        modifiedKeys.contains(it.gigId)
                    }
                    allGigs[tag]!!.value!!.addAll(modified)

                    allGigs[tag]!!.value = allGigs[tag]!!.value

                }
    }


    fun preferenceListener() {
        preferencesRepository.getDBCollection()
                .addSnapshotListener { document, firebaseFirestoreException ->
                    document?.let {
                        userPref.postValue(it.toObject(PreferencesDataModel::class.java))
                    }
                }
    }

    fun resetDayTimeAvailability(
            viewModelCustomPreference: CustomPreferencesViewModel, parentView: ConstraintLayout,
            config: ConfigDataModel?
    ) {
        val date = currentDateTime.value!!

        try {
            viewModelCustomPreference.customPreferencesDataModel
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("DEBUG", "Returning from day time availability reset without performing action")
            return
        }

        var dayAvailable = setDayAvailability(date, viewModelCustomPreference)
        setHourAvailability(date, dayAvailable, parentView, viewModelCustomPreference, config)
    }

    private fun setDayAvailability(
            date: LocalDateTime, viewModelCustomPreference: CustomPreferencesViewModel
    ): Boolean {
        var dayAvailable = false
        // check from preferences
        userPref.value?.let {
            val weekDays = it.selecteddays.map { item -> item.toUpperCase(Locale.ROOT) }
            val weekEnds = it.selectedweekends.map { item -> item.toUpperCase(Locale.ROOT) }

            // set availability if day present in preferences
            dayAvailable = weekDays.contains(date.dayOfWeek.toString()) ||
                    weekEnds.contains(date.dayOfWeek.toString())
        }

        // check from custom preferences
        dayAvailable = true

        for (unavailable in viewModelCustomPreference.customPreferencesDataModel.unavailable) {
            if (date.toLocalDate().equals(unavailable.date.toLocalDate())) {
                dayAvailable = !unavailable.dayUnavailable
            }
        }


        isDayAvailable.postValue(dayAvailable)
        return dayAvailable
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setHourAvailability(
            activeDateTime: LocalDateTime, dayAvailable: Boolean, parentView: ConstraintLayout,
            viewModelCustomPreference: CustomPreferencesViewModel, config: ConfigDataModel?
    ) {
        val actualDateTime = LocalDateTime.now()
        if (isSameDate(activeDateTime, actualDateTime)) {
            todayHourActive(parentView, actualDateTime)
        } else if (isLessDate(activeDateTime, actualDateTime)) {
            allHourInactive(parentView)
        } else if (isMoreDate(activeDateTime, actualDateTime)) {
            allHourActive(parentView)
        }

        switchHourAvailability(activeDateTime, parentView, viewModelCustomPreference)
        switchDefaultHourAvailability(parentView, config)

        if (!dayAvailable) {
            allHourInactive(parentView)
        }
    }

    private fun switchHourAvailability(
            activeDateTime: LocalDateTime, parentView: ConstraintLayout,
            viewModelCustomPreference: CustomPreferencesViewModel
    ) {
        viewModelCustomPreference.customPreferencesDataModel.unavailable.filter {
            it.date == activeDateTime.toDate
        }.forEach {
            it.timeSlots.forEach {
                selectedHourInactive(parentView, it.startTime, it.endTime)
            }
        }
    }

    private fun switchDefaultHourAvailability(
            parentView: ConstraintLayout,
            config: ConfigDataModel?
    ) {
        var selectedSlots: ArrayList<Int> = ArrayList()
        config?.let {
            userPref.value?.let { pref ->
                pref.selectedslots.forEach { slot ->
                    var slotId = slot.toInt()
                    selectedSlots.add(slotId)
                }
            }

            for (idx in 0 until it.time_slots.size) {
                Log.d("RosterDayViewModel", idx.toString())
                if (!selectedSlots.contains(idx + 1)) {
                    selectedHourInactive(
                            parentView,
                            it.time_slots[idx].start_time_slot!!,
                            it.time_slots[idx].end_time_slot!!
                    )
                }

            }
        }
    }

    private fun confirmCancellation(
            activeDateTime: LocalDateTime,
            viewModelCustomPreference: CustomPreferencesViewModel
    ) {

        Log.d("SwitchDayAvailability", "Cancellation is confirmed")
        isDayAvailable.postValue(false)

        val unavailable =
                UnavailableDataModel(
                        activeDateTime.toDate
                )
        unavailable.dayUnavailable = true

        viewModelCustomPreference.updateCustomPreference(unavailable)
    }

    fun switchDayAvailability(
            context: Context, parentView: ConstraintLayout, currentDayAvailability: Boolean,
            viewModelCustomPreference: CustomPreferencesViewModel
    ) = viewModelScope.launch {
//        try {
//            viewModelCustomPreference.customPreferencesDataModel
//        } catch (e:UninitializedPropertyAccessException) {
////            Toast.makeText(context, "UNINITIALIZED", Toast.LENGTH_SHORT).show()
//            return
//        }

        Log.d("SwitchDayAvailability", "Entered")
        Log.d("SwitchDayAvailability", "Day availability is " + currentDayAvailability.toString())

        val activeDateTime = currentDateTime.value!!


        Log.d("SwitchDayAvailability", "Active date time is " + activeDateTime.toString())

        if (currentDayAvailability) {
            // today is active
            // make inactive


            val upcomingActiveGigs =
                    gigsRepository.getTodaysUpcomingGigs(activeDateTime.toLocalDate())

            Log.d("SwitchDayAvailability", "Trying to mark inactive")
            val confirmCancellation = if (upcomingActiveGigs.size > 0) showGigsTodayWarning(
                    context,
                    upcomingGigs,
                    upcomingActiveGigs.size,
                    parentView,
                    activeDateTime,
                    viewModelCustomPreference
            ) else true

            if (confirmCancellation) {

                Log.d("SwitchDayAvailability", "Cancellation is confirmed")
                isDayAvailable.postValue(false)

                val unavailable =
                        UnavailableDataModel(
                                activeDateTime.toDate
                        )
                unavailable.dayUnavailable = true

                viewModelCustomPreference.updateCustomPreference(unavailable)
            }
        } else {
            // today is inactive
            // make active
            Log.d("SwitchDayAvailability", "Marking day available ")
            isDayAvailable.value = true

            val available =
                    UnavailableDataModel(
                            activeDateTime.toDate
                    )
            available.dayUnavailable = false

            viewModelCustomPreference.updateCustomPreference(available)
        }
    }

    fun toggleHourUnavailable(
            context: Context, parentView: ConstraintLayout, upcomingGigs: ArrayList<Gig>,
            startDateTime: LocalDateTime, endDateTime: LocalDateTime,
            viewModelCustomPreference: CustomPreferencesViewModel
    ) {
        viewModelCustomPreference.markUnavaialbleTimeSlots(
                UnavailableDataModel(
                        startDateTime.toDate,
                        endDateTime.toDate
                )
        )
    }


    private fun selectedHourInactive(
            parentView: ConstraintLayout,
            startDateTime: Date,
            endDateTime: Date
    ) {
        for (idx in 1..24) {
            val widget = parentView.findViewWithTag<HourRow>("hour_$idx")
            if (widget.hour <= endDateTime.hours && widget.hour >= startDateTime.hours)
                widget.isDisabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showGigsTodayWarning(
            context: Context,
            upcomingGigs: ArrayList<Gig>,
            upcomingGigsCount: Int,
            gigParentView: ConstraintLayout,
            activeDateTime: LocalDateTime, viewModelCustomPreference: CustomPreferencesViewModel
    ): Boolean {
        var flag = false


        Log.d("SwitchDayAvailability", "Entered ShowGigsTodayWarning")

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.gigs_today_warning_dialog)

        dialog.dialog_content.text = "You have $upcomingGigsCount Gig(s) active on the day. Please cancel them individually."

        dialog.cancel.setOnClickListener {
            flag = false
            dialog.dismiss()
        }

        dialog.yes.setOnClickListener {

            showDeclineGigDialog.value = true
            //flag = if (upcomingGigs.size > 0) showReasonForGigCancel(context, upcomingGigs, gigParentView) else true
            confirmCancellation(activeDateTime, viewModelCustomPreference)
            dialog.dismiss()
        }

        dialog.show()
        return flag
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showReasonForGigCancel(
            context: Context,
            upcomingGigs: ArrayList<Gig>,
            gigParentView: ConstraintLayout
    ): Boolean {
        var flag = false
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)

        var selectedText = ""

        dialog.cancel_options.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
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
            dialog.dismiss()
        }

        dialog.cancel_button.setOnClickListener {
            flag = false
            dialog.dismiss()
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
    fun isLessDate(compareWith: LocalDateTime, compareTo: LocalDateTime): Boolean {
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

        if (tag in allGigs.keys) {
            allGigs[tag]!!.value?.forEach {
                if (it.isUpcomingGig() && filter == "upcoming" && !it.isFullDay)
                    result.add(it)
                if (it.isPresentGig() && filter == "current" && !it.isFullDay)
                    result.add(it)
                if (it.isPastGig() && filter == "completed" && !it.isFullDay)
                    result.add(it)
                if (it.isFullDay && filter == "fullday")
                    result.add(it)
            }
        }
        return result
    }


    fun setFullDayGigs() {
        val currentDate = currentDateTime.value!!
        val fullDayGig = getFilteredGigs(currentDate.toDate, "fullday")

        if (fullDayGig.size == 0)
            topBar?.fullDayGigCard = null
        if (topBar == null) return
        fullDayGig.forEach {
            if (it.isPastGig()) {
                val widget = CompletedGigCard(
                        topBar!!.context,
                        title = it.getGigTitle(),
                        gigSuccess = it.isGigCompleted,
                        paymentSuccess = it.isPaymentDone,
                        rating = it.gigRating,
                        amount = it.gigAmount,
                        duration = 0.0F,
                        cardHeight = itemHeight.px,
                        isFullDay = true,
                        gigId = it.gigId,
                        isNewgigPage = it.openNewGig(),
                        startDateTime = it.startDateTime,
                        endDateTime = it.endDateTime
                )
                topBar!!.fullDayGigCard = widget
            } else if (it.isPresentGig()) {
                // TODO: Implement current day gig card
                val widget = CurrentGigCard(
                        topBar!!.context,
                        title = it.getGigTitle(),
                        startHour = it.startHour,
                        startMinute = it.startMinute,
                        duration = 0.0F,
                        cardHeight = itemHeight.px,
                        isFullDay = true,
                        gigId = it.gigId,
                        isNewGigPage = it.openNewGig(),
                        startDateTime = it.startDateTime,
                        endDateTime = it.endDateTime
                )
                topBar!!.fullDayGigCard = widget
            } else if (it.isUpcomingGig()) {
                val widget = UpcomingGigCard(
                        topBar!!.context,
                        title = it.getGigTitle(),
                        startHour = it.startHour,
                        startMinute = it.startMinute,
                        duration = 0.0F,
                        cardHeight = itemHeight.px,
                        isFullDay = true,
                        gigId = it.gigId,
                        isNewGigPage = it.openNewGig(),
                        startDateTime = it.startDateTime,
                        endDateTime = it.endDateTime
                )
                topBar!!.fullDayGigCard = widget
            } else {
                // TODO: Raise Error
            }
        }
    }

    fun scrollToPosition(date: Date) {
        Log.d("RosterDayFragment", "called")

        val gigs = getFilteredGigs(date, "upcoming")

        nestedScrollView?.let {
            gigs.let { it1 ->
                if (it1.size != 0) {
                    val sortedUpcomingGigs = gigs.sortedBy { gig -> gig.startHour }
                    it.scrollTo(
                            0,
                            ((sortedUpcomingGigs[0].startHour - 4) * itemHeight).px
                    )
                } else {
                    it.scrollTo(0, (8 * itemHeight).px)
                }
            }
        }
    }
}