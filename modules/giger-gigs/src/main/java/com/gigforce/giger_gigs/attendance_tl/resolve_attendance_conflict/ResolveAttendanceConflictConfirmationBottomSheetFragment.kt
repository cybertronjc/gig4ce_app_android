package com.gigforce.giger_gigs.attendance_tl.resolve_attendance_conflict

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.attendance_tl.select_decline_reasons.SelectMarkInactiveReasonsBottomSheetViewModel
import com.gigforce.giger_gigs.attendance_tl.select_decline_reasons.SelectMarkInactiveReasonsViewContract
import com.gigforce.giger_gigs.databinding.FragmentGigerAttendanceDetailsBinding
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentResolveAttendanceConflictConfirmationBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.GigAttendanceData
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResolveAttendanceConflictConfirmationBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentResolveAttendanceConflictConfirmationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_resolve_attendance_conflict_confirmation
) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"
    }

    private val viewModel: ResolveAttendanceConflictBottomSheetViewModel by viewModels()
    private val sharedViewModel: AttendanceTLSharedViewModel by activityViewModels()

    private lateinit var gigId: String
    private lateinit var gigAttendanceData: GigAttendanceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            gigAttendanceData = it.getParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            gigAttendanceData = it.getParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS) ?: return@let
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
        outState.putParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS, gigAttendanceData)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentResolveAttendanceConflictConfirmationBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.apply {

        val hasGigerMarkedHimselfActive = AttendanceStatus.PRESENT == gigAttendanceData.gigerAttendanceStatus
        if (hasGigerMarkedHimselfActive) {
            this.confirmationTextLabel.text = buildSpannedString {
                append("Giger has marked ")
                color(ResourcesCompat.getColor(resources, R.color.lipstick_2, null)) {
                    append("Active")
                }
                append(" in his app. but you have marked Inactive.Do you want to change your response?")
            }
        } else {
            this.confirmationTextLabel.text = buildSpannedString {
                append("Giger has marked ")
                color(ResourcesCompat.getColor(resources, R.color.lipstick_2, null)) {
                    append("Inactive")
                }
                append(" in his app. but you have marked Active.Do you want to change your response?")
            }
        }

        this.yesButton.setOnClickListener {
            viewModel.resolveConflict(
                gigAttendanceData.resolveId!!,
                true,
                sharedViewModel
            )
        }

        this.noButton.setOnClickListener {

            viewModel.resolveConflict(
                gigAttendanceData.resolveId!!,
                false,
                sharedViewModel
            )
        }
    }


    private fun initViewModel() {
        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    when (it) {
                        ResolveAttendanceConflictViewContract.UiState.ConflictResolvedSuccessfully -> dismiss()
                        is ResolveAttendanceConflictViewContract.UiState.ErrorWhileResolvingConflict -> errorWhileResolvingConflict(it.error)
                        ResolveAttendanceConflictViewContract.UiState.ResolvingConflict -> resolvingConflict()
                        ResolveAttendanceConflictViewContract.UiState.ScreenLoaded -> {}
                    }
                }
        }
    }


    private fun errorWhileResolvingConflict(
        error: String
    ) = viewBinding.apply {

        this.noButton.isEnabled = true
        this.yesButton.isEnabled = true
        this.yesButton.hideProgress("Yes")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to resolve conflict")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun resolvingConflict() = viewBinding.apply {

        this.noButton.isEnabled = false
        this.yesButton.isEnabled = false
        this.yesButton.showProgress {
            this.buttonText = "Resolving.."
            this.progressColor = Color.WHITE
        }
    }
}