package com.gigforce.app.tl_work_space.user_info_bottomsheet

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.navigation.tl_workspace.attendance.ActivityTrackerNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.gigforce.app.tl_work_space.user_info_bottomsheet.views.UserDetailBusinessAndUserDetailsView
import com.gigforce.app.tl_work_space.user_info_bottomsheet.views.UserDetailsAndActionButtonsView
import com.gigforce.app.tl_work_space.user_info_bottomsheet.views.WarningCardView
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.navigation.lead_management.LeadManagementNavigation
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewState
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class UserInfoBottomSheetFragment : BaseBottomSheetDialogFragment<BottomsheetGigerInfoBinding>(
    fragmentName = TAG,
    layoutId = R.layout.bottomsheet_giger_info
) {
    companion object {
        const val TAG = "UserInfoBottomSheetFragment"
    }

    @Inject
    lateinit var tlWorkSpaceActivityTrackerNavigation: ActivityTrackerNavigation

    @Inject
    lateinit var gigNavigation: GigNavigation

    @Inject
    lateinit var leadManagementNavigation: LeadManagementNavigation

    private val viewModel: UserInfoBottomSheetViewModel by viewModels()
    private val sharedGigViewModel: AttendanceTLSharedViewModel by activityViewModels()
    private val gigsJoiningSharedViewModel: SharedGigViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            val openDetailsFor =
                it.getString(TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF) ?: return@let
            val gigerId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_ID) ?: return@let
            val businessId =
                it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID) ?: return@let
            val jobProfileId =
                it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID) ?: return@let
            val payoutId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_PAYOUT_ID)

            viewModel.setKeysReceivedFromPreviousScreen(
                openDetailsFor,
                gigerId,
                businessId,
                jobProfileId,
                payoutId
            )
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: BottomsheetGigerInfoBinding,
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

    }

    private fun initViewModel() {

        lifecycleScope.launch {

            viewModel.uiState.collect {
                when (it) {
                    is GigerInformationDetailsBottomSheetFragmentViewState.ErrorWhileFetchingGigerInformation -> errorInLoadingDetails(
                        it.errorMessage
                    )
                    GigerInformationDetailsBottomSheetFragmentViewState.LoadingGigerInformation -> showLoadingView()
                    is GigerInformationDetailsBottomSheetFragmentViewState.ShowGigerInformation -> showGigerInformationDetailsOnView(
                        it.viewItems
                    )
                }
            }
        }

        lifecycleScope.launch {

            viewModel.effect.collect {
                when (it) {
                    is GigerInformationDetailsBottomSheetFragmentViewEffects.CallPhoneNumber -> callPhoneNumber(
                        it.phoneNumber
                    )
                    is GigerInformationDetailsBottomSheetFragmentViewEffects.DownloadPayslip -> startDocumentDownload(
                        it.businessName,
                        it.payslipUrl
                    )
                    is GigerInformationDetailsBottomSheetFragmentViewEffects.DropGiger -> openDropScreen(
                        jobProfileId = it.jobProfileId,
                        gigerId = it.gigerId
                    )
                }
            }
        }
    }

    private fun openDropScreen(
        jobProfileId: String,
        gigerId: String
    ) {

    }

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

    private fun callPhoneNumber(phoneNumber: String) {

        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoadingView() = viewBinding.apply {
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
                orientation = LinearLayout.VERTICAL,
                itemsToBeDrawn = 3
            ),
            R.id.shimmer_controller
        )
    }

    private fun showGigerInformationDetailsOnView(
        attendanceDetails: List<UserInfoBottomSheetData>
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
        attendanceDetails: List<UserInfoBottomSheetData>
    ) = viewBinding.mainLayout.mainLinearLayout.apply {

        attendanceDetails.forEach {

            if (it is UserInfoBottomSheetData.UserDetailsAndActionData) {

                val view = UserDetailsAndActionButtonsView(requireContext(), null)
                addView(view)
                view.bind(it)
            } else if (it is UserInfoBottomSheetData.RetentionComplianceWarningCardData) {

                val view = WarningCardView(requireContext(), null)
                addView(view)
                view.bind(it)
            } else if (it is UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData) {

                val view = UserDetailBusinessAndUserDetailsView(requireContext(), null)
                addView(view)
                view.bind(it)
            }
        }
    }

    private fun errorInLoadingDetails(
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

    private fun startDocumentDownload(
        businessName: String,
        url: String
    ) {
        try {
            val filePathName = FirebaseUtils.extractFilePath(url)

            val downloadRequest = DownloadManager.Request(Uri.parse(url)).run {
                setTitle(filePathName)
                setDescription(businessName)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filePathName
                )
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            }

            val downloadManager = requireContext()
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(downloadRequest)

            ToastHandler.showToast(
                requireContext(),
                "Saving file in Downloads,check notification...",
                Toast.LENGTH_LONG
            )
        } catch (e: Exception) {
            ToastHandler.showToast(
                requireContext(),
                "Unable to start payslip download",
                Toast.LENGTH_LONG
            )

            logger.e(
                TAG,
                "Unable to start payslip download",
                e
            )
        }
    }

}