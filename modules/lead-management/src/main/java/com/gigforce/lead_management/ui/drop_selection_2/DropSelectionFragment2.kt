package com.gigforce.lead_management.ui.drop_selection_2

import android.app.DatePickerDialog
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding
import com.gigforce.lead_management.databinding.DropSelection2BottomSheetMainBinding
import com.gigforce.lead_management.databinding.DropSelectionFragment2FragmentBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.drop_selection.DropSelectionViewModel
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DropSelectionFragment2 : BaseBottomSheetDialogFragment<DropSelectionFragment2FragmentBinding>(
    fragmentName = "DropSelectionFragment2",
    layoutId = R.layout.drop_selection_fragment2_fragment
) {

    companion object {
        const val TAG = "DropSelectionFragment2"
        const val INTENT_SELECTIONS_TO_DROP = "selections_to_drop"
        const val INTENT_BANK_DETAILS_VERIFIED = "bank_verified"

        fun launch(
            selectionIdsToDrop: ArrayList<String>,
            isBankDetailVerified: Boolean,
            childFragmentManager : FragmentManager
        ){
            DropSelectionFragment2().apply {
                arguments = bundleOf(
                    INTENT_SELECTIONS_TO_DROP to selectionIdsToDrop,
                    INTENT_BANK_DETAILS_VERIFIED to isBankDetailVerified
                )
            }.show(childFragmentManager,TAG)
        }
    }


    private lateinit var selectionIdsToDrop: ArrayList<String>
    private var isBankDetailsVerified: Boolean = false
    private val viewModel: DropSelectionFragment2ViewModel by viewModels()
    private val sharedLeadMgmtViewModel: LeadManagementSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            selectionIdsToDrop = it.getStringArrayList(DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP) ?: return@let
            isBankDetailsVerified = it.getBoolean(DropSelectionFragment2.INTENT_BANK_DETAILS_VERIFIED) ?: return@let
        }

        savedInstanceState?.let {
            selectionIdsToDrop = it.getStringArrayList(DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP) ?: return@let
            isBankDetailsVerified = it.getBoolean(DropSelectionFragment2.INTENT_BANK_DETAILS_VERIFIED) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP,
            selectionIdsToDrop
        )
        outState.putBoolean(
            DropSelectionFragment2.INTENT_BANK_DETAILS_VERIFIED,
            isBankDetailsVerified
            )
    }

    override fun viewCreated(
        viewBinding: DropSelectionFragment2FragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initView()
        initViewModel()
        initListeners()
    }

    private fun initView() = viewBinding.apply {

        lastWorkingLayout.dateText.text = DateHelper.getDateInDDMMMYYYYComma(Date())

    }

    private fun initListeners() = viewBinding.apply{

        joinedNotJoinedLayout.apply {
            //first step
            confirmDrop.setOnClickListener {
                //check if any radio button is selected
                if (radioGroup.checkedRadioButtonId != -1){
                    //get checked radio button id
                    when(radioGroup.checkedRadioButtonId){
                        R.id.joinedRadio -> {
                            if (isBankDetailsVerified){
                                //ask last date of working
                                showLastWorkingLayout()
                            }else{
                                //ask giger to upload bank details
                                showAskBankDetails()
                            }
                        }
                        R.id.notJoinedRadio -> {
                            showMainLayout()
                        }
                    }
                }else{
                    showToast("Select an option")
                }
            }
            cancelDrop.setOnClickListener {
                dismiss()
            }

            radioGroup.setOnCheckedChangeListener { radioGroup, i ->
                confirmDrop.isEnabled = true
            }
        }

        mainLayout.apply {
            //giger has not joined the gig
            dropSelectionButton.setOnClickListener {
                //call drop api
                viewModel.dropSelections(selectionIdsToDrop)

            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
        bankVerifyLayout.apply {
            dropOkay.setOnClickListener {
                dismiss()
            }
        }
        lastWorkingLayout.apply {
            changeDateText.setOnClickListener {
                lastWorkingDatePicker.show()
            }
            confirmButton.setOnClickListener {
                //call drop api
                viewModel.dropSelections(selectionIdsToDrop)
            }
            cancelBtn.setOnClickListener {
                dismiss()
            }
        }


    }

    private fun showAskBankDetails() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.gone()
        this.lastWorkingLayout.root.gone()
        val transition: Transition = Slide(Gravity.END).setDuration(500).addTarget(this.bankVerifyLayout.root)
        TransitionManager.beginDelayedTransition(viewBinding.rootLayout, transition)
        this.bankVerifyLayout.root.visible()
        this.joinedNotJoinedLayout.root.gone()
    }

    private fun showMainLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        val transition: Transition = Slide(Gravity.END).setDuration(500).addTarget(this.mainLayout.root)
        TransitionManager.beginDelayedTransition(viewBinding.rootLayout, transition)
        this.mainLayout.root.visible()
        this.lastWorkingLayout.root.gone()
        this.bankVerifyLayout.root.gone()
        this.joinedNotJoinedLayout.root.gone()
    }

    private fun showLastWorkingLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.gone()
        val transition: Transition = Slide(Gravity.END).setDuration(500).addTarget(this.lastWorkingLayout.root)
        TransitionManager.beginDelayedTransition(viewBinding.rootLayout, transition)
        this.lastWorkingLayout.root.visible()
        this.bankVerifyLayout.root.gone()
        this.joinedNotJoinedLayout.root.gone()
    }

    private val lastWorkingDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.lastWorkingLayout.dateText.text = DateHelper.getDateInDDMMMYYYYComma(newCal.time)

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun initViewModel() = viewModel
        .submitDropSelectionState
        .observe(viewLifecycleOwner, {

            when (it) {
                is Lce.Error -> {
                    viewBinding.mainLayout.dropSelectionButton.hideProgress("Drop Selection")
                    viewBinding.mainLayout.dropSelectionButton.isEnabled = false

                    viewBinding.successLayout.root.gone()
                    viewBinding.mainLayout.root.gone()
                    viewBinding.lastWorkingLayout.root.gone()
                    viewBinding.joinedNotJoinedLayout.root.gone()
                    viewBinding.bankVerifyLayout.root.gone()
                    viewBinding.errorLayout.root.visible()

                    viewBinding.errorLayout.infoMessageTv.text = it.error
                    viewBinding.errorLayout.retryBtn.visible()
                }
                Lce.Loading -> {
                    viewBinding.mainLayout.dropSelectionButton.showProgress {
                        buttonText = "Dropping..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.mainLayout.dropSelectionButton.isEnabled = false


                }
                is Lce.Content -> {
                    viewBinding.successLayout.root.visible()
                    viewBinding.mainLayout.root.gone()
                    viewBinding.errorLayout.root.gone()
                    viewBinding.lastWorkingLayout.root.gone()
                    viewBinding.joinedNotJoinedLayout.root.gone()
                    viewBinding.bankVerifyLayout.root.gone()
                    if (selectionIdsToDrop.size == 1){
                        viewBinding.successLayout.dropedSelectionLabel.text = getString(R.string.one_selection_drop_lead)
                    } else {
                        viewBinding.successLayout.dropedSelectionLabel.text =
                            selectionIdsToDrop.size.toString() + getString(R.string.selection_drop_lead)
                    }
                    //dismiss()
                }
            }
        })

}