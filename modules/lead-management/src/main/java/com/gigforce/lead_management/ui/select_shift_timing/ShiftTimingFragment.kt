package com.gigforce.lead_management.ui.select_shift_timing

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobShift
import com.gigforce.common_ui.viewdatamodels.leadManagement.WorkingDays
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.navigation.LeadManagementConstants
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ShiftTimingFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

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

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val viewModel: ShiftTimingViewModel by viewModels()
    private lateinit var userUid: String
    private lateinit var assignGigRequest: AssignGigRequest
    private var currentGigerInfo: GigerProfileCardDVM? = null

    val selectedShifts = arrayListOf<JobShift>()
    var shiftChips = arrayListOf<ChipGroupModel>()
    var shifts = listOf<JobShift>()

    val selectedWorkingDays = arrayListOf<WorkingDays>()
    var workingDaysChips = arrayListOf<ChipGroupModel>()
    var workingDays = listOf<WorkingDays>()

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
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL) ?: return@let
            currentGigerInfo = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL) ?: return@let
            currentGigerInfo = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
        }
        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {
        if (::userUid.isInitialized) {
            logger.d(logTag, "User-id received from bundles : $userUid")
        } else {
            logger.e(
                logTag,
                "no User-id received from bundles",
                Exception("no User-id received from bundles")
            )
        }

        if (::assignGigRequest.isInitialized.not()) {
            logger.e(
                logTag,
                "null assignGigRequest received from bundles",
                Exception("null assignGigRequest received from bundles")
            )
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        outState.putParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL, assignGigRequest)
        outState.putParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO, currentGigerInfo)
    }

    private fun initViewModel() {
        viewModel.getJobProfileDetails(assignGigRequest.jobProfileId, userUid)
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

    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            hideActionMenu()
            showTitle(context.getString(R.string.shift_timings_lead))
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        submitBtn.setOnClickListener {
            selectedShifts.clear()
            shiftChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected){
                    selectedShifts.add(shifts.get(index))
                }
            }

            selectedWorkingDays.clear()
            workingDaysChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected){
                    selectedWorkingDays.add(workingDays.get(index))
                }
            }

            when {
                selectedShifts.isEmpty() -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.select_atleast_one_shift_lead))
                        .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                        .show()
                }
                selectedWorkingDays.isEmpty() -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.select_one_working_day_lead))
                        .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                        .show()
                }
                expectedDate.text.isEmpty() -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.select_expected_date_lead))
                        .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                        .show()
                }
                else -> {
                    assignGigRequest.shift = selectedShifts
                    assignGigRequest.workingDays = selectedWorkingDays.first()

                    logger.d(TAG, "AssignGigRequest $assignGigRequest")
                    navigation.navigateTo(
                        LeadManagementNavDestinations.FRAGMENT_SELECT_TEAM_LEADERS, bundleOf(
                            LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to assignGigRequest,
                            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to currentGigerInfo
                        ))
                }
            }
        }
        calendarIcon.setOnClickListener {
            expectedStartDatePicker.show()
        }

        expectedDate.setOnClickListener {
            expectedStartDatePicker.show()
        }

        if (currentGigerInfo != null) {
            viewBinding.gigerProfileCard.setProfileCard(currentGigerInfo!!)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewBinding.gigerProfileCard.setGigerProfileData(userUid)
            }
        }

    }


    private val expectedStartDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewBinding.expectedDate.text = DateHelper.getDateInDDMMYYYY(newCal.time)
                assignGigRequest.assignGigsFrom = newCal.time.toLocalDate().format(dateFormatter)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = Date().time
        datePickerDialog
    }

    private fun showGigShifts(jobProfile: JobProfileDetails) = viewBinding.apply {

        stopShimmer(
            this.shiftShimmerContainer,
            R.id.shimmer_controller
        )
        shiftLayout.visible()
        shiftShimmerContainer.gone()
        shiftInfoLayout.root.gone()
        //set chips for gig shift timings

        shifts = jobProfile.shifts
        if (shifts.isEmpty()) {
            showNoGigShiftsFound()
        } else {
            shiftChips.clear()
            shifts.forEachIndexed { index, jobShift ->
                jobShift.let {
                    shiftChips.add(ChipGroupModel(it.name.toString(), -1, index))
                }
            }
            viewBinding.shiftChipGroup.removeAllViews()
            viewBinding.shiftChipGroup.addChips(shiftChips, isSingleSelection = false, setFirstChecked = true)
            logger.d(TAG, "Shift timings chips ${shiftChips.size}  shifts $shifts")
        }

        workingDays = jobProfile.workingDays
        if (workingDays.isNotEmpty()) {
            workingDaysChips.clear()
            workingDays.forEachIndexed { index, workingDay ->
                workingDay.let {
                    workingDaysChips.add(ChipGroupModel(it.title.toString(), -1, index))
                }
            }
            viewBinding.workingDaysChipgroup.removeAllViews()
            viewBinding.workingDaysChipgroup.addChips(workingDaysChips, isSingleSelection = true, setFirstChecked = true)
            logger.d(TAG, "working day chips set ,count: ${workingDays.size}")
        }

        if(assignGigRequest.assignGigsFrom != ""){
            viewBinding.expectedDate.text = assignGigRequest.assignGigsFrom
        }
    }

    private fun showErrorInLoadingGigShifts(error: String) = viewBinding.apply{
        stopShimmer(
            shiftShimmerContainer,
            R.id.shimmer_controller
        )
        shiftShimmerContainer.gone()
        shiftInfoLayout.root.visible()
        shiftLayout.gone()
        shiftInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        shiftInfoLayout.infoMessageTv.text = error
    }

    private fun showNoGigShiftsFound() = viewBinding.apply {
        stopShimmer(
            shiftShimmerContainer,
            R.id.shimmer_controller
        )
        shiftShimmerContainer.gone()
        shiftInfoLayout.root.visible()
        shiftLayout.gone()
        shiftInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        shiftInfoLayout.infoMessageTv.text = getString(R.string.no_gig_shift_lead)
    }

    private fun showGigShiftAsLoading() = viewBinding.apply{
        shiftLayout.gone()
        shiftInfoLayout.root.gone()
        shiftShimmerContainer.visible()

        startShimmer(
            this.shiftShimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }


}