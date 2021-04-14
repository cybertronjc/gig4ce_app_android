package com.gigforce.profile.onboarding.fragments.jobpreference

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import kotlinx.android.synthetic.main.job_preference_fragment.*

class JobPreferenceFragment : Fragment() {

    companion object {
        fun newInstance() = JobPreferenceFragment()
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

    private fun listeners() {
        imageTextCardcl.setOnClickListener(View.OnClickListener {
            job_preferences.gone()
            work_days_cl.visible()
            timing_cl.gone()
        })
        imageTextCardcl_.setOnClickListener(View.OnClickListener {
            job_preferences.gone()
            work_days_cl.visible()
            timing_cl.gone()

        })

        days_rg.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            job_preferences.gone()
            work_days_cl.gone()
            timing_cl.visible()
        })
    }

}