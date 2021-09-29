package com.gigforce.lead_management.ui.drop_selection

import android.os.Bundle
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding

class DropSelectionBottomSheetDialogFragment : BaseBottomSheetDialogFragment<BottomSheetDialogFragmentDropSelectionBinding>(
    fragmentName = "DropSelectionBottomSheetDialogFragment",
    layoutId = R.layout.bottom_sheet_dialog_fragment_drop_selection
) {
    override fun viewCreated(
        viewBinding: BottomSheetDialogFragmentDropSelectionBinding,
        savedInstanceState: Bundle?
    ) {

    }
}