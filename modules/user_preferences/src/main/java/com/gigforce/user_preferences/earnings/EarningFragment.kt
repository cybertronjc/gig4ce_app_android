package com.gigforce.user_preferences.earnings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.preferences.EarningDataModel
import com.gigforce.core.navigation.INavigation
import com.gigforce.user_preferences.R
import com.gigforce.user_preferences.SharedPreferenceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.earning_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class EarningFragment : Fragment(), IOnBackPressedOverride {

    companion object {
        fun newInstance() = EarningFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private var earningDataModel: EarningDataModel = EarningDataModel()
    @Inject lateinit var navigation : INavigation
    @Inject lateinit var appDialogInterface : AppDialogsInterface
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.earning_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        listener()
        initializeViews()
        observer()
    }

    private fun observer() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            if (preferenceData != null) {
                viewModel.setPreferenceDataModel(preferenceData)
                initializeViews()
            }
        })
    }

    private fun listener() {
        back_arrow_iv.setOnClickListener {
            activity?.onBackPressed()
        }
        perDayGoalSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                    (progress * (seekBar.width - 2 * seekBar.thumbOffset)) / seekBar.max
                seekBarDependentCanvas2.x = (seekBar.x + value + seekBar.thumbOffset / 2) - 35
                var progress1 = progress * 500
                seekBarDependentCanvas2.text = getString(R.string.rs) + " " + progress1.toString()
                dailyGoalsTV.text = getString(R.string.zero_to_rs) + progress1
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        permonthGoalSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                    (progress * (seekBar.width - 2 * seekBar.thumbOffset)) / seekBar.max
                seekBarDependentCanvas3.x = (seekBar.x + value + seekBar.thumbOffset / 2) - 35
                var progress1 = progress * 500
                monthlyGoalsTV.text = getString(R.string.zero_to_rs) + progress1
                seekBarDependentCanvas3.text = getString(R.string.rs) + " " + progress1.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        monthlyExpectationSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                    (progress * (seekBar.width - 2 * seekBar.thumbOffset)) / seekBar.max
                seekBarDependentCanvas4.x = (seekBar.x + value + seekBar.thumbOffset / 2) - 35
                var progress1 = progress * 500
                monthlyContractTV.text = getString(R.string.zero_to_rs) + progress1

                seekBarDependentCanvas4.text = getString(R.string.rs) + progress1.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        checkbox_monthly_constract.setOnCheckedChangeListener { buttonview, ischecked ->
            if (ischecked) {
                monthly_expectation_constraintlayout.visibility = View.VISIBLE
                monthlyExpectationSB.progress = 0
                monthlyExpectationSB.progress = viewModel.getPreferenceDataModel().earning.monthlyExpectation
            } else
                monthly_expectation_constraintlayout.visibility = View.GONE
        }
        preferredNoOfDays.setOnClickListener {
            showDialogPreferredNoOfDays()
        }

    }

    private fun initializeViews() {
        if (viewModel.getPreferenceDataModel().earning.preferredNoOfDays.isEmpty())
            viewModel.getPreferenceDataModel().earning.preferredNoOfDays = "0"
        selected_pre_no_of_days.text =
            viewModel.getPreferenceDataModel().earning.preferredNoOfDays + " " + getString(R.string.days_camel_case)
        perDayGoalSB.progress = 0
        perDayGoalSB.progress = viewModel.getPreferenceDataModel().earning.perDayGoal / 500
        permonthGoalSB.progress = 0
        permonthGoalSB.progress = viewModel.getPreferenceDataModel().earning.perMonthGoal / 500
        checkbox_monthly_constract.isChecked =
            viewModel.getPreferenceDataModel().earning.monthlyContractRequired
        setMonthlyContractVisibility()
        monthlyExpectationSB.progress = 0
        monthlyExpectationSB.progress = viewModel.getPreferenceDataModel().earning.monthlyExpectation / 500

        dailyGoalsTV.text =
            getString(R.string.zero_to_rs) + viewModel.getPreferenceDataModel().earning.perDayGoal
        monthlyGoalsTV.text =
            getString(R.string.zero_to_rs) + viewModel.getPreferenceDataModel().earning.perMonthGoal
    }

    private fun setMonthlyContractVisibility() {
        if (viewModel.getPreferenceDataModel().earning.monthlyContractRequired)
            monthly_expectation_constraintlayout.visibility = View.VISIBLE
        else
            monthly_expectation_constraintlayout.visibility = View.GONE

    }

    override fun onBackPressed(): Boolean {
        if (isDataChanged()) {
            confirmationForSavingData()
            return true
        } else return false
    }

    private fun confirmationForSavingData() {
        appDialogInterface.showConfirmationDialogType2(getString(R.string.are_you_sure_change_preferences),
            object : ConfirmationDialogOnClickListener {
                override fun clickedOnYes(dialog: Dialog?) {
                    saveDataToDB()
                    saveDataSubmitDialog()
                    dialog?.dismiss()
                }

                override fun clickedOnNo(dialog: Dialog?) {
                    navigation.popBackStack("preferences/earningFragment")
//                    popFragmentFromStack(R.id.earningFragment)
                    dialog?.dismiss()
                }

            })
    }



    fun isDataChanged(): Boolean {
        if (viewModel.getPreferenceDataModel().earning.preferredNoOfDays != selected_pre_no_of_days.text.toString()
                .split(
                    " "
                )[0]
        )
            return true
        if (viewModel.getPreferenceDataModel().earning.perDayGoal / 500 != perDayGoalSB.progress) {
            return true
        }
        if (viewModel.getPreferenceDataModel().earning.perMonthGoal / 500 != permonthGoalSB.progress) {
            return true
        }
        if (viewModel.getPreferenceDataModel().earning.monthlyContractRequired != checkbox_monthly_constract.isChecked) {
            return true
        }
        if (viewModel.getPreferenceDataModel().earning.monthlyExpectation / 500 != monthlyExpectationSB.progress) {
            return true
        }
        return false
    }

    private fun saveDataToDB() {
        earningDataModel.preferredNoOfDays = selected_pre_no_of_days.text.toString().split(" ")[0]
        earningDataModel.perDayGoal = perDayGoalSB.progress * 500
        earningDataModel.perMonthGoal = permonthGoalSB.progress * 500
        earningDataModel.monthlyContractRequired = checkbox_monthly_constract.isChecked
        earningDataModel.monthlyExpectation = monthlyExpectationSB.progress * 500
        viewModel.saveEarningData(earningDataModel)

    }

    private fun saveDataSubmitDialog() {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.custom_alert_1)
        val okay = customialog?.findViewById(R.id.okay) as TextView
        okay.setOnClickListener(View.OnClickListener {
            customialog.dismiss()
//            activity?.onBackPressed()
            navigation.popBackStack("preferences/earningFragment")
//            popFragmentFromStack(R.id.earningFragment)
        })
        customialog.show()
    }

    private fun showDialogPreferredNoOfDays() {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.custom_alert_2)
        var radioGroup = customialog?.findViewById(R.id.radio_group) as RadioGroup
        setPreferenceNoOfDays(radioGroup)
        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radioButton = radioGroup.findViewById<RadioButton>(checkedId)
                selected_pre_no_of_days.text = radioButton.text.toString()
                customialog.dismiss()
            })
        radioGroup.setOnClickListener {
            showToast(getString(R.string.working))
        }
        customialog.show()
    }

    private fun setPreferenceNoOfDays(radioGroup: RadioGroup) {
        when (selected_pre_no_of_days.text) {
            getString(R.string.zero_to_four_days) -> radioGroup.findViewById<RadioButton>(R.id.zeroToFour).isChecked =
                true
            getString(R.string.four_to_eight) -> radioGroup.findViewById<RadioButton>(R.id.fourToEight).isChecked =
                true
            getString(R.string.eight_to_fifteen) -> radioGroup.findViewById<RadioButton>(R.id.eightToFifteen).isChecked =
                true
            getString(R.string.fifteen_to_thirty) -> radioGroup.findViewById<RadioButton>(R.id.fifteenToThirty).isChecked =
                true
        }

    }

}