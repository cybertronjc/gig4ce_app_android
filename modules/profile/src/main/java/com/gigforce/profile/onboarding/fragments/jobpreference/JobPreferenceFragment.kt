package com.gigforce.profile.onboarding.fragments.jobpreference

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import kotlinx.android.synthetic.main.job_preference_fragment.*

class JobPreferenceFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) :
    Fragment(), OnboardingFragmentNew.FragmentInteractionListener,
    OnboardingFragmentNew.FragmentSetLastStateListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) =
            JobPreferenceFragment(formCompletionListener)
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
    private fun listeners() {
        imageTextCardcl.setOnClickListener(View.OnClickListener {
            fullTimeJob = true
            resetAll()
            setSelected(icon_iv, full_time, imageTextCardcl)
            formCompletionListener.enableDisableNextButton(true)

        })
        imageTextCardcl_.setOnClickListener(View.OnClickListener {
            fullTimeJob = false
            resetAll()
            setSelected(icon_iv1, part_time, imageTextCardcl_)
            formCompletionListener.enableDisableNextButton(true)
        })

        workingDaysIds.forEach { obj ->
            obj.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    formCompletionListener.enableDisableNextButton(true)
                    }
                else if (anyCheckboxChecked(workingDaysIds)) {
                    formCompletionListener.enableDisableNextButton(true)
                } else {
                    formCompletionListener.enableDisableNextButton(false)
                }
            }
        }

        timeSlotsIds.forEach { obj ->
            obj.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    formCompletionListener.enableDisableNextButton(true)
                    }
                else if (anyCheckboxChecked(timeSlotsIds)) {
                    formCompletionListener.enableDisableNextButton(true)
                } else {
                    formCompletionListener.enableDisableNextButton(false)
                }
            }
        }
    }

    fun resetAll() {
        resetSelected(icon_iv, full_time, imageTextCardcl)
        resetSelected(icon_iv1, part_time, imageTextCardcl_)
    }

    fun anyCheckboxChecked(ids: ArrayList<CheckBox>): Boolean {
        ids.forEach { checkbox -> if (checkbox.isChecked) return true}

        return false
    }

//    class CheckedChangedListener : OnCheck

    fun getWorkingDays(): ArrayList<String> {
        var workingdays = ArrayList<String>()
        workingDaysIds.forEach { day -> if (day.isChecked) {
            workingdays.add(day.tag.toString())
            day.background = resources.getDrawable(R.drawable.rect_gray_border)
        }
        else{
            day.background = resources.getDrawable(R.drawable.option_selection_border)
        }}
        return workingdays
    }

    fun getTimeSlots(): ArrayList<String> {
        var workingTimeSlots = ArrayList<String>()
        timeSlotsIds.forEach { slot -> if (slot.isChecked){
            workingTimeSlots.add(slot.tag.toString())
            slot.background = resources.getDrawable(R.drawable.option_selection_border)
        }
        else {
            slot.background = resources.getDrawable(R.drawable.rect_gray_border)
        }}
        return workingTimeSlots
    }

    var currentStep = 0
    override fun nextButtonActionFound(): Boolean {
        when (currentStep) {
            0 -> if (!fullTimeJob) {
                job_preferences.gone()
                work_days_cl.visible()
                timing_cl.gone()
                currentStep = 1
                return true
            }
            1 -> if (getWorkingDays().size > 0) {
                job_preferences.gone()
                work_days_cl.gone()
                timing_cl.visible()
                currentStep = 2
                return true
            }
            else -> return false
        }
        return false

    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener.enableDisableNextButton(true)
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
}