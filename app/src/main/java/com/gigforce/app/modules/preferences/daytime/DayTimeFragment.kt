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
        preferredDistanceSwitch.setChecked(preferencesDataModel.isweekdaysenabled)
        arroundCurrentAddSwitch.setChecked(preferencesDataModel.isweekendenabled)
        reflectForWeekdays()
        reflectForWeekends()
    }

    private fun reflectForWeekends() {
        textView57.text = getYesNoforWeekend()
        if(preferencesDataModel.isweekendenabled)
            setTextViewColor(textView56,R.color.black)
        else
            setTextViewColor(textView56,R.color.gray_color)
    }

    private fun reflectForWeekdays() {
        setWeekDaysText()
        if(preferencesDataModel.isweekdaysenabled)
            setTextViewColor(textView50,R.color.black)
        else
            setTextViewColor(textView50,R.color.gray_color)
    }


    private fun setWeekDaysText() {
        var subTitle = ""
        var daysStr  = "day"
        if(preferencesDataModel.selecteddays.size==0){
            subTitle = "None"
        }else if(preferencesDataModel.selecteddays.size>1){
            var totalDays = preferencesDataModel.selecteddays.size
            if(totalDays==6)
                totalDays-=1
            subTitle = totalDays.toString()+" days"
        }
        else if(preferencesDataModel.selecteddays.size==1){
            subTitle = preferencesDataModel.selecteddays.size.toString()+" day"
        }
        textView51.text = subTitle
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
        textView55.setOnClickListener(View.OnClickListener { navigate(R.id.weekEndFragment) })
        imageView10.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        // weekday listener
        preferredDistanceSwitch.setOnClickListener{ view->
            var isChecked = (view as Switch).isChecked
            if(isChecked && preferencesDataModel.selecteddays.size == 0){
                navigate(R.id.weekDayFragment)
                preferredDistanceSwitch.setChecked(false)
            }
            else
            viewModel.setIsWeekdays(isChecked)
        }
        //weekend listener
        arroundCurrentAddSwitch.setOnClickListener{ view->
            var isChecked = (view as Switch).isChecked
            viewModel.setIsWeekend(isChecked)
        }
//        switch2.setOnCheckedChangeListener{ buttonView, isChecked ->
//            viewModel.setIsWeekend(isChecked)
//        }
    }

}