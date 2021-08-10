package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.gigforce.core.base.BaseDialogFragment
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentReferralResultDialogBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralLinkSharedResultDialogFragment :
    BaseDialogFragment<FragmentReferralResultDialogBinding>(
        fragmentName = TAG,
        layoutId = R.layout.fragment_referral_result_dialog,
    ) {

    companion object {
        const val TAG = "ReferralLinkSharedSuccessDialogFragment"
        const val INTENT_EXTRA_RESULT = "result"
        const val INTENT_EXTRA_LINK = "link"

        fun launchSuccess(
            fragmentManager: FragmentManager,
            link : String
        ) {

            val dialog = ReferralLinkSharedResultDialogFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_RESULT to true,
                    INTENT_EXTRA_LINK to link
                )
            }

            try {
                dialog.show(fragmentManager, TAG)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun launchError(
            fragmentManager: FragmentManager,
            link : String
        ) {

            val dialog = ReferralLinkSharedResultDialogFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_RESULT to false,
                    INTENT_EXTRA_LINK to link
                )
            }

            try {
                dialog.show(fragmentManager, TAG)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val viewModel : LeadManagementSharedViewModel by activityViewModels()
    private var result: Boolean = false
    private lateinit var link : String

    override fun viewCreated(
        viewBinding: FragmentReferralResultDialogBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFromBundles(
            arguments,
            savedInstanceState
        )
        initView(viewBinding)
        viewBinding.successLayout.root.isVisible = result
        viewBinding.errorLayout.root.isVisible = !result
    }

    private fun getDataFromBundles(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            result = it.getBoolean(INTENT_EXTRA_RESULT)
            link = it.getString(INTENT_EXTRA_LINK) ?: return@let
        }

        savedInstanceState?.let {
            result = it.getBoolean(INTENT_EXTRA_RESULT)
            link = it.getString(INTENT_EXTRA_LINK) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            INTENT_EXTRA_RESULT,
            result
        )
        outState.putString(
            INTENT_EXTRA_LINK,
            link
        )
    }

    private fun initView(
        viewBinding: FragmentReferralResultDialogBinding
    ) = viewBinding.apply {
        this.successLayout.okayBtn.setOnClickListener {
            viewModel.referralDialogOkayClicked()
        }

        this.successLayout.didNotGotLinkBtn.setOnClickListener {
            viewModel.referralDialogSendLinkViaLocalWhatsappClicked(
                link
            )
        }

        this.errorLayout.okayBtn.setOnClickListener {
            viewModel.referralDialogOkayClicked()
        }

        this.errorLayout.didNotGotLinkBtn.setOnClickListener {
            viewModel.referralDialogSendLinkViaLocalWhatsappClicked(
                link
            )
        }
    }
}