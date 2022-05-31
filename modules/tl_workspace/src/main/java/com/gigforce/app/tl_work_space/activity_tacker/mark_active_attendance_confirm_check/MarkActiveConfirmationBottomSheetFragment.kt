package com.gigforce.app.tl_work_space.activity_tacker.mark_active_attendance_confirm_check

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.app.navigation.tl_workspace.attendance.GigAttendanceConstants
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.tl_work_space.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

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
        val bottomSheet  = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

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

                        is MarkActiveViewContract.UiState.ErrorWhileMarkingPresent -> errorWhileMarkingDecline(
                            it.error
                        )
                        MarkActiveViewContract.UiState.MarkingPresent -> showMarkingDecline()
                        MarkActiveViewContract.UiState.PresentMarkedSuccessfully -> dismiss()
                        MarkActiveViewContract.UiState.ScreenLoaded -> {}
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
        return dialog.apply {
            setOnShowListener { dialog ->

                //Makes the Bottom Open full , without it opens half
                val d: BottomSheetDialog = dialog as BottomSheetDialog
                val bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

                bottomSheet?.let {
                    BottomSheetBehavior.from(it).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
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