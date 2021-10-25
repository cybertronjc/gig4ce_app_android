package com.gigforce.lead_management.ui.drop_selection_2

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.BottomSheetDialogFragmentDropSelectionBinding
import com.gigforce.lead_management.databinding.DropSelection2BottomSheetMainBinding
import com.gigforce.lead_management.databinding.DropSelectionFragment2FragmentBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.drop_selection.DropSelectionViewModel
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
            DropSelectionFragment2().apply {
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
            selectionIdsToDrop = it.getStringArrayList(DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP) ?: return@let
        }

        savedInstanceState?.let {
            selectionIdsToDrop = it.getStringArrayList(DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            DropSelectionFragment2.INTENT_SELECTIONS_TO_DROP,
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

    private fun initView() {

    }

    private fun showMainLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.visible()
        this.lastWorkingLayout.root.gone()
    }

    private fun showLastWorkingLayout() = viewBinding.apply {
        this.successLayout.root.gone()
        this.errorLayout.root.gone()
        this.mainLayout.root.gone()
        this.lastWorkingLayout.root.visible()
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