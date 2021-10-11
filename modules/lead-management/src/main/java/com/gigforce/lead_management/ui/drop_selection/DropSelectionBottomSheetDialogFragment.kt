package com.gigforce.lead_management.ui.drop_selection

import android.graphics.Color
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DropSelectionBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<BottomSheetDialogFragmentDropSelectionBinding>(
        fragmentName = "DropSelectionBottomSheetDialogFragment",
        layoutId = R.layout.bottom_sheet_dialog_fragment_drop_selection
    ) {

    companion object {
        const val TAG = "DropSelectionBottomSheetDialogFragment"
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
    private val viewModel: DropSelectionViewModel by viewModels()
    private val sharedLeadMgmtViewModel: LeadManagementSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            selectionIdsToDrop = it.getStringArrayList(INTENT_SELECTIONS_TO_DROP) ?: return@let
        }

        savedInstanceState?.let {
            selectionIdsToDrop = it.getStringArrayList(INTENT_SELECTIONS_TO_DROP) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            INTENT_SELECTIONS_TO_DROP,
            selectionIdsToDrop
        )
    }

    override fun viewCreated(
        viewBinding: BottomSheetDialogFragmentDropSelectionBinding,
        savedInstanceState: Bundle?
    ) {

        initView()
        initViewModel()
    }

    private fun initView() = viewBinding.apply {

        bindProgressButton(this.successLayout.okayButton)
        this.successLayout.okayButton.attachTextChangeAnimator()

        this.mainLayout.dropSelectionLabel.text = getString(R.string.are_sure_to_drop_lead) + " ${selectionIdsToDrop.size} " + getString(R.string.selections_lead)

        this.errorLayout.retryBtn.setOnClickListener {
            showMainLayout()
        }

        this.mainLayout.dropSelectionButton.setOnClickListener {
            viewModel.dropSelections(
                selectionIdsToDrop
            )
        }

        this.mainLayout.cancelButton.setOnClickListener {
            dismiss()
        }

        this.successLayout.okayButton.setOnClickListener {
            setFragmentResult("drop_status", bundleOf("drop_status" to "dropped"))
            dismiss()
        }
    }

    private fun showMainLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.visible()

        this.mainLayout.dropSelectionLabel.text = if(selectionIdsToDrop.size == 1){
            getString(R.string.are_you_sure_drop_lead)
        } else{
            getString(R.string.are_sure_to_drop_lead) + " ${selectionIdsToDrop.size} " + getString(R.string.selections_lead)
        }
    }

    private fun initViewModel() = viewModel
        .submitDropSelectionState
        .observe(viewLifecycleOwner, {

            when (it) {
                is Lce.Error -> {
                    viewBinding.mainLayout.dropSelectionButton.hideProgress("Drop Selection")
                    viewBinding.mainLayout.dropSelectionButton.isEnabled = false

                    viewBinding.successLayout.root.gone()
                    viewBinding.mainLayout.root.gone()
                    viewBinding.errorLayout.root.visible()

                    viewBinding.errorLayout.infoMessageTv.text = it.error
                    viewBinding.errorLayout.retryBtn.visible()
                }
                Lce.Loading -> {
                    viewBinding.mainLayout.dropSelectionButton.showProgress {
                        buttonText = "Dropping..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.mainLayout.dropSelectionButton.isEnabled = false


                }
                is Lce.Content -> {
                    viewBinding.successLayout.root.visible()
                    viewBinding.mainLayout.root.gone()
                    viewBinding.errorLayout.root.gone()
                   if (selectionIdsToDrop.size == 1){
                       viewBinding.successLayout.dropedSelectionLabel.text = getString(R.string.one_selection_drop_lead)
                    } else {
                       viewBinding.successLayout.dropedSelectionLabel.text =
                           selectionIdsToDrop.size.toString() + getString(R.string.selection_drop_lead)
                   }
                    //dismiss()
                }
            }
        })
}