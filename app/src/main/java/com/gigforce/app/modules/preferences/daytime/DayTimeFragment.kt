package com.gigforce.app.modules.preferences.daytime

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.date_time_fragment.*
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel


class DayTimeFragment : BaseFragment() {

    companion object {
        fun newInstance() = DayTimeFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferencesDataModel: PreferencesDataModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.date_time_fragment, inflater,container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(this, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })
    }

    private fun initializeViews() {
        preferencesDataModel = viewModel.getPreferenceDataModel()
        switch1.setChecked(preferencesDataModel.isweekdaysenabled)
        switch2.setChecked(preferencesDataModel.isweekendenabled)
        textView57.text = getYesNoforWeekend()
    }

    private fun getYesNoforWeekend(): String {
        if(preferencesDataModel.isweekendenabled){
            return "Yes"
        }
        else{
            return "No"
        }
    }

    private fun listener() {
        textView49.setOnClickListener(View.OnClickListener { navigate(R.id.weekDayFragment) })
        textView55.setOnClickListener(View.OnClickListener {  })
        // weekday listener
        switch1.setOnClickListener{view->
            var isChecked = (view as Switch).isChecked
            viewModel.setIsWeekdays(isChecked)
        }
        //weekend listener
        switch2.setOnClickListener{view->
            var isChecked = (view as Switch).isChecked
            viewModel.setIsWeekend(isChecked)
        }
        switch2.setOnCheckedChangeListener{ buttonView, isChecked ->
            viewModel.setIsWeekend(isChecked)
        }
    }

}