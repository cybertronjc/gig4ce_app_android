package com.gigforce.user_preferences.daytime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.core.navigation.INavigation
import com.gigforce.user_preferences.R
import com.gigforce.user_preferences.SharedPreferenceViewModel
import com.gigforce.user_preferences.prefdatamodel.PreferencesDataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.date_time_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class DayTimeFragment : Fragment() {

    companion object {
        fun newInstance() = DayTimeFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferencesDataModel: PreferencesDataModel

    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.date_time_fragment, container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            if (preferenceData != null) {
                viewModel.setPreferenceDataModel(preferenceData)
                initializeViews()
            }
        })
        viewModel.configLiveDataModel.observe(
            viewLifecycleOwner,
            Observer { configDataModel1 ->
                viewModel.setConfiguration(configDataModel1)
                initializeViews()
            })
        viewModel.getConfiguration()
    }

    private fun initializeViews() {
        preferencesDataModel = viewModel.getPreferenceDataModel()
        workFromHomeSwitch.setChecked(preferencesDataModel.isweekdaysenabled)
        arroundCurrentAddSwitch.setChecked(preferencesDataModel.isweekendenabled)
        reflectForWeekdays()
        reflectForWeekends()
    }

    private fun reflectForWeekends() {
        textView57.text = getYesNoforWeekend()

        context?.let {
            var colorCode = if (preferencesDataModel.isweekendenabled)  R.color.black else R.color.gray_color
            textView56.setTextColor(ContextCompat.getColor(it, colorCode))
        }
    }

    private fun reflectForWeekdays() {
        setWeekDaysText()
        context?.let {
            var colorCode = if(preferencesDataModel.isweekdaysenabled) R.color.black else R.color.gray_color
            textView50.setTextColor(ContextCompat.getColor(it, colorCode))
        }
    }


    private fun setWeekDaysText() {
        var subTitle = ""
        var daysStr = getString(R.string.day)
        if (preferencesDataModel.selecteddays.size == 0) {
            subTitle = getString(R.string.none)
        } else if (preferencesDataModel.selecteddays.size > 1) {
            var totalDays = preferencesDataModel.selecteddays.size
            if (totalDays == 6)
                totalDays -= 1
            subTitle = totalDays.toString()
        } else if (preferencesDataModel.selecteddays.size == 1) {
            subTitle =
                preferencesDataModel.selecteddays.size.toString() + " " + getString(R.string.days)
        }
        textView51.text = subTitle
    }


    private fun getYesNoforWeekend(): String {
        if (preferencesDataModel.isweekendenabled) {
            return getString(R.string.yes)
        } else {
            return getString(R.string.no)
        }
    }

    private fun listener() {
        textView49.setOnClickListener { navigation.navigateTo("preferences/weekDayFragment") }
        textView55.setOnClickListener{ navigation.navigateTo("preferences/weekEndFragment") }
        back_arrow_iv.setOnClickListener{ activity?.onBackPressed() }
        // weekday listener
        workFromHomeSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if (isChecked && preferencesDataModel.selecteddays.size == 0) {
                navigation.navigateTo("preferences/weekDayFragment")
                workFromHomeSwitch.setChecked(false)
            } else
                viewModel.setIsWeekdays(isChecked)
        }
        //weekend listener
        arroundCurrentAddSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked

            if(isChecked && preferencesDataModel.selectedweekends.size == 0){
                navigation.navigateTo("preferences/weekEndFragment")
                arroundCurrentAddSwitch.setChecked(false)
            }
            else
            viewModel.setIsWeekend(isChecked)
        }
    }

}