package com.gigforce.lead_management.ui.reference_check

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.core.ValidationHelper
import com.gigforce.core.base.BaseFragment2
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ReferenceCheckFragmentBinding
import com.gigforce.lead_management.ui.assign_gig_dialog.AssignGigsDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReferenceCheckFragment : BaseFragment2<ReferenceCheckFragmentBinding>(
    fragmentName = "ReferenceCheckFragment",
    layoutId = R.layout.reference_check_fragment,
    statusBarColor = R.color.status_bar_pink
) {
    //Data
    private lateinit var userUid: String
    private lateinit var assignGigRequest: AssignGigRequest
    private var currentGigerInfo: GigerProfileCardDVM? = null
    private var s = ""

    private val viewModel: ReferenceCheckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDataFrom(
            arguments,
            savedInstanceState
        )
    }

    override fun viewCreated(
        viewBinding: ReferenceCheckFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        initToolbar(viewBinding)
        initUi(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        fetchReferenceData()
    }


    private fun initUi(
        viewBinding: ReferenceCheckFragmentBinding
    ) = viewBinding.apply {

        if (currentGigerInfo != null) {
            viewBinding.gigerProfileCard.setProfileCard(currentGigerInfo!!)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewBinding.gigerProfileCard.setGigerProfileData(userUid)
            }
        }
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
            currentGigerInfo =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
                    ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
            currentGigerInfo =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
                    ?: return@let
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

        if (::assignGigRequest.isInitialized.not()) {
            logger.e(
                logTag,
                "null assignGigRequest received from bundles",
                Exception("null assignGigRequest received from bundles")
            )
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        outState.putParcelable(
            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL,
            assignGigRequest
        )
        outState.putParcelable(
            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO,
            currentGigerInfo
        )
    }

    private fun initToolbar(
        viewBinding: ReferenceCheckFragmentBinding
    ) = viewBinding.toolbar.apply {

        showTitle(context.getString(R.string.reference_check_lead))
        hideActionMenu()
        setBackButtonListener {
            activity?.onBackPressed()
        }
    }

    @OptIn(FlowPreview::class)
    private fun initListeners(
        viewBinding: ReferenceCheckFragmentBinding
    ) = viewBinding.apply {

//        bindProgressButton(viewBinding.submitButton)
//        viewBinding.submitButton.attachTextChangeAnimator()

        lifecycleScope.launchWhenCreated {
            viewBinding.contactNoET.getTextChangeAsStateFlow()
                .debounce(200)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    s  = it
                    viewBinding.callGigerBtn.isEnabled = ValidationHelper.isValidIndianMobileNo(it)
                    viewModel.handleEvent(ReferenceCheckEvent.ContactNoChanged(it))
                }
        }

        lifecycleScope.launchWhenCreated {

            viewBinding.nameET.getTextChangeAsStateFlow()
                .debounce(200)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(ReferenceCheckEvent.NameChanged(it))
                }
        }

        lifecycleScope.launchWhenCreated {

            viewBinding.relationET.getTextChangeAsStateFlow()
                .debounce(200)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(ReferenceCheckEvent.RelationChanged(it))
                }
        }


        viewBinding.callGigerBtn.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", viewBinding.contactNoET.text.toString(), null)
            )
            requireContext().startActivity(intent)
        }

        PushDownAnim
            .setPushDownAnimTo(submitButton)
            .setOnClickListener {
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
                    is ReferenceCheckViewState.ErrorWhileFetchingPreviousReferenceData ->{
                        viewBinding.refernceLoadingPb.gone()
                        viewBinding.mainScrollView.visible()
                    }
                    ReferenceCheckViewState.FetchingReferenceDataFromProfile -> {
                        viewBinding.mainScrollView.gone()
                        viewBinding.refernceLoadingPb.visible()
                    }
                    is ReferenceCheckViewState.PreviousReferenceDataFetched -> {
                        viewBinding.refernceLoadingPb.gone()
                        viewBinding.mainScrollView.visible()

                        viewBinding.relationET.setText( state.referenceData.relation)
                        viewBinding.contactNoET.setText( state.referenceData.contactNo)
                        viewBinding.nameET.setText( state.referenceData.name)
                    }
                }
            })
    }

    private fun fetchReferenceData() {
        viewModel.fetchPreviousReferenceCheckData(userUid)
    }

    private fun referenceDataSubmitted() = viewBinding.apply {
        // submitButton.hideProgress("Submitted")
        showToast(getString(R.string.reference_submitted_lead))
        assignGigRequest.userUid = userUid
        AssignGigsDialogFragment.launch(
            fragmentManager = childFragmentManager,
            gigRequest = assignGigRequest
        )
    }

    private fun showReferenceSubmittingState() = viewBinding.apply {
        showToast(getString(R.string.submitting_data_lead))
//        submitButton.showProgress {
//            this.buttonText = "Submitting..."
//            this.progressColor = R.color.white
//        }
    }

    private fun showErrorInSubmittingReferenceData(
        error: String,
        shouldShowErrorButton: Boolean
    ) = viewBinding.apply {
//        submitButton.hideProgress("Submit")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.unable_to_submit_lead))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
            .show()
    }

    private fun errorInValidation(
        nameValidationError: String?,
        relationValidationError: String?,
        contactValidationError: String?
    ) = viewBinding.apply {

        if (nameValidationError != null) {
            nameErrorTv.visible()
            nameErrorTv.text = nameValidationError
        } else {
            nameErrorTv.text = ""
            nameErrorTv.gone()
        }

        if (relationValidationError != null) {
            relationErrorTv.visible()
            relationErrorTv.text = relationValidationError
        } else {
            relationErrorTv.text = ""
            relationErrorTv.gone()
        }

        if (contactValidationError != null) {
            contactNoErrorTv.visible()
            contactNoErrorTv.text = contactValidationError
        } else {
            contactNoErrorTv.text = ""
            contactNoErrorTv.gone()
        }
    }
}