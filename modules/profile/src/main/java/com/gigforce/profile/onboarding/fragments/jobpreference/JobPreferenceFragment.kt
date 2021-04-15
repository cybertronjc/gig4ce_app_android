package com.gigforce.profile.onboarding.fragments.jobpreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.core.view.isVisible
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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.job_preference_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JobPreferenceViewModel::class.java)
        listeners()
    }

    var fullTimeJob = false
    private fun listeners() {
        imageTextCardcl.setOnClickListener(View.OnClickListener {
//            job_preferences.gone()
//            work_days_cl.visible()
//            timing_cl.gone()
            fullTimeJob = true
            validateForm()
        })
        imageTextCardcl_.setOnClickListener(View.OnClickListener {
            job_preferences.gone()
            work_days_cl.visible()
            timing_cl.gone()
            fullTimeJob = false
            validateForm()
        })


    }

    override fun actionFound(): Boolean {
        if (work_days_cl.isVisible) {
            job_preferences.gone()
            work_days_cl.gone()
            timing_cl.visible()
            return true
        } else {
            return false
        }
    }

    fun getWorkingDays():ArrayList<String>{
        var ids = ArrayList<CheckBox>()
        ids.add(monday)
        ids.add(tuesday)
        ids.add(wednesday)
        ids.add(thursday)
        ids.add(friday)
        ids.add(saturday)
        ids.add(sunday)
        var workingdays = ArrayList<String>()
        ids.forEach { day-> if(day.isChecked) workingdays.add(day.tag.toString())}
        return workingdays
    }

    fun getTimeSlots():ArrayList<String>{
        var ids = ArrayList<CheckBox>()
        ids.add(early_morning)
        ids.add(morning)
        ids.add(day_time)
        ids.add(evening)
        ids.add(night)
        var workingTimeSlots = ArrayList<String>()
        ids.forEach { slot-> if(slot.isChecked) workingTimeSlots.add(slot.tag.toString())}
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

}