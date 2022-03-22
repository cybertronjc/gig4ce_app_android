package com.gigforce.giger_gigs.attendance_tl.mark_active_attendance_confirm_check

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.attendance_tl.select_decline_reasons.SelectMarkInactiveReasonsBottomSheetViewModel
import com.gigforce.giger_gigs.attendance_tl.select_decline_reasons.SelectMarkInactiveReasonsViewContract
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MarkActiveConfirmationBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentMarkActiveConfirmationBinding>(
        fragmentName = TAG,
        layoutId = R.layout.fragment_mark_active_confirmation
    ) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"
    }

    private val viewModel: MarkActiveBottomSheetViewModel by viewModels()
    private val sharedViewModel: AttendanceTLSharedViewModel by activityViewModels()

    private lateinit var gigId: String
    private var hasGigerMarkedHimselfInactive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            hasGigerMarkedHimselfInactive = it.getBoolean(GigAttendanceConstants.INTENT_HAS_GIGER_MARKED_HIMSELF_INACTIVE)
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            hasGigerMarkedHimselfInactive = it.getBoolean(GigAttendanceConstants.INTENT_HAS_GIGER_MARKED_HIMSELF_INACTIVE)
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
        outState.putBoolean(GigAttendanceConstants.INTENT_HAS_GIGER_MARKED_HIMSELF_INACTIVE, hasGigerMarkedHimselfInactive)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentMarkActiveConfirmationBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    private fun initViewModel() {
        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    when (it) {

                        is MarkInactiveReasonsViewContract.UiState.ErrorWhileMarkingPresent -> errorWhileMarkingDecline(
                            it.error
                        )
                        MarkInactiveReasonsViewContract.UiState.MarkingPresent -> showMarkingDecline()
                        MarkInactiveReasonsViewContract.UiState.PresentMarkedSuccessfully -> dismiss()
                        MarkInactiveReasonsViewContract.UiState.ScreenLoaded -> {}
                    }
                }
        }
    }

    private fun errorWhileMarkingDecline(
        error: String
    ) = viewBinding.apply {

        this.noButton.isEnabled = true
        this.yesButton.isEnabled = true
        this.yesButton.hideProgress("Yes")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to mark present")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun showMarkingDecline() = viewBinding.apply {

        this.noButton.isEnabled = false
        this.yesButton.isEnabled = false
        this.yesButton.showProgress {
            this.buttonText = "Marking Present.."
            this.progressColor = Color.WHITE
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.apply {

        if (hasGigerMarkedHimselfInactive) {
            this.attendanceLabel.text = "Are you sure that giger is Active today?"
            this.confirmationTextLabel.text = buildSpannedString {
                append("Giger has marked ")
                color(ResourcesCompat.getColor(resources, R.color.lipstick_2, null)) {
                    append("Inactive")
                }
                append(" in his app. Do you still want to mark him as Active? ")
            }
        } else {
            this.attendanceLabel.text = "Mark Giger Active"
            this.confirmationTextLabel.text = "Are you sure that you want to mark giger active?"
        }

        this.yesButton.setOnClickListener {
            viewModel.markPresent(
                gigId,
                sharedViewModel
            )
        }

        this.noButton.setOnClickListener {
            dismiss()
        }
    }
}