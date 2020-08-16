package com.gigforce.app.modules.preferences.earnings

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import kotlinx.android.synthetic.main.earning_fragment.*

class EarningFragment : BaseFragment() {

    companion object {
        fun newInstance() = EarningFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private var earningDataModel: EarningDataModel = EarningDataModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.earning_fragment, inflater, container)
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
                    (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekBarDependentCanvas2.setX((seekBar.getX() + value + seekBar.getThumbOffset() / 2) - 35)
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
                    (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekBarDependentCanvas3.setX((seekBar.getX() + value + seekBar.getThumbOffset() / 2) - 35)
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
                    (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekBarDependentCanvas4.setX((seekBar.getX() + value + seekBar.getThumbOffset() / 2) - 35)
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
                monthlyExpectationSB.setProgress(0)
                monthlyExpectationSB.setProgress(viewModel.getPreferenceDataModel().earning.monthlyExpectation)
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
            viewModel.getPreferenceDataModel().earning.preferredNoOfDays + " "+getString(R.string.days_camel_case)
        perDayGoalSB.setProgress(0)
        perDayGoalSB.setProgress(viewModel.getPreferenceDataModel().earning.perDayGoal / 500)
        permonthGoalSB.setProgress(0)
        permonthGoalSB.setProgress(viewModel.getPreferenceDataModel().earning.perMonthGoal / 500)
        checkbox_monthly_constract.isChecked =
            viewModel.getPreferenceDataModel().earning.monthlyContractRequired
        monthlyExpectationSB.setProgress(0)
        monthlyExpectationSB.setProgress(viewModel.getPreferenceDataModel().earning.monthlyExpectation / 500)

        dailyGoalsTV.text = getString(R.string.zero_to_rs)  + viewModel.getPreferenceDataModel().earning.perDayGoal
        monthlyGoalsTV.text =getString(R.string.zero_to_rs) + viewModel.getPreferenceDataModel().earning.perMonthGoal
    }

    override fun onBackPressed(): Boolean {
        if (isDataChanged()) {
            confirmationForSavingData()
            return true
        } else return false
    }

    private fun confirmationForSavingData() {
        showConfirmationDialogType2(getString(R.string.are_you_sure_change_preferences),
            object : ConfirmationDialogOnClickListener {
                override fun clickedOnYes(dialog: Dialog?) {
                    saveDataToDB()
                    saveDataSubmitDialog()
                    dialog?.dismiss()
                }

                override fun clickedOnNo(dialog: Dialog?) {
                    popFragmentFromStack(R.id.earningFragment)
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
            popFragmentFromStack(R.id.earningFragment)
        })
        customialog?.show()
    }

    private fun showDialogPreferredNoOfDays() {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.custom_alert_2)
        var radioGroup = customialog?.findViewById(R.id.radio_group) as RadioGroup;
        setPreferenceNoOfDays(radioGroup)
        radioGroup?.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radioButton = radioGroup.findViewById<RadioButton>(checkedId)
                selected_pre_no_of_days.text = radioButton.text.toString()
                customialog?.dismiss()
            })
        radioGroup?.setOnClickListener {
            showToast(getString(R.string.working))
        }
        customialog?.show()
    }

    private fun setPreferenceNoOfDays(radioGroup: RadioGroup) {
        when (selected_pre_no_of_days.text) {
            getString(R.string.zero_to_four_days) -> radioGroup.findViewById<RadioButton>(R.id.zeroToFour).isChecked = true
            getString(R.string.four_to_eight) -> radioGroup.findViewById<RadioButton>(R.id.fourToEight).isChecked = true
            getString(R.string.eight_to_fifteen) -> radioGroup.findViewById<RadioButton>(R.id.eightToFifteen).isChecked =
                true
            getString(R.string.fifteen_to_thirty) -> radioGroup.findViewById<RadioButton>(R.id.fifteenToThirty).isChecked =
                true
        }

    }

}