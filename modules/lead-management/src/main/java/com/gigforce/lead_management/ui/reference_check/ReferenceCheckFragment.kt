package com.gigforce.lead_management.ui.reference_check

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.core.base.BaseFragment2
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ReferenceCheckFragmentBinding
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferenceCheckFragment : BaseFragment2<ReferenceCheckFragmentBinding>(
    fragmentName = "ReferenceCheckFragment",
    layoutId = R.layout.reference_check_fragment,
    statusBarColor = R.color.status_bar_pink
) {
    //Data
    private lateinit var userUid: String
    private val viewModel: ReferenceCheckViewModel by viewModels()

    override fun viewCreated(
        viewBinding: ReferenceCheckFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFrom(
            arguments,
            savedInstanceState
        )
        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
        }

        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {
        if (::userUid.isInitialized) {
            logger.d(logTag, "User-id received from bundles : $userUid")
        } else {
            logger.e(
                logTag,
                "no User-id received from bundles",
                Exception("no User-id received from bundles")
            )
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_UID, userUid)
    }

    private fun initToolbar(
        viewBinding: ReferenceCheckFragmentBinding
    ) = viewBinding.toolbar.apply {

        showTitle("Reference Check")
        hideActionMenu()
        setBackButtonListener {
            activity?.onBackPressed()
        }
    }

    private fun initListeners(
        viewBinding: ReferenceCheckFragmentBinding
    ) = viewBinding.apply {

        bindProgressButton(viewBinding.submitButton)
        viewBinding.submitButton.attachTextChangeAnimator()
        submitButton.setOnClickListener {
            submitReferenceData()
        }
    }

    private fun submitReferenceData() {

        viewModel.saveReference(
            userUid = userUid,
            name = viewBinding.nameET.text.toString(),
            relation = viewBinding.relationET.text.toString(),
            contactNo = viewBinding.contactNoET.text.toString()
        )
    }

    private fun initViewModel() {
        viewModel.viewState
            .observe(viewLifecycleOwner, {
                val state = it ?: return@observe
                when (state) {
                    ReferenceCheckViewState.SubmittingReferenceData -> {
                        showReferenceSubmittingState()
                    }
                    is ReferenceCheckViewState.SubmittingReferenceDataError -> showErrorInSubmittingReferenceData(
                        state.error,
                        state.shouldShowErrorButton
                    )
                    ReferenceCheckViewState.SubmittingReferenceDataSuccess -> referenceDataSubmitted()
                    is ReferenceCheckViewState.ValidationError -> errorInValidation(
                        state.nameValidationError,
                        state.relationValidationError,
                        state.contactValidationError
                    )
                }
            })
    }

    private fun referenceDataSubmitted() = viewBinding.apply {
        submitButton.hideProgress("Submitted")
    }

    private fun showReferenceSubmittingState() = viewBinding.apply {
        submitButton.showProgress {
            this.buttonText = "Submitting..."
            this.progressColor = R.color.white
        }
    }

    private fun showErrorInSubmittingReferenceData(
        error: String,
        shouldShowErrorButton: Boolean
    ) = viewBinding.apply {
        submitButton.hideProgress("Submit")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to submit")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun errorInValidation(
        nameValidationError: String?,
        relationValidationError: String?,
        contactValidationError: String?
    ) {
        if (nameValidationError != null) {

        } else {

        }

        if (relationValidationError != null) {

        } else {

        }

        if (contactValidationError != null) {

        } else {

        }
    }

}