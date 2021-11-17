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
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropDetail
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding
import com.gigforce.lead_management.databinding.DropSelection2BottomSheetMainBinding
import com.gigforce.lead_management.databinding.DropSelectionFragment2FragmentBinding
import com.gigforce.lead_management.models.DropScreenIntentModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.drop_selection.DropSelectionViewModel
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
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
        const val INTENT_GIG_START_DATE = "gig_start_date"
        const val INTENT_GIG_END_DATE = "gig_end_date"
        const val INTENT_CURRENT_DATE = "current_date"

        fun launch(
            selectionJoiningsToDrop: ArrayList<DropScreenIntentModel>,
            childFragmentManager : FragmentManager
        ){
            DropSelectionFragment2().apply {
                arguments = bundleOf(
                    INTENT_SELECTIONS_TO_DROP to selectionJoiningsToDrop,

                )
            }.show(childFragmentManager,TAG)
        }
    }


    private lateinit var selectionJoiningsToDrop: ArrayList<DropScreenIntentModel>
    private var selectionJoiningIdsToDrop = arrayListOf<String>()
    private var selectionsToDrop = arrayListOf<DropDetail>()
    private var isBankDetailsVerified: Boolean = false
    private var gigStartDate: Date? = null
    private var gigEndDate: Date? = null
    private var currentDate: Date? = null
    private var selectedLastWorkingDate: String? = null
    private val viewModel: DropSelectionFragment2ViewModel by viewModels()
    private val sharedLeadMgmtViewModel: LeadManagementSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            selectionJoiningsToDrop = it.getParcelableArrayList<DropScreenIntentModel>(INTENT_SELECTIONS_TO_DROP) ?: return@let
        }

        savedInstanceState?.let {
            selectionJoiningsToDrop = it.getParcelableArrayList<DropScreenIntentModel>(INTENT_SELECTIONS_TO_DROP) ?: return@let
        }
        logDataReceivedFromBundles()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_SELECTIONS_TO_DROP,
            selectionJoiningsToDrop
        )
    }

    private fun logDataReceivedFromBundles() {
        if (::selectionJoiningsToDrop.isInitialized) {
            logger.d(logTag, "Joinings received from bundles : $selectionJoiningsToDrop")
        } else {
            logger.e(
                logTag,
                "no selectionJoiningsToDrop-id received from bundles",
                Exception("no selectionJoiningsToDrop-id received from bundles")
            )
        }
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
        selectionJoiningsToDrop.forEach {
            selectionJoiningIdsToDrop.add(it.joiningId)
        }
        lastWorkingLayout.dateText.text = DateHelper.getDateInDDMMMYYYYComma(Date())
        selectedLastWorkingDate = DateHelper.getDateInYYYYMMDD(Date())
        if (selectionJoiningsToDrop.size == 1){
            //single selection
            currentDate = DateHelper.getDateFromString(selectionJoiningsToDrop.get(0).currentDate)
            gigStartDate = DateHelper.getDateFromString(selectionJoiningsToDrop.get(0).gigStartDate)
            gigEndDate = DateHelper.getDateFromString(selectionJoiningsToDrop.get(0).gigEndDate)
            isBankDetailsVerified = selectionJoiningsToDrop.get(0).isBankVerified
            if (currentDate!! < gigStartDate){
                //normal drop
                showDirectDropLayout()
            }else{
                showJoinedNotJoinedLayout()
            }
        }else{
            //for multi selection in future
        }
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
                confirmDrop.alpha = 1f
                confirmDrop.isEnabled = true
            }
        }

        mainLayout.apply {
            //giger has not joined the gig
            dropSelectionButton.setOnClickListener {
                //call drop api
                val joiningId = selectionJoiningsToDrop.get(0).joiningId
                val lastWorkingDate = getFormattedDateInYYYYMMDD(selectionJoiningsToDrop.get(0).currentDate)
                val message = "Giger has not joined the gig"
                val newCal = Calendar.getInstance()
                val droppedDate = DateHelper.getDateInyyyyMMddHHmmss(newCal.time)
                val dropDetail = DropDetail(joiningId = joiningId, lastWorkingDate, message, droppedDate )
                selectionsToDrop.add(dropDetail)
                viewModel.dropSelections(selectionsToDrop)

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
                //call drop api this will have the last working date
                val joiningId = selectionJoiningsToDrop.get(0).joiningId
                val lastWorkingDate = selectedLastWorkingDate
                val message = "Giger has resigned after working"
                val newCal = Calendar.getInstance()
                val droppedDate = DateHelper.getDateInyyyyMMddHHmmss(newCal.time)
                val dropDetail = DropDetail(joiningId = joiningId, lastWorkingDate, message, droppedDate)
                selectionsToDrop.add(dropDetail)
                viewModel.dropSelections(selectionsToDrop)
            }
            cancelBtn.setOnClickListener {
                dismiss()
            }
        }
        directDropLayout.apply {
            dropSelectionDirect.setOnClickListener {
                //call drop api, current date is lesser than gig start date
                val joiningId = selectionJoiningsToDrop.get(0).joiningId
                val lastWorkingDate = getFormattedDateInYYYYMMDD(selectionJoiningsToDrop.get(0).currentDate)
                val message = "Giger has not joined the gig"
                val newCal = Calendar.getInstance()
                val droppedDate = DateHelper.getDateInyyyyMMddHHmmss(newCal.time)
                val dropDetail = DropDetail(joiningId = joiningId, lastWorkingDate, message, droppedDate )
                selectionsToDrop.add(dropDetail)
                viewModel.dropSelections(selectionsToDrop)
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }

        successLayout.apply {
            okayButton.setOnClickListener {
                setFragmentResult("drop_status", bundleOf("drop_status" to "dropped"))
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
        this.directDropLayout.root.gone()
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
        this.directDropLayout.root.gone()
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
        this.directDropLayout.root.gone()
    }

    private fun showDirectDropLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.gone()
        this.lastWorkingLayout.root.gone()
        this.bankVerifyLayout.root.gone()
        this.joinedNotJoinedLayout.root.gone()
        this.directDropLayout.root.visible()
    }
    private fun showJoinedNotJoinedLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.gone()
        this.lastWorkingLayout.root.gone()
        this.bankVerifyLayout.root.gone()
        this.joinedNotJoinedLayout.root.visible()
        this.directDropLayout.root.gone()
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
                selectedLastWorkingDate = DateHelper.getDateInYYYYMMDD(newCal.time)

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = gigEndDate!!.time
        datePickerDialog.datePicker.minDate = gigStartDate!!.time
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
                    viewBinding.directDropLayout.root.gone()
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

                    viewBinding.lastWorkingLayout.confirmButton.showProgress {
                        buttonText = "Dropping..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.lastWorkingLayout.confirmButton.isEnabled = false

                    viewBinding.directDropLayout.dropSelectionDirect.showProgress {
                        buttonText = "Dropping..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.directDropLayout.dropSelectionDirect.isEnabled = false

                }
                is Lce.Content -> {
                    //check if the api call was successful and drop api status is true
                    if (it.content.status){
                        viewBinding.successLayout.root.visible()
                        viewBinding.mainLayout.root.gone()
                        viewBinding.errorLayout.root.gone()
                        viewBinding.lastWorkingLayout.root.gone()
                        viewBinding.joinedNotJoinedLayout.root.gone()
                        viewBinding.bankVerifyLayout.root.gone()
                        viewBinding.directDropLayout.root.gone()
                    }else{
                        //drop selection failed
                        viewBinding.mainLayout.dropSelectionButton.hideProgress("Drop Selection")
                        viewBinding.mainLayout.dropSelectionButton.isEnabled = false
                        viewBinding.successLayout.root.gone()
                        viewBinding.mainLayout.root.gone()
                        viewBinding.lastWorkingLayout.root.gone()
                        viewBinding.joinedNotJoinedLayout.root.gone()
                        viewBinding.bankVerifyLayout.root.gone()
                        viewBinding.directDropLayout.root.gone()
                        viewBinding.errorLayout.root.visible()
                        viewBinding.errorLayout.infoMessageTv.text = it.content.misingFields?.get(0)?.errorMessage
                        viewBinding.errorLayout.retryBtn.visible()
                    }
                }
            }
        })

    fun getFormattedDateInYYYYMMDD(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }
}