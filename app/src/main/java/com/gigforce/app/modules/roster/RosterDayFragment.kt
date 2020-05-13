package com.gigforce.app.modules.roster

import android.app.ActionBar
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.gigforce.app.R
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.card.MaterialCardView
import com.google.type.Color
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class RosterDayFragment: RosterBaseFragment() {

    var TAG: String = "DEBUGDAYVIEW"

    var upcomingGigs: ArrayList<Gig> = ArrayList<Gig>()
    var unavailableCards: ArrayList<String> = ArrayList()

    val marginCardStart = 85.px
    val marginCardEnd = 16.px

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roster_day_fragment, inflater, container)

        return getFragmentView()
    }

    // TODO: Modify to get this height from dimens
    val itemHeight = 70

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initialize()
        setListeners()

        var sampleGigUpcoming = Gig(startHour = 13, startMinute = 30, duration = 3.5F)
        var sampleGigCompleted = Gig(startHour = 9, duration = 4.0F)

        upcomingGigs.add(sampleGigUpcoming)

        addUpcomingGigCard(sampleGigUpcoming)

        addUnAvailableCard(5, 3.0F)

        addCompletedGigCard(sampleGigCompleted)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        initializeHourViews()
        setCurrentTimeDivider()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeHourViews() {
        val times = ArrayList<String>()
        times.addAll(listOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
            "21:00", "22:00", "23:00", "24:00"))

        val timeViewGroup = day_times
        val hourIds = ArrayList<Int>()
        var widget: MaterialCardView
        val constraintSet: ConstraintSet?

        // Adding hourly widgets
        for ((index, time) in times.withIndex()) {
            widget = HourRow(this.requireContext())
            widget.id = View.generateViewId()
            widget.hour = index + 1
            widget.time = time
            widget.setOnClickListener {
                Toast.makeText(requireContext(), "Clicked on hour " + widget.hour.toString(), Toast.LENGTH_SHORT).show()
            }

            timeViewGroup.addView(widget)

            hourIds.add(widget.id)
            Log.d("PreviousID", widget.id.toString())

        }
        // Adding constraints for hourly widgets
        constraintSet = ConstraintSet()
        constraintSet.clone(timeViewGroup)
        for ((index,idx) in hourIds.withIndex()) {
            if (index == 0) {
                constraintSet.connect(idx, ConstraintSet.TOP, hour_0.id, ConstraintSet.BOTTOM, 0)
            } else {
                constraintSet.connect(idx, ConstraintSet.TOP, hourIds[index - 1], ConstraintSet.BOTTOM, 0)
            }
            constraintSet.connect(idx, ConstraintSet.START, start_guideline.id, ConstraintSet.START)
            constraintSet.connect(idx, ConstraintSet.END, end_guideline.id, ConstraintSet.START)

            Log.d("Constraint", "applied")
        }
        constraintSet.applyTo(timeViewGroup)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCurrentTimeDivider() {
        val datetime = LocalDateTime.now()
        // set current time divider
        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, marginTop, 0, 0)
        current_time_divider.requestLayout()
    }

    private fun addUpcomingGigCard(gig: Gig) {
        val upcomingCard = UpcomingGigCard(requireContext())

        day_times.addView(upcomingCard)

        upcomingCard.id = View.generateViewId()
        upcomingCard.startHour = gig.startHour
        upcomingCard.startMinute = gig.startMinute
        upcomingCard.duration = gig.duration
        upcomingCard.cardHeight = (itemHeight * gig.duration).toInt().px

        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        upcomingCard.setLayoutParams(params)

        upcomingCard.tag = "gig_" + upcomingCard.startHour.toString() + "_" + upcomingCard.startMinute.toString()
        var marginTop = (upcomingCard.startHour * itemHeight + ((upcomingCard.startMinute/60.0F)*itemHeight).toInt()).px

        var constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(upcomingCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart)
        constraintSet.connect(upcomingCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START, marginCardEnd)
        constraintSet.connect(upcomingCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)

        upcomingCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on upcoming card", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUnAvailableCard(startHour: Int, duration: Float) {
        // Sample attachment of unavailable card
        val unavailableCard = UnavailableCard(this.requireContext())
        unavailableCard.id = View.generateViewId()
        unavailableCard.unavailableStartHour = startHour
        unavailableCard.unavailableStartMinute = 0
        unavailableCard.unavailableDuration = duration
        unavailableCard.tag = "gig_" + unavailableCard.unavailableStartHour.toString() + "_" + unavailableCard.unavailableStartMinute
        unavailableCard.cardHeight = (itemHeight * duration).toInt().px

        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        unavailableCard.setLayoutParams(params)
        val marginTop = (unavailableCard.unavailableStartHour * itemHeight + ((unavailableCard.unavailableStartMinute/60.0F)*itemHeight).toInt()).px

        day_times.addView(unavailableCard)

        unavailableCards.add(unavailableCard.tag.toString())

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(unavailableCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart)
        constraintSet.connect(unavailableCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START)
        constraintSet.connect(unavailableCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)

        unavailableCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on unavailable card", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCompletedGigCard(gig: Gig) {
        var completedGigCard = CompletedGigCard(requireContext())
        completedGigCard.gigStartHour = gig.startHour
        completedGigCard.gigStartMinute = gig.startMinute
        completedGigCard.gigDuration = gig.duration
        completedGigCard.cardHeight = (itemHeight * gig.duration).toInt().px
        completedGigCard.id = View.generateViewId()

        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        completedGigCard.setLayoutParams(params)
        completedGigCard.tag = "gig_" + completedGigCard.gigStartHour.toString() + "_" + completedGigCard.gigStartMinute.toString()
        val marginTop = (completedGigCard.gigStartHour * itemHeight + ((completedGigCard.gigStartMinute/60.0F)*itemHeight).toInt()).px

        day_times.addView(completedGigCard)

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(completedGigCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart - 16.px)
        constraintSet.connect(completedGigCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START, marginCardEnd)
        constraintSet.connect(completedGigCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)

        completedGigCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on completed card", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setListeners() {
        top_bar.available_toggle.setOnClickListener {
            toggleAvailability()

        }

    }

    private fun toggleAvailability() {
        top_bar.isAvailable = !top_bar.isAvailable

        // disable hours
        var hourView: HourRow
        for (idx in 1..24) {
            hourView = day_times.findViewWithTag("hour_$idx")
            hourView.isDisabled = !hourView.isDisabled
        }
        if (!top_bar.isAvailable) {
            if (upcomingGigs.size > 0) {
                showGigsTodayWarning()
            }
        }
    }

    private fun removeUnavailableCards() {
        for (tag in unavailableCards) {
            var view = day_times.findViewWithTag<UnavailableCard>(tag)
            day_times.removeView(view)
        }

    }

    private fun removeUpcomingGigsCards() {
        for (gig in upcomingGigs) {
            var tag = "gig_" + gig.startHour + "_" + gig.startMinute
            var view = day_times.findViewWithTag<UpcomingGigCard>(tag)
            day_times.removeView(view)
        }
    }

    private fun showGigsTodayWarning() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.gigs_today_warning_dialog)

        dialog.dialog_content.setText(
            "You have " + upcomingGigs.size.toString() + " Gig(s) active on the day. These gigs will get canceled as well."
        )

        dialog.cancel.setOnClickListener {
            toggleAvailability()
            dialog .dismiss()
        }

        dialog.yes.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on YeS", Toast.LENGTH_SHORT).show()
            showReasonForGigCancel()
            dialog .dismiss()
        }

        dialog.show()
    }

    private fun showReasonForGigCancel() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)

        var selectedText = ""

        dialog.cancel_options.setOnCheckedChangeListener ( RadioGroup.OnCheckedChangeListener { group, checkedId ->
            selectedText = dialog.findViewById<RadioButton>(checkedId).text.toString()
        })

        dialog.submit_button.setOnClickListener {
            Toast.makeText(requireContext(), "selected option " + selectedText, Toast.LENGTH_SHORT).show()
            removeUnavailableCards()
            removeUpcomingGigsCards()
            dialog .dismiss()
        }

        dialog.cancel_button.setOnClickListener {
            toggleAvailability()
            dialog .dismiss()
        }

        dialog.show()
    }
}