package com.gigforce.app.tl_work_space.activity_tacker.attendance_details

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.app.navigation.tl_workspace.attendance.ActivityTrackerNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.navigation.lead_management.LeadManagementNavigation
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropScreenIntentModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewState
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.navigation.tl_workspace.attendance.GigAttendanceConstants
import com.gigforce.app.tl_work_space.activity_tacker.SharedAttendanceTLSharedViewModelEvents
import com.gigforce.app.tl_work_space.databinding.FragmentGigerAttendanceDetailsBinding
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class GigerAttendanceDetailsFragment :
    BaseBottomSheetDialogFragment<FragmentGigerAttendanceDetailsBinding>(
        fragmentName = TAG,
        layoutId = R.layout.fragment_giger_attendance_details
    ) {

    companion object {
        const val TAG = "GigerAttendanceDetailsFragment"
    }

    @Inject
    lateinit var tlWorkSpaceActivityTrackerNavigation: ActivityTrackerNavigation

    @Inject
    lateinit var gigNavigation: GigNavigation

    @Inject
    lateinit var leadManagementNavigation: LeadManagementNavigation

    private val viewModel: GigerAttendanceDetailsViewModel by viewModels()
    private val sharedGigViewModel: AttendanceTLSharedViewModel by activityViewModels()
    private val gigsJoiningSharedViewModel : SharedGigViewModel by activityViewModels()
    private lateinit var gigId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        viewModel.setGigerAttendanceReceivedFromPreviousScreen(
            gigId = gigId
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentGigerAttendanceDetailsBinding,
        savedInstanceState: Bundle?
    ) {
        val bottomSheet = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR;
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT);
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);

        if (viewCreatedForTheFirstTime) {

            initView()
            initViewModel()
            initSharedViewModel()
        }
    }

    private fun initSharedViewModel() {
        lifecycleScope.launchWhenCreated {

            sharedGigViewModel.sharedEvents
                .collect {

                    when (it) {
                        is SharedAttendanceTLSharedViewModelEvents.AttendanceUpdated -> viewModel.gigUpdateReceived(
                            it.attendance
                        )
                    }
                }
        }

        lifecycleScope.launchWhenCreated {

            gigsJoiningSharedViewModel.gigSharedViewModelState
                .collect {

                    when (it) {
                        is SharedGigViewState.TeamLeaderOfGigerChangedWithGigId -> dismiss()
                        is SharedGigViewState.UserDroppedWithGig -> dismiss()
                    }
                }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog.apply {
            setOnShowListener { dialog -> // In a previous life I used this method to get handles to the positive and negative buttons
                // of a dialog in order to change their Typeface. Good ol' days.
                val d: BottomSheetDialog = dialog as BottomSheetDialog

                // This is gotten directly from the source of BottomSheetDialog
                // in the wrapInBottomSheet() method
                val bottomSheet =
                    d.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

                bottomSheet?.let {
                    BottomSheetBehavior.from(it).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
    }

    private fun initView() = viewBinding.apply {

        this.mainLayout.apply {
            this.callLayout.floatingActionButton.setImageResource(R.drawable.ic_call_phone_pink)
            this.callLayout.textView.text = "Call"
            this.callLayout.floatingActionButton.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.CallGigerButtonClicked)
            }
            this.callLayout.root.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.CallGigerButtonClicked)
            }

            this.changeTlLayout.floatingActionButton.setImageResource(R.drawable.ic_change_pink)
            this.changeTlLayout.textView.text = "Change TL"
            this.changeTlLayout.root.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.ChangeTLButtonClicked)
            }
            this.changeTlLayout.floatingActionButton.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.ChangeTLButtonClicked)
            }

            this.attendanceHistoryLayout.floatingActionButton.setImageResource(R.drawable.ic_calendar_pink)
            this.attendanceHistoryLayout.textView.text = "Att. History"
            this.attendanceHistoryLayout.floatingActionButton.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.AttendanceHistoryClicked)
            }
            this.attendanceHistoryLayout.root.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.AttendanceHistoryClicked)
            }

            this.dropGigerLayout.floatingActionButton.setImageResource(R.drawable.ic_block_pink)
            this.dropGigerLayout.textView.text = "Drop Giger"
            this.dropGigerLayout.floatingActionButton.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.DropGigerClicked)
            }
            this.dropGigerLayout.root.setOnClickListener {
                viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.DropGigerClicked)
            }
        }

        this.mainLayout.activeButton.setOnClickListener {
            viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.ActiveButtonClicked)
        }

        this.mainLayout.inactiveButton.setOnClickListener {
            viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.InactiveButtonClicked)
        }
    }

    private fun initViewModel() {

        lifecycleScope.launch {

            viewModel.viewState.collect {
                when (it) {
                    is GigerAttendanceDetailsViewContract.State.ErrorInLoadingAttendanceDetails -> errorInLoadingAttendanceDetails(
                        it.error
                    )
                    is GigerAttendanceDetailsViewContract.State.LoadingAttendanceDetails -> showAttendanceLoading()
                    is GigerAttendanceDetailsViewContract.State.ShowAttendanceDetails -> showAttendanceDetailsOnView(
                        it.attendanceDetails
                    )
                }
            }
        }

        lifecycleScope.launch {

            viewModel.viewEffects.collect {
                when (it) {
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerActiveScreen -> tlWorkSpaceActivityTrackerNavigation.openActiveConfirmationDialog(
                        gigId = it.gigId,
                        hasGigerMarkedHimselfInactive = it.hasGigerMarkedHimselfInActive
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerInActiveConfirmationScreen -> tlWorkSpaceActivityTrackerNavigation.openMarkInactiveConfirmationDialog(
                        it.gigId,
                        false
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenSelectGigerInActiveReasonScreen -> tlWorkSpaceActivityTrackerNavigation.openMarkInactiveReasonDialog(
                        it.gigId
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenResolveAttendanceScreen -> tlWorkSpaceActivityTrackerNavigation.openResolveAttendanceConflictDialog(
                        it.gigId,
                        it.gigAttendanceData

                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.CallGiger -> callGiger(
                        it.phoneNumber
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenChangeTLScreen -> openChangeTlScreen(
                        it.gigId,
                        it.gigerId,
                        it.gigerName,
                        it.teamLeaderUid
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenDropGigerScreen -> openDropScreen(
                        it.dropScreenData
                    )
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenMonthlyAttendanceScreen -> gigNavigation.openGigAttendanceHistoryScreen(
                        gigDate = it.date,
                        gigTitle = it.jobProfile ?: "",
                        gigOrderId = it.gigOrderId,
                        companyLogo = it.companyLogo ?: "",
                        companyName = it.companyName ?: ""
                    )
                }
            }
        }
    }

    private fun openDropScreen(
        dropScreenData: DropScreenIntentModel
    ) = leadManagementNavigation.openDropJoiningOrGigerScreen(dropScreenData)

    private fun openChangeTlScreen(
        gigId: String,
        gigerId: String?,
        gigerName: String?,
        teamLeaderUid: String
    ) {
        leadManagementNavigation.openChangeTLBottomSheet(
            arrayListOf(
                ChangeTeamLeaderRequestItem(
                    gigerUid = gigerId,
                    gigerName = gigerName,
                    teamLeaderId = teamLeaderUid,
                    joiningId = null,
                    gigId = gigId
                )
            )
        )
    }

    private fun callGiger(phoneNumber: String) {

        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAttendanceLoading() = viewBinding.apply {
        mainLayout.root.gone()
        infoLayout.root.gone()

        shimmerContainer.visible()
        startShimmer(
            this.shimmerContainer,
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

    private fun showAttendanceDetailsOnView(
        attendanceDetails: GigAttendanceData
    ) = viewBinding.apply {

        shimmerContainer.gone()
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )

        infoLayout.root.gone()
        mainLayout.root.visible()

        showInfoOnView(attendanceDetails)

        lifecycleScope.launch {

            delay(400)
            try {
                dialog
                    ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.requestLayout()
            } catch (e: Exception) {
                //Ignore this one
            }
        }
    }


    private fun showInfoOnView(
        attendanceDetails: GigAttendanceData
    ) = viewBinding.mainLayout.apply {

        this.statusView.bind(
            attendanceDetails.status,
            attendanceDetails.statusBackgroundColorCode,
            attendanceDetails.statusTextColorCode,
            false
        )
        this.userImageImageview.loadProfilePicture(
            attendanceDetails.gigerImage,
            attendanceDetails.gigerImage
        )
        this.nameTextview.text = attendanceDetails.gigerName
        this.lastActiveTextview.text = attendanceDetails.lastActiveText
        this.markedByTextview.text = attendanceDetails.markedByText

        this.gigerMarkedAttendanceStatus.isVisible = attendanceDetails.showGigerAttendanceLayout
        if (attendanceDetails.gigerAttendanceStatus != null) {
            this.gigerMarkedAttendanceStatus.bind(
                attendanceDetails.gigerAttendanceStatus!!,
                null,//TODO
                attendanceDetails.hasAttendanceConflict,
                false
            )
        }

        this.infoLayout.bind(
            attendanceDetails
        )

        if (attendanceDetails.hasAttendanceConflict.not() &&
            (attendanceDetails.canTLMarkPresent || attendanceDetails.canTLMarkAbsent)) {

            this.attendanceTextview.isVisible = true
            this.attendanceActionButtonsLayout.isVisible = true
            this.divider1.isVisible = true

            if (attendanceDetails.canTLMarkAbsent && attendanceDetails.canTLMarkPresent) {
                this.attendanceTextview.text = "Mark Attendance"
            } else {
                this.attendanceTextview.text = "Change Attendance Status"
            }

            this.activeButton.isVisible = attendanceDetails.canTLMarkPresent
            this.inactiveButton.isVisible = attendanceDetails.canTLMarkAbsent

            if (attendanceDetails.canTLMarkPresent && attendanceDetails.canTLMarkAbsent) {
                val params = this.activeButton.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    24,
                    params.bottomMargin,
                )

                val params2 = this.inactiveButton.layoutParams as ViewGroup.MarginLayoutParams
                params2.setMargins(
                    24,
                    params2.topMargin,
                    params2.rightMargin,
                    params2.bottomMargin,
                )
            } else {

                val params = this.activeButton.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    0,
                    params.bottomMargin,
                )

                val params2 = this.inactiveButton.layoutParams as ViewGroup.MarginLayoutParams
                params2.setMargins(
                    0,
                    params2.topMargin,
                    params2.rightMargin,
                    params2.bottomMargin,
                )
            }

        } else {
            this.attendanceTextview.isVisible = false
            this.attendanceActionButtonsLayout.isVisible = false
            this.divider1.isVisible = false
        }
    }

    private fun errorInLoadingAttendanceDetails(
        error: String
    ) = viewBinding.apply {
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        infoLayout.root.visible()
        infoLayout.infoMessageTv.text = error
    }

}