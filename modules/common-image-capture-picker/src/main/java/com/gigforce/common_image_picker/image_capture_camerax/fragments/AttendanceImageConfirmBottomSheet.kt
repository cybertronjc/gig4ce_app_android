package com.gigforce.common_image_picker.image_capture_camerax.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.databinding.FragmentAttendanceImageConfirmBottomSheetBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment

class AttendanceImageConfirmBottomSheet : BaseBottomSheetDialogFragment<FragmentAttendanceImageConfirmBottomSheetBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_attendance_image_confirm_bottom_sheet
) {
    companion object {
        const val TAG = "AttendanceImageConfirmBottomSheet"
        fun launch(
            childFragmentManager : FragmentManager
        ){
            AttendanceImageConfirmBottomSheet().apply {
            }.show(childFragmentManager,TAG)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun viewCreated(
        viewBinding: FragmentAttendanceImageConfirmBottomSheetBinding,
        savedInstanceState: Bundle?
    ) {
        listeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }


    private fun listeners() = viewBinding.apply {
        submitImage.setOnClickListener {
            //submit clicked picture
            setFragmentResult("imageConfirm", bundleOf("confirm" to 1))
            dismiss()
        }

        tryAgainImage.setOnClickListener {
            //retake picture
            setFragmentResult("imageConfirm", bundleOf("confirm" to 0))
            dismiss()
        }
    }


}