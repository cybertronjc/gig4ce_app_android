package com.gigforce.lead_management.ui.new_selection_form_3_verification_documents

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.dynamic_fields.DynamicFieldsInflaterHelper
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.AppConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm3VerificationBinding
import com.gigforce.lead_management.models.WhatsappTemplateModel
import com.gigforce.lead_management.ui.new_selection_form_submittion_success.SelectionFormSubmitSuccessFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewSelectionVerificationDocumentsForm3Fragment :
    BaseFragment2<FragmentNewSelectionForm3VerificationBinding>(
        fragmentName = "NewSelectionForm3Fragment",
        layoutId = R.layout.fragment_new_selection_form3_verification,
        statusBarColor = R.color.lipstick_2
    ) {

    companion object {

        const val SCREEN_ID = "form_3"
        const val INTENT_EXTRA_JOINING_DATA = "joining_data"
        const val INTENT_EXTRA_USER_UID = "user_uid"
        const val INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS = "verification_dynamic_fields"
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var userinfo: UserInfoImp

    @Inject
    lateinit var dynamicFieldsInflaterHelper: DynamicFieldsInflaterHelper

    private var cameFromAttendace: Boolean = false

    private val viewModel: NewSelectionForm3VerificationDocumentViewModel by viewModels()


    //Data from previous screen
    private lateinit var joiningRequest: SubmitJoiningRequest
    private lateinit var userUid: String
    private lateinit var verificationRelatedDynamicInputsFields: ArrayList<DynamicVerificationField>

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
            verificationRelatedDynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS) ?: arrayListOf()
            userUid = it.getString(INTENT_EXTRA_USER_UID) ?: return@let
            cameFromAttendace = it.getBoolean(AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE, false) ?: return@let
        }

        savedInstanceState?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
            verificationRelatedDynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS) ?: arrayListOf()
            userUid = it.getString(INTENT_EXTRA_USER_UID) ?: return@let
            cameFromAttendace = it.getBoolean(AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE, false) ?: return@let
        }


        viewModel.handleEvent(
            NewSelectionForm3Events.RequiredVerificationDocumentListAcquiredFromPreviousPage(
                joiningRequest = joiningRequest,
                userUid = userUid,
                requiredVerificationDocument = verificationRelatedDynamicInputsFields.toMutableList()
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_USER_UID, userUid)
        outState.putParcelable(INTENT_EXTRA_JOINING_DATA, joiningRequest)
        outState.putParcelableArrayList(
            INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS,
            verificationRelatedDynamicInputsFields
        )
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm3VerificationBinding,
        savedInstanceState: Bundle?
    ) {
        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initViewModelForUiEffects()
    }

    private fun initListeners(
        viewBinding: FragmentNewSelectionForm3VerificationBinding
    ) = viewBinding.mainForm.apply {

        bindProgressButton(nextButton)
        nextButton.attachTextChangeAnimator()
        nextButton.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm3Events.SubmitButtonPressed)
        }
    }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm3VerificationBinding
    ) = viewBinding.toolbar.apply {

        this.setBackButtonListener {
            navigation.navigateUp()
        }
        setBackButtonDrawable(R.drawable.ic_chevron)
        makeBackgroundMoreRound()
    }

    private fun initViewModel() = viewModel
        .viewState
        .observe(viewLifecycleOwner, {
            val state = it ?: return@observe
            when (state) {
                //Loading initial data states
                NewSelectionForm3ViewState.CheckingVerificationDocumentsStatus -> loadingVerificationDocumentWithStatus()
                is NewSelectionForm3ViewState.ShowVerificationDocumentFields -> showMainForm(
                    state.requiredVerificationDocument
                )
                is NewSelectionForm3ViewState.ErrorWhileCheckingVerificationStatus -> showErrorInLoadingVerificationDocumentStatus(
                    state.error
                )

                NewSelectionForm3ViewState.SubmittingJoiningData -> submittingJoiningRequest()
                is NewSelectionForm3ViewState.JoiningDataSubmitted -> navigateToSubmitSuccessScreen(state.whatsappTemplate)
                is NewSelectionForm3ViewState.ErrorWhileSubmittingJoiningData -> errorWhileSubmittingJoiningRequest(
                    state.error
                )
            }
        })

    private fun initViewModelForUiEffects() = lifecycleScope.launchWhenCreated {

        viewModel.uiEffects.collect {

            when (it) {
                NewSelectionForm3UiEffects.DisableSubmitButton -> disableSubmitButton()
                NewSelectionForm3UiEffects.EnableSubmitButton -> enableSubmitButton()
            }
        }
    }

    private fun navigateToSubmitSuccessScreen(
        whatsAppIntentData: WhatsappTemplateModel
    ) {
        whatsAppIntentData.tlName = userinfo.getData().profileName
        whatsAppIntentData.tlMobileNumber =  userinfo.sharedPreAndCommonUtilInterface.getLoggedInMobileNumber()

        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_FORM_SUCCESS,
            bundleOf(SelectionFormSubmitSuccessFragment.INTENT_EXTRA_WHATSAPP_DATA to whatsAppIntentData, AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE to cameFromAttendace),
            getNavOptions()
        )
    }

    private fun errorWhileSubmittingJoiningRequest(
        error: String
    ) {
        viewBinding.mainForm.nextButton.hideProgress(getString(R.string.submit_lead))
        viewBinding.mainForm.nextButton.isEnabled = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.unable_to_submit_joining_request_lead))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_common_ui)) { _, _ -> }
            .show()
    }

    private fun submittingJoiningRequest() {
        viewBinding.mainForm.nextButton.showProgress {
            buttonText = getString(R.string.submitting_data_lead)
            progressColor = Color.WHITE
        }
        viewBinding.mainForm.nextButton.isEnabled = false
    }

    private fun disableSubmitButton() {
        viewBinding.mainForm.nextButton.isEnabled = false
    }

    private fun enableSubmitButton() {
        viewBinding.mainForm.nextButton.isEnabled = true
    }

    private fun loadingVerificationDocumentWithStatus() = viewBinding.apply {

        mainForm.root.gone()
        formMainInfoLayout.root.gone()
        dataLoadingShimmerContainer.visible()

        startShimmer(
            this.dataLoadingShimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showMainForm(
        requiredVerificationDocument: List<DynamicVerificationField>
    ) = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()
        showVerificationRelatedDynamicFields(requiredVerificationDocument)
    }

    private fun showErrorInLoadingVerificationDocumentStatus(
        error: String
    ) = viewBinding.apply {

        mainForm.root.gone()
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.visible()

        formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        formMainInfoLayout.infoMessageTv.text = error
    }

    private fun showVerificationRelatedDynamicFields(
        verificationRelatedDynamicInputsFields: List<DynamicVerificationField>
    ) = dynamicFieldsInflaterHelper.apply {

        inflateVerificationDynamicFields(
            requireContext(),
            viewBinding.mainForm.verificationRelatedDynamicFieldsContainer,
            verificationRelatedDynamicInputsFields
        )
    }


}