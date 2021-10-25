package com.gigforce.lead_management.ui.drop_selection_2

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding
import com.gigforce.lead_management.databinding.DropSelection2BottomSheetMainBinding
import com.gigforce.lead_management.databinding.DropSelectionFragment2FragmentBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.drop_selection.DropSelectionViewModel

class DropSelectionFragment2 : BaseBottomSheetDialogFragment<DropSelectionFragment2FragmentBinding>(
    fragmentName = "DropSelectionFragment2",
    layoutId = R.layout.drop_selection_fragment2_fragment
) {

    companion object {
        const val TAG = "DropSelectionFragment2"
        const val INTENT_SELECTIONS_TO_DROP = "selections_to_drop"

        fun launch(
            selectionIdsToDrop: ArrayList<String>,
            childFragmentManager : FragmentManager
        ){
            DropSelectionBottomSheetDialogFragment().apply {
                arguments = bundleOf(
                    INTENT_SELECTIONS_TO_DROP to selectionIdsToDrop
                )
            }.show(childFragmentManager,TAG)
        }
    }


    private lateinit var selectionIdsToDrop: ArrayList<String>
    private val viewModel: DropSelectionFragment2ViewModel by viewModels()
    private val sharedLeadMgmtViewModel: LeadManagementSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            selectionIdsToDrop = it.getStringArrayList(DropSelectionBottomSheetDialogFragment.INTENT_SELECTIONS_TO_DROP) ?: return@let
        }

        savedInstanceState?.let {
            selectionIdsToDrop = it.getStringArrayList(DropSelectionBottomSheetDialogFragment.INTENT_SELECTIONS_TO_DROP) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            DropSelectionBottomSheetDialogFragment.INTENT_SELECTIONS_TO_DROP,
            selectionIdsToDrop
        )
    }

    override fun viewCreated(
        viewBinding: DropSelectionFragment2FragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initView()
        initViewModel()
    }

}