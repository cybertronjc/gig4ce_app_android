package com.gigforce.profile.onboarding.fragments.jobpreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.job_preference_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class JobPreferenceFragment() :
        Fragment(), OnboardingFragmentNew.FragmentInteractionListener,
        OnboardingFragmentNew.FragmentSetLastStateListener, OnboardingFragmentNew.SetInterfaceListener {

    @Inject
    lateinit var eventTracker: IEventTracker

    companion object {
        fun newInstance() =
                JobPreferenceFragment()
    }

    private lateinit var viewModel: JobPreferenceViewModel
    var timeSlotsIds = ArrayList<CheckBox>()
    var workingDaysIds = ArrayList<CheckBox>()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.job_preference_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JobPreferenceViewModel::class.java)
        initVar()
        listeners()
    }

    fun getAllWorkingDays(): ArrayList<String> {
        var workingDaysList = ArrayList<String>()
        workingDaysList.add("Monday")
        workingDaysList.add("Tuesday")
        workingDaysList.add("Wednesday")
        workingDaysList.add("Thursday")
        workingDaysList.add("Friday")
        workingDaysList.add("Saturday")
        workingDaysList.add("Sunday")
        return workingDaysList
    }

    fun getAllTimeSlots(): ArrayList<String> {
        var timeSlots = ArrayList<String>()
        timeSlots.add("5 am - 8 am")
        timeSlots.add("8 am - 12 pm")
        timeSlots.add("12 pm - 4 pm")
        timeSlots.add("4 pm - 8 pm")
        timeSlots.add("8 pm - 12 am")
        return timeSlots
    }

    private fun initVar() {
        workingDaysArr()
        timeSlotsArr()

    }

    private fun workingDaysArr() {
        workingDaysIds.add(monday)
        workingDaysIds.add(tuesday)
        workingDaysIds.add(wednesday)
        workingDaysIds.add(thursday)
        workingDaysIds.add(friday)
        workingDaysIds.add(saturday)
        workingDaysIds.add(sunday)
    }

    private fun timeSlotsArr() {
        timeSlotsIds.add(early_morning)
        timeSlotsIds.add(morning)
        timeSlotsIds.add(day_time)
        timeSlotsIds.add(evening)
        timeSlotsIds.add(night)
    }

    var fullTimeJob = false
    var clickedOnPreferenceOptions = false
    private fun listeners() {
        imageTextCardcl.setOnClickListener(View.OnClickListener {
            fullTimeJob = true
            resetAll()
            setSelected(icon_iv, full_time, imageTextCardcl)
            formCompletionListener?.enableDisableNextButton(true)
            clickedOnPreferenceOptions = true

        })
        imageTextCardcl_.setOnClickListener(View.OnClickListener {
            fullTimeJob = false
            resetAll()
            setSelected(icon_iv1, part_time, imageTextCardcl_)
            formCompletionListener?.enableDisableNextButton(true)
            clickedOnPreferenceOptions = true

        })

        workingDaysIds.forEach { obj ->
            obj.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else if (anyCheckboxChecked(workingDaysIds)) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else {
                    formCompletionListener?.enableDisableNextButton(false)
                }
            }
        }

        timeSlotsIds.forEach { obj ->
            obj.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else if (anyCheckboxChecked(timeSlotsIds)) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else {
                    formCompletionListener?.enableDisableNextButton(false)
                }
            }
        }
    }

    fun resetAll() {
        resetSelected(icon_iv, full_time, imageTextCardcl)
        resetSelected(icon_iv1, part_time, imageTextCardcl_)
    }

    fun anyCheckboxChecked(ids: ArrayList<CheckBox>): Boolean {
        ids.forEach { checkbox -> if (checkbox.isChecked) return true }

        return false
    }

//    class CheckedChangedListener : OnCheck

    fun getWorkingDays(): ArrayList<String> {
        var workingdays = ArrayList<String>()
        workingDaysIds.forEach { day ->
            if (day.isChecked) {
                workingdays.add(day.tag.toString())
            }
        }
        return workingdays
    }

    fun getTimeSlots(): List<String> {
        var workingTimeSlots = ArrayList<String>()
        timeSlotsIds.forEach { slot ->
            if (slot.isChecked) {
                workingTimeSlots.add(slot.tag.toString())
            }
        }

        return workingTimeSlots.filter {
            it.isNotBlank()
        }
    }

    var currentStep = 0
    override fun nextButtonActionFound(): Boolean {
        when (currentStep) {
            0 -> if (!fullTimeJob) {
                job_preferences.gone()
                work_days_cl.visible()
                timing_cl.gone()
                currentStep = 1

                val fullTimePartime = if (fullTimeJob) getString(R.string.full_time_profile) else getString(
                                    R.string.part_time_profile)
                eventTracker.pushEvent(args = TrackingEventArgs(
                        OnboardingEvents.EVENT_USER_EXPLOYMENT_PREFERENCE_SELECTED,
                        props = mapOf(
                                "preference_selected" to fullTimePartime
                        )
                )
                )


                return true
            }
            1 -> if (getWorkingDays().size > 0) {
                job_preferences.gone()
                work_days_cl.gone()
                timing_cl.visible()

                eventTracker.pushEvent(args = TrackingEventArgs(
                        OnboardingEvents.EVENT_USER_WORK_DAYS_PREFERRED_SELECTED,
                        props = mapOf(
                                "time_slots" to getTimeSlots()
                        )
                )
                )

                currentStep = 2
                return true
            }
            else -> {
                partTimeJobTracker()
                return false
            }
        }

        fullTimeJobTracker()
        return false

    }

    private fun partTimeJobTracker() {
        var map = mapOf("FullTimeJob" to false, "Days" to getWorkingDays(), "TimeSlots" to getTimeSlots())
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_CURRENT_JOB_STATUS_SELECTED, map))
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("FullTimeJob", false))
        eventTracker.setProfileProperty(ProfilePropArgs("Days", getWorkingDays()))
        eventTracker.setProfileProperty(ProfilePropArgs("TimeSlots", getTimeSlots()))
    }

    private fun fullTimeJobTracker() {
        var map = mapOf("FullTimeJob" to true)
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_CURRENT_JOB_STATUS_SELECTED, map))
        eventTracker.removeUserProperty("Days")
        eventTracker.removeUserProperty("TimeSlots")
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("FullTimeJob", true))
    }


    override fun activeNextButton() {
        when (currentStep) {
            0 -> if (clickedOnPreferenceOptions) formCompletionListener?.enableDisableNextButton(true) else formCompletionListener?.enableDisableNextButton(false)
            1 -> if (getWorkingDays().size > 0) formCompletionListener?.enableDisableNextButton(true) else formCompletionListener?.enableDisableNextButton(false)
            else -> if (getTimeSlots().size > 0) formCompletionListener?.enableDisableNextButton(true) else formCompletionListener?.enableDisableNextButton(false)
        }
    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        if (currentStep == 2) {
            job_preferences.gone()
            work_days_cl.visible()
            timing_cl.gone()
            currentStep = 1

            return true
        } else if (currentStep == 1) {
            job_preferences.visible()
            work_days_cl.gone()
            timing_cl.gone()
            currentStep = 0
            return true
        }
        return false
    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                            it,
                            R.drawable.option_default_border
                    )
            )
        }
    }

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.selected_image_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.selected_text_color))
            view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                            it,
                            R.drawable.option_selection_border
                    )
            )
        }
    }
    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener?:onFragmentFormCompletionListener
    }

}