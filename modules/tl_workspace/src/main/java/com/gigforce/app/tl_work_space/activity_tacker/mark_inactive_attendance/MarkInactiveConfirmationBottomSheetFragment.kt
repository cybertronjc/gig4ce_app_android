package com.gigforce.app.tl_work_space.activity_tacker.mark_inactive_attendance

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
import com.gigforce.app.navigation.tl_workspace.attendance.GigAttendanceConstants
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.tl_work_space.databinding.FragmentMarkInactiveConfirmationBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarkInactiveConfirmationBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentMarkInactiveConfirmationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_mark_inactive_confirmation
) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"
    }


    private val viewModel: AttendanceTLSharedViewModel by activityViewModels()
    private val sharedViewModel: AttendanceTLSharedViewModel by activityViewModels()

    private lateinit var gigId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentMarkInactiveConfirmationBinding,
        savedInstanceState: Bundle?
    ) {
        val bottomSheet  = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        if (viewCreatedForTheFirstTime) {
            initView()
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
        this.confirmationTextLabel.text = buildSpannedString {
            append("Giger has marked ")
            color(ResourcesCompat.getColor(resources, R.color.green_medium, null)) {
                append("Active")
            }
            append(" in his app. Do you still want to mark him as Inactive? ")
        }

        this.yesButton.setOnClickListener {
            sharedViewModel.openMarkInactiveReasonsDialog(gigId)
        }

        this.noButton.setOnClickListener {
            dismiss()
        }
    }
}