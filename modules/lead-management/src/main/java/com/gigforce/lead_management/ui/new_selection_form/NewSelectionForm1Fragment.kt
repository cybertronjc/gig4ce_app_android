package com.gigforce.lead_management.ui.new_selection_form

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.contacts.ContactsDelegate
import com.gigforce.common_ui.contacts.PhoneContact
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.dynamic_fields.DynamicFieldsInflaterHelper
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.signature.SharedSignatureUploadViewModel
import com.gigforce.common_ui.signature.SharedSignatureUploadViewModelViewState
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm1Binding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2Fragment
import com.gigforce.lead_management.ui.select_business_screen.SelectBusinessFragment
import com.gigforce.lead_management.ui.select_job_profile_screen.SelectJobProfileFragment
import com.gigforce.lead_management.ui.select_team_leader.SelectTeamLeaderFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class NewSelectionForm1Fragment : BaseFragment2<FragmentNewSelectionForm1Binding>(
    fragmentName = "NewSelectionForm1Fragment",
    layoutId = R.layout.fragment_new_selection_form1,
    statusBarColor = R.color.lipstick_2
) {

    companion object {

        const val SCREEN_ID = "form_1"
        const val TAG = "NewSelectionForm1Fragment"
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var dynamicFieldsInflaterHelper: DynamicFieldsInflaterHelper

    private val viewModel: NewSelectionForm1ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val sharedSignatureViewModel: SharedSignatureUploadViewModel by activityViewModels()

    private val contactsDelegate: ContactsDelegate by lazy {
        ContactsDelegate(requireContext().contentResolver)
    }

    @SuppressLint("NewApi")
    private val pickContactContract = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) {
        if (it == null) return@registerForActivityResult

        contactsDelegate.parseResults(
            uri = it,
            onSuccess = { contacts ->
                showContactNoOnMobileNo(contacts)
            }, onFailure = { exception ->
                logger.e(TAG, "while picking contact", exception)
                showToast(getString(R.string.unable_to_pick_contact_lead))
            })
    }

    private fun showContactNoOnMobileNo(contacts: List<PhoneContact>) {
        if (contacts.isEmpty()) return

        val pickedContact = contacts.first()
        if (pickedContact.phoneNumbers.isEmpty()) return

        if (pickedContact.phoneNumbers.size > 1) {
            //show choose no dialog

            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle("Select Phone Number")
            builder.setItems(pickedContact.phoneNumbers.toTypedArray()) { _, item ->
                val number = pickedContact.phoneNumbers[item]
                setMobileNoOnEditText(number)
            }
            builder.show()
        } else {
            setMobileNoOnEditText(pickedContact.phoneNumbers.first())
        }
    }

    private fun setMobileNoOnEditText(number: String) {
        viewBinding.mainForm.mobileNoEt.setText(number)

        viewBinding.mainForm.mobileNoEt.post {
            viewBinding.mainForm.mobileNoEt.setSelection(viewBinding.mainForm.mobileNoEt.length())
        }
        viewModel.handleEvent(NewSelectionForm1Events.ContactNoChanged("+91" + number))
    }

    @SuppressLint("NewApi")
    private val requestContactPermissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { contactPermissionGranted ->

        if (contactPermissionGranted) {
            pickContactContract.launch(null)
        } else {
            val hasUserOptedForDoNotAskAgain =
                requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                    .not()
            if (hasUserOptedForDoNotAskAgain) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.read_contact_permission_required_lead))
                    .setMessage(getString(R.string.please_grant_read_permissions_to_lead))
                    .setPositiveButton(getString(R.string.okay_common_ui)) { _, _ -> openSettingsPage() }
                    .setNegativeButton(getString(R.string.cancel_common_ui)) { _, _ -> }
                    .show()
            }
        }
    }

    private fun openSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm1Binding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            attachTextWatcher()
        }

        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
        initSharedSingatureViewModel()
    }

    private fun requestFocusOnMobileNoEditText() = viewBinding.mainForm.apply {

        mobileNoEt.requestFocus()
        val imm: InputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun attachTextWatcher() = viewBinding.apply {
        lifecycleScope.launchWhenCreated {

            mainForm.mobileNoEt.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(NewSelectionForm1Events.ContactNoChanged("+91$it"))
                }
        }

        lifecycleScope.launchWhenCreated {

            mainForm.gigerNameEt.getTextChangeAsStateFlow()
                .collect {
                    viewModel.handleEvent(NewSelectionForm1Events.GigerNameChanged(it))
                }
        }

    }

    private fun initListeners(
        viewBinding: FragmentNewSelectionForm1Binding
    ) = viewBinding.apply {

        mainForm.selectBusinessLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.OpenSelectBusinessScreenSelected)
        }

        mainForm.selectJobProfileLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.OpenSelectJobProfileScreenSelected)
        }

        mainForm.selectReportingTlLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.OpenSelectReportingTLScreenSelected)
        }

        mainForm.nextButton.setOnClickListener {

            validateDataAndSubmitData()
        }

        mainForm.pickContactsButton.setOnClickListener {

            if (readContactsPermissionsGranted()) {
                pickContactContract.launch(null)
            } else {

                requestContactPermissionContract.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
        }
    }

    private fun validateDataAndSubmitData() =
        viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer.apply {

            val dynamicFieldsData =
                dynamicFieldsInflaterHelper.validateDynamicFieldsReturnFieldValueIfValid(this)
                    ?: return@apply
            viewModel.handleEvent(NewSelectionForm1Events.SubmitButtonPressed(
                dataFromDynamicFields = dynamicFieldsData.toMutableList()
            ))
        }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm1Binding
    ) = viewBinding.toolbar.apply {
        this.setBackButtonListener {
            activity?.onBackPressed()
        }
        setBackButtonDrawable(R.drawable.ic_chevron)
        makeBackgroundMoreRound()
        makeTitleBold()
    }

    private fun initViewModel() = viewModel
        .viewState
        .observe(viewLifecycleOwner, {
            val state = it ?: return@observe

            when (state) {
                //Loading initial data states
                NewSelectionForm1ViewState.LoadingBusinessAndJobProfiles -> loadingBusinessAndJobProfiles()
                is NewSelectionForm1ViewState.JobProfilesAndBusinessLoadSuccess -> showMainForm(
                    state.selectedTeamLeader
                )
                is NewSelectionForm1ViewState.ErrorWhileLoadingBusinessAndJobProfiles -> showErrorInLoadingBusinessAndJobProfiles(
                    state.error
                )

                //Validation error states
                is NewSelectionForm1ViewState.ValidationError -> handleValidationError(state)

                //Checking Mobile no in profile states
                NewSelectionForm1ViewState.CheckingForUserDetailsFromProfiles -> {
                    viewBinding.mainForm.gigerNameEt.isEnabled = false
                    viewBinding.mainForm.nameProgressbar.visible()
                    viewBinding.mainForm.gigerNameEt.setText("")

                    viewBinding.mainForm.contactNoError.errorTextview.text = null
                    viewBinding.mainForm.contactNoError.root.gone()
                }
                is NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile -> {
                    viewBinding.mainForm.gigerNameEt.isEnabled = true
                    viewBinding.mainForm.gigerNameEt.setText("")
                    viewBinding.mainForm.nameProgressbar.gone()
                }
                is NewSelectionForm1ViewState.UserDetailsFromProfiles -> {
                    viewBinding.mainForm.nameProgressbar.gone()

                    if (state.profile.name.isBlank()) {
                        viewBinding.mainForm.gigerNameEt.isEnabled = true
                        viewBinding.mainForm.gigerNameEt.setText("")
                    } else {
                        viewBinding.mainForm.gigerNameEt.isEnabled = false
                        viewBinding.mainForm.gigerNameEt.setText(state.profile.name)
                    }
                }

                //Open data selection screen states
                is NewSelectionForm1ViewState.OpenSelectedBusinessScreen -> openSelectBusinessScreen(
                    ArrayList(state.business)
                )
                is NewSelectionForm1ViewState.OpenSelectedJobProfileScreen -> openSelectJobProfileScreen(
                    state.selectedBusiness,
                    ArrayList(state.jobProfiles)
                )
                is NewSelectionForm1ViewState.OpenSelectTLScreen -> openSelectTLScreen(
                    state.selectedTLId,
                    state.shouldShowAllTls
                )

                //Data submit states
                is NewSelectionForm1ViewState.NavigateToForm2 -> openForm2(
                    state.submitJoiningRequest,
                    ArrayList(state.dynamicInputsFields),
                    ArrayList(state.verificationRelatedDynamicInputsFields)
                )
                is NewSelectionForm1ViewState.ShowJobProfileRelatedField -> showJobProfileRelatedFields(
                    state.dynamicFields
                )
                is NewSelectionForm1ViewState.EnteredPhoneNumberSanitized -> {
                    setMobileNoOnEditText(state.sanitizedPhoneNumber)
                }
                is NewSelectionForm1ViewState.DisableSubmitButton -> {
                    enableDisableSubmitButton(false)
                }
                is NewSelectionForm1ViewState.EnableSubmitButton -> {
                    enableDisableSubmitButton(true)
                }
            }
        })

    private fun openSelectTLScreen(
        selectedTLId : String?,
        shouldShowAllTls : Boolean
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_TEAM_LEADERS,
            bundleOf(
                SelectTeamLeaderFragment.INTENT_EXTRA_SELECTED_TL_ID to selectedTLId,
                SelectTeamLeaderFragment.INTENT_EXTRA_SHOW_ALL_TLS to shouldShowAllTls
            ),
            getNavOptions()
        )

        hideSoftKeyboard()
    }

    private fun enableDisableSubmitButton(b: Boolean) {
        viewBinding.mainForm.nextButton.isEnabled = b
    }

    private fun showJobProfileRelatedFields(
        dynamicFields: List<DynamicField>
    ) = dynamicFieldsInflaterHelper.apply {

        //Inflating
        inflateDynamicFields(
            requireContext(),
            viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer,
            dynamicFields,
            childFragmentManager
        )
    }

    private fun openForm2(
        submitJoiningRequest: SubmitJoiningRequest,
        dynamicInputsFieldValues: ArrayList<DynamicField>,
        verificationRelatedDynamicInputsFieldValues: ArrayList<DynamicVerificationField>
    ) {

        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2, bundleOf(
                NewSelectionForm2Fragment.INTENT_EXTRA_JOINING_DATA to submitJoiningRequest,
                NewSelectionForm2Fragment.INTENT_EXTRA_DYNAMIC_FIELDS to dynamicInputsFieldValues,
                NewSelectionForm2Fragment.INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS to verificationRelatedDynamicInputsFieldValues
            )
        )
        hideSoftKeyboard()
    }

    private fun openSelectJobProfileScreen(
        selectedBusiness: JoiningBusinessAndJobProfilesItem,
        jobProfiles: ArrayList<JobProfilesItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_JOB_PROFILE,
            bundleOf(
                SelectJobProfileFragment.INTENT_EXTRA_SELECTED_BUSINESS to selectedBusiness,
                SelectJobProfileFragment.INTENT_EXTRA_JOB_PROFILES to jobProfiles
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openSelectBusinessScreen(
        business: ArrayList<JoiningBusinessAndJobProfilesItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_BUSINESS,
            bundleOf(SelectBusinessFragment.INTENT_EXTRA_BUSINESS_LIST to business),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun handleValidationError(
        errorState: NewSelectionForm1ViewState.ValidationError
    ) = viewBinding.mainForm.apply {

        if (errorState.invalidMobileNoMessage != null) {

            viewBinding.mainForm.contactNoError.root.visible()
            viewBinding.mainForm.contactNoError.errorTextview.text =
                errorState.invalidMobileNoMessage
        } else {
            viewBinding.mainForm.contactNoError.errorTextview.text = null
            viewBinding.mainForm.contactNoError.root.gone()
        }

        if (errorState.gigerNameError != null) {
            viewBinding.mainForm.gigerNameError.root.visible()
            viewBinding.mainForm.gigerNameError.errorTextview.text = errorState.gigerNameError
        } else {
            viewBinding.mainForm.gigerNameError.errorTextview.text = null
            viewBinding.mainForm.gigerNameError.root.gone()
        }


        if (errorState.businessError != null) {

            viewBinding.mainForm.businessError.root.visible()
            viewBinding.mainForm.businessError.errorTextview.text = errorState.businessError
        } else {
            viewBinding.mainForm.businessError.errorTextview.text = null
            viewBinding.mainForm.businessError.root.gone()
        }

        if (errorState.jobProfilesError != null) {
            viewBinding.mainForm.jobProfileError.root.visible()
            viewBinding.mainForm.jobProfileError.errorTextview.text = errorState.jobProfilesError
        } else {
            viewBinding.mainForm.jobProfileError.errorTextview.text = null
            viewBinding.mainForm.jobProfileError.root.gone()
        }

        if(errorState.reportingTLError != null){
            viewBinding.mainForm.reportingTlError.root.visible()
            viewBinding.mainForm.reportingTlError.errorTextview.text = errorState.reportingTLError
        } else{
            viewBinding.mainForm.reportingTlError.errorTextview.text = null
            viewBinding.mainForm.reportingTlError.root.gone()
        }
    }

    private fun loadingBusinessAndJobProfiles() = viewBinding.apply {

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
        selectedTeamLeader: TeamLeader?
    ) = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()

        if (viewCreatedForTheFirstTime) {
            Handler(Looper.getMainLooper()).postDelayed({
                requestFocusOnMobileNoEditText()
            }, 300)
        }

        if(selectedTeamLeader != null) {
            showSelectedTeamLeader(
                selectedTeamLeader
            )
        }
    }



    private fun showErrorInLoadingBusinessAndJobProfiles(
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

    private fun initSharedViewModel() = lifecycleScope.launchWhenCreated {
        leadMgmtSharedViewModel
            .viewStateFlow
            .collect {

                when (it) {
                    is LeadManagementSharedViewModelState.BusinessSelected -> showSelectedBusiness(
                        it.businessSelected
                    )
                    is LeadManagementSharedViewModelState.JobProfileSelected -> {
                        showSelectedJobProfile(
                            it.businessSelected,
                            it.jobProfileSelected
                        )
                    }
                    is LeadManagementSharedViewModelState.ReportingTLSelected -> {
                        viewModel.handleEvent(NewSelectionForm1Events.ReportingTeamLeaderSelected(
                            it.tlSelected,
                            it.showingAllTLs
                        ))
                        showSelectedTeamLeader(it.tlSelected)
                    }
                }
            }
    }

    private fun initSharedSingatureViewModel() = lifecycleScope.launchWhenCreated {
        sharedSignatureViewModel
            .viewState
            .collect {

                when (it) {
                    is SharedSignatureUploadViewModelViewState.SignatureCaptured -> {
                        dynamicFieldsInflaterHelper.signatureCapturedUpdateStatus(
                            viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer,
                            it.pathOnFirebase,
                            it.imageFullUrl
                        )
                    }
                }
            }
    }


    private fun showSelectedBusiness(
        businessSelected: JoiningBusinessAndJobProfilesItem
    ) = viewBinding.mainForm.apply {

        businessSelectedLabel.text = businessSelected.name
        businessSelectedLabel.typeface = Typeface.DEFAULT_BOLD
        viewModel.handleEvent(NewSelectionForm1Events.BusinessSelected(businessSelected))

        //reseting job profile selected
        selectedJobProfileLabel.text = getString(R.string.click_to_select_job_profile_lead)
        selectedJobProfileLabel.typeface = Typeface.DEFAULT

        viewBinding.mainForm.businessError.errorTextview.text = null
        viewBinding.mainForm.businessError.root.gone()

    }

    private fun showSelectedJobProfile(
        businessSelected: JoiningBusinessAndJobProfilesItem,
        jobProfileSelected: JobProfilesItem
    ) = viewBinding.mainForm.apply {

        businessSelectedLabel.text = businessSelected.name
        businessSelectedLabel.typeface = Typeface.DEFAULT_BOLD
        viewModel.handleEvent(NewSelectionForm1Events.BusinessSelected(businessSelected))

        selectedJobProfileLabel.text = jobProfileSelected.name
        selectedJobProfileLabel.typeface = Typeface.DEFAULT_BOLD
        viewModel.handleEvent(NewSelectionForm1Events.JobProfileSelected(jobProfileSelected))

        viewBinding.mainForm.businessError.errorTextview.text = null
        viewBinding.mainForm.businessError.root.gone()

        viewBinding.mainForm.jobProfileError.errorTextview.text = null
        viewBinding.mainForm.jobProfileError.root.gone()
    }

    private fun readContactsPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showSelectedTeamLeader(
        selectedTeamLeader: TeamLeader
    )  = viewBinding.mainForm.apply{

        this.selectedReportingTlLabel.text = selectedTeamLeader.name
        this.selectedReportingTlLabel.typeface = Typeface.DEFAULT_BOLD
    }
}