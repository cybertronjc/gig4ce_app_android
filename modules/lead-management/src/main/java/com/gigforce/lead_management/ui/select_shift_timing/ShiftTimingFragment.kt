package com.gigforce.lead_management.ui.select_shift_timing

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ShiftTimingFragmentBinding
import com.gigforce.lead_management.ui.select_gig_location.SelectGigLocationFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class ShiftTimingFragment : BaseFragment2<ShiftTimingFragmentBinding>(
    fragmentName = "ShiftTimingFragment",
    layoutId = R.layout.shift_timing_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = ShiftTimingFragment()
        private const val TAG = "ShiftTimingFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: ShiftTimingViewModel by viewModels()
    private  var userUid: String = ""
    private  var jobProfileId: String = ""

    override fun viewCreated(
        viewBinding: ShiftTimingFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initListeners()
        initViewModel()
    }


    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
            jobProfileId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
            jobProfileId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_UID, userUid)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE, jobProfileId)
    }

    private fun initViewModel() {
        viewModel.getJobProfileDetails(jobProfileId, userUid)
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val jobProfileDetails = it

            when (jobProfileDetails) {
                is Lce.Content -> showGigShifts(jobProfileDetails.content)
                is Lce.Error -> showErrorInLoadingGigShifts(jobProfileDetails.error)
                Lce.Loading -> {
                    showGigShiftAsLoading()
                }
            }
        })
    }

    private fun initListeners() {
        viewBinding.toolbar.apply {
            hideActionMenu()
            showTitle("Shift Timings")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        viewBinding.submitBtn.setOnClickListener {
            navigation.navigateTo("LeadMgmt/selectTeamLeaders", bundleOf(
                LeadManagementConstants.INTENT_EXTRA_USER_UID to userUid,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE to jobProfileId
            )
            )
        }
        viewBinding.calendarIcon.setOnClickListener {
            expectedStartDatePicker.show()
        }

        viewBinding.expectedDate.setOnClickListener {
            expectedStartDatePicker.show()
        }

    }


    private val expectedStartDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewBinding.expectedDate.text = DateHelper.getDateInDDMMYYYY(newCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

//        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun showGigShifts(jobProfile: JobProfileDetails){
        //set chips for gig shift timings
        var shiftChips = arrayListOf<ChipGroupModel>()
        val shifts = jobProfile.shifts
        shifts.forEachIndexed { index, jobShift ->
            jobShift.let {
                shiftChips.add(ChipGroupModel(it.name.toString(), -1, index))
            }
        }
        viewBinding.shiftChipGroup.addChips(shiftChips)
        logger.d(TAG, "Shift timings ${shiftChips.toArray()}")
        viewBinding.shiftChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model: ChipGroupModel) {

            }
        })




    }

    private fun showErrorInLoadingGigShifts(error: String){

    }

    private fun showGigShiftAsLoading(){

    }


}