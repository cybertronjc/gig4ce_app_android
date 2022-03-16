package com.gigforce.lead_management.ui.assign_gig_dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.core.base.BaseDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentAssignGigDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssignGigsDialogFragment : BaseDialogFragment<FragmentAssignGigDialogBinding>(
    fragmentName = "AssignGigsDialogFragment",
    layoutId = R.layout.fragment_assign_gig_dialog,
) {

    companion object {
        const val TAG = "AssignGigsDialogFragment"

        fun launch(
            fragmentManager: FragmentManager,
            gigRequest: AssignGigRequest
        ) {

            val dialog = AssignGigsDialogFragment().apply {
                arguments = bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to gigRequest
                )
            }

            try {
                dialog.show(fragmentManager, TAG)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: AssignGigsViewModel by viewModels()
    private lateinit var gigRequest: AssignGigRequest

    override fun viewCreated(
        viewBinding: FragmentAssignGigDialogBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFromBundles(
            arguments,
            savedInstanceState
        )
        initView(viewBinding)
        initViewModel()
        submitAssignGigRequest()
    }

    private fun getDataFromBundles(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            gigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
        }

        savedInstanceState?.let {
            gigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL,
            gigRequest
        )
    }

    private fun initView(
        viewBinding: FragmentAssignGigDialogBinding
    ) = viewBinding.apply {

        successLayout.okayBtn.setOnClickListener {

            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }

        errorLayout.retryBtn.setOnClickListener {
            submitAssignGigRequest()
        }
    }


    private fun initViewModel() {
        viewModel.viewState.observe(viewLifecycleOwner, {

            when (it) {
                Lse.Loading -> {
                    viewBinding.apply {
                        successLayout.root.gone()
                        errorLayout.root.gone()
                        processingLayout.root.visible()
                    }

                }
                Lse.Success -> {
                    showToast(getString(R.string.gig_assigned_lead))
                    viewBinding.apply {
                        processingLayout.root.gone()
                        errorLayout.root.gone()
                        successLayout.root.visible()
                    }
                }
                is Lse.Error -> {
                    viewBinding.apply {
                        processingLayout.root.gone()
                        successLayout.root.gone()
                        errorLayout.root.visible()


                        errorLayout.infoMessageTv.text = it.error
                        errorLayout.infoIv.loadImage(com.gigforce.common_ui.R.drawable.banner_error)
                        errorLayout.retryBtn.visible()
                        showToast(it.error)
                    }
                }
            }
        })
    }

    private fun submitAssignGigRequest() {
        viewModel.assignGigs(gigRequest)
    }
}