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
        const val INTENT_SUCCESSFUL_REFERRAL_TEXT = "successful_referral_text"
        const val INTENT_ERROR_REFERRAL_TEXT = "error_referral_text"

        fun launchSuccess(
            fragmentManager: FragmentManager,
            link : String,
            successReferralText : String
        ) {

            val dialog = ReferralLinkSharedResultDialogFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_RESULT to true,
                    INTENT_EXTRA_LINK to link,
                    INTENT_SUCCESSFUL_REFERRAL_TEXT to successReferralText
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
            link : String,
            errorReferralText : String
        ) {

            val dialog = ReferralLinkSharedResultDialogFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_RESULT to false,
                    INTENT_EXTRA_LINK to link,
                    INTENT_ERROR_REFERRAL_TEXT to errorReferralText
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
    private var successfulReferralText : String = ""
    private var errorReferralText : String = ""

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
            successfulReferralText = it.getString(INTENT_SUCCESSFUL_REFERRAL_TEXT) ?: ""
            errorReferralText = it.getString(INTENT_ERROR_REFERRAL_TEXT) ?: ""
            link = it.getString(INTENT_EXTRA_LINK) ?: return@let
        }

        savedInstanceState?.let {
            result = it.getBoolean(INTENT_EXTRA_RESULT)
            successfulReferralText = it.getString(INTENT_SUCCESSFUL_REFERRAL_TEXT) ?: ""
            errorReferralText = it.getString(INTENT_ERROR_REFERRAL_TEXT) ?: ""
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
        outState.putString(
            INTENT_SUCCESSFUL_REFERRAL_TEXT,
            successfulReferralText
        )
        outState.putString(
            INTENT_ERROR_REFERRAL_TEXT,
            errorReferralText
        )
    }

    private fun initView(
        viewBinding: FragmentReferralResultDialogBinding
    ) = viewBinding.apply {

        this.successLayout.gigsAssignedLabel.text = if(successfulReferralText.isNotBlank())
            successfulReferralText
        else
            errorReferralText

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