package com.gigforce.profile.onboarding.fragments.jobpreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import kotlinx.android.synthetic.main.job_preference_fragment.*

class JobPreferenceFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(), OnboardingFragmentNew.FragmentInteractionListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = JobPreferenceFragment(formCompletionListener)
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
            formCompletionListener.formcompleted(true)
        })
        imageTextCardcl_.setOnClickListener(View.OnClickListener {
            fullTimeJob = false
            formCompletionListener.formcompleted(true)
        })

        workingDaysIds.forEach { obj ->
            obj.setOnCheckedChangeListener { _, isChecked ->


            }
        }

    }

//    class CheckedChangedListener : OnCheck

    fun getWorkingDays():ArrayList<String>{
        var workingdays = ArrayList<String>()
        workingDaysIds.forEach { day-> if(day.isChecked) workingdays.add(day.tag.toString())}
        return workingdays
    }

    fun getTimeSlots():ArrayList<String>{
        var workingTimeSlots = ArrayList<String>()
        timeSlotsIds.forEach { slot-> if(slot.isChecked) workingTimeSlots.add(slot.tag.toString())}
        return workingTimeSlots
    }

    fun validateForm(){
        if(fullTimeJob || (getWorkingDays().size>0 && getTimeSlots().size>0)){
            formCompletionListener.formcompleted(true)
        }
        else{
            formCompletionListener.formcompleted(false)

        }
    }
    override fun actionFound(): Boolean {
        if(!fullTimeJob && getWorkingDays().size == 0){
            job_preferences.gone()
            work_days_cl.visible()
            timing_cl.gone()
            return true
        }else if(!fullTimeJob && getWorkingDays().size>0 && getTimeSlots().size == 0) {
            job_preferences.gone()
            work_days_cl.gone()
            timing_cl.visible()
            return true
        }
        return false

    }
}