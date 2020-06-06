package com.gigforce.app.modules.preferences.earnings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import kotlinx.android.synthetic.main.earning_fragment.*

class EarningFragment : BaseFragment() {

    companion object {
        fun newInstance() = EarningFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.earning_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
    }

    private fun listener() {
        checkbox_monthly_constract.setOnCheckedChangeListener{buttonview,ischecked->
            if(ischecked)
            monthly_expectation_constraintlayout.visibility = View.GONE
            else
                monthly_expectation_constraintlayout.visibility = View.VISIBLE
        }
        preferredNoOfDays.setOnClickListener{
            showDialogPreferredNoOfDays()
        }
    }

    private fun initializeViews() {
        seekBarWithHint.setOtherView(seekBarDependentCanvas2)
    }

    override fun onBackPressed(): Boolean {
        confirmationForSavingData()
        return true
    }

    private fun confirmationForSavingData() {
        showConfirmationDialog(" Are sure you want to change prefrences ?",
            object :ConfirmationDialogOnClickListener{
                override fun clickedOnYes(dialog: Dialog?) {
                    saveEarningDataToDB()
                }

                override fun clickedOnNo(dialog: Dialog?) {
                   dialog?.dismiss()
                }

            })
    }

    private fun saveEarningDataToDB() {
        var customialog:Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.custom_alert_1)
        val yesBtn = customialog?.findViewById(R.id.okay) as TextView
        yesBtn.setOnClickListener (View.OnClickListener {
            popFragmentFromStack(R.id.earningFragment)
            customialog.dismiss()
        })
        customialog?.show()
    }

    private fun showDialogPreferredNoOfDays() {
        var customialog:Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.custom_alert_2)
        customialog?.show()
    }

}