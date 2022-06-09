package com.gigforce.app.tl_work_space.drop_giger

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetDropSuccessBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DropSuccessBottomSheetFragment : BaseBottomSheetDialogFragment<BottomsheetDropSuccessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.bottomsheet_drop_success
) {
    companion object {
        const val TAG = "DropSuccessBottomSheetFragment"
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

    override fun viewCreated(
        viewBinding: BottomsheetDropSuccessBinding,
        savedInstanceState: Bundle?
    ) {
        val bottomSheet = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        listeners()
    }

    private fun listeners() = viewBinding.apply {

        this.okayButton.setOnClickListener {
            publishFilterResults()
        }
    }

    private fun publishFilterResults() {
        setFragmentResult(
            "drop_success",
            bundleOf(
                TLWorkSpaceNavigation.INTENT_EXTRA_SELECTED_DATE_FILTER to true
            )
        )
        dismiss()
    }
}