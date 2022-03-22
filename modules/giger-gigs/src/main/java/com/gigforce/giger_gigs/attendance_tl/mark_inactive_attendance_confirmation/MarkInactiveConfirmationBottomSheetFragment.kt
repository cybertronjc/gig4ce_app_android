package com.gigforce.giger_gigs.attendance_tl.mark_inactive_attendance_confirmation

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.giger_gigs.GigNavigation
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentMarkInactiveConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarkInactiveConfirmationBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentMarkInactiveConfirmationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_mark_inactive_confirmation
) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"
    }

    @Inject
    lateinit var gigNavigation: GigNavigation

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

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
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

        if (viewCreatedForTheFirstTime) {
            initView()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
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