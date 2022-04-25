package com.gigforce.lead_management.ui.new_selection_form_2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.contacts.ContactsDelegate
import com.gigforce.common_ui.contacts.PhoneContact
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.DynamicFieldsInflaterHelper
import com.gigforce.common_ui.dynamic_fields.DynamicScreenFieldView
import com.gigforce.common_ui.dynamic_fields.data.*
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.AppConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm2Binding
import com.gigforce.lead_management.models.WhatsappTemplateModel
import com.gigforce.lead_management.ui.DynamicFields.DynamicInputSalaryComponentView
import com.gigforce.lead_management.ui.DynamicFields.DynamicSelectClusterView
import com.gigforce.lead_management.ui.DynamicFields.DynamicSelectOtherCitiesView
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.input_salary_components.InputSalaryComponentsFragment
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1Fragment
import com.gigforce.lead_management.ui.new_selection_form_3_verification_documents.NewSelectionVerificationDocumentsForm3Fragment
import com.gigforce.lead_management.ui.new_selection_form_submittion_success.SelectionFormSubmitSuccessFragment
import com.gigforce.lead_management.ui.other_cities.SelectOtherCitiesFragment
import com.gigforce.lead_management.ui.select_city.SelectCityFragment
import com.gigforce.lead_management.ui.select_cluster.SelectClusterFragment
import com.gigforce.lead_management.ui.select_reporting_location.SelectReportingLocationFragment
import com.gigforce.lead_management.ui.select_tls.SelectClientTlFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NewSelectionForm2Fragment : BaseFragment2<FragmentNewSelectionForm2Binding>(
    fragmentName = "NewSelectionForm1Fragment",
    layoutId = R.layout.fragment_new_selection_form2,
    statusBarColor = R.color.lipstick_2
) {

    companion object {

        const val SCREEN_ID = "form_2"
        const val INTENT_EXTRA_JOINING_DATA = "joining_data"
        const val INTENT_EXTRA_DYNAMIC_FIELDS = "dynamic_fields"
        const val INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS = "verification_dynamic_fields"
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var userinfo: UserInfoImp

    @Inject
    lateinit var dynamicFieldsInflaterHelper: DynamicFieldsInflaterHelper

    private var cameFromAttendace: Boolean = false

    private val viewModel: NewSelectionForm2ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val sharedSignatureViewModel: com.gigforce.verification.mainverification.signature.SharedSignatureUploadViewModel by activityViewModels()

    private val dateFormatter = SimpleDateFormat("dd/MMM/yy", Locale.getDefault())

    //Data from previous screen
    private lateinit var joiningRequest: SubmitJoiningRequest
    private lateinit var dynamicInputsFields: ArrayList<DynamicField>
    private lateinit var verificationRelatedDynamicInputsFields: ArrayList<DynamicVerificationField>

    private val expectedStartDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewBinding.mainForm.selectedDateLabel.text = dateFormatter.format(newCal.time)
                viewModel.handleEvent(NewSelectionForm2Events.DateOfJoiningSelected(newCal.time.toLocalDate()))

                viewBinding.mainForm.expectedDateOfJoiningError.errorTextview.text = null
                viewBinding.mainForm.expectedDateOfJoiningError.root.gone()

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = cal.timeInMillis + (1000 * 60 * 60 * 24) //adding one day
        datePickerDialog
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
                logger.e(NewSelectionForm1Fragment.TAG, "while picking contact", exception)
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
        viewBinding.mainForm.alternateMobileNoEt.setText(number)

        viewBinding.mainForm.alternateMobileNoEt.post {
            viewBinding.mainForm.alternateMobileNoEt.setSelection(viewBinding.mainForm.alternateMobileNoEt.length())
        }
        viewModel.handleEvent(NewSelectionForm2Events.SecondaryPhoneNumberChanged("+91" + number))
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
            dynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_DYNAMIC_FIELDS) ?: arrayListOf()
            verificationRelatedDynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS) ?: arrayListOf()
            cameFromAttendace = it.getBoolean(AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE, false)
        }

        savedInstanceState?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
            dynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_DYNAMIC_FIELDS) ?: arrayListOf()
            verificationRelatedDynamicInputsFields =
                it.getParcelableArrayList(INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS) ?: arrayListOf()
            cameFromAttendace = it.getBoolean(AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE, false)
        }

        viewModel.handleEvent(
            NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen(
                joiningRequest,
                verificationRelatedDynamicInputsFields
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            INTENT_EXTRA_JOINING_DATA,
            joiningRequest
        )
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm2Binding,
        savedInstanceState: Bundle?
    ) {
        if (viewCreatedForTheFirstTime) {
            showJobProfileRelatedDynamicFields(
                dynamicInputsFields
            )
            setTextWatchers()
        }

        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun setTextWatchers() = viewBinding.mainForm.apply {

        lifecycleScope.launchWhenCreated {

            alternateMobileNoEt.getTextChangeAsStateFlow()
                .debounce(200)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(NewSelectionForm2Events.SecondaryPhoneNumberChanged("+91$it"))
                }
        }

    }

    private fun initListeners(
        viewBinding: FragmentNewSelectionForm2Binding
    ) = viewBinding.mainForm.apply {

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DATE, 1)
        viewBinding.mainForm.selectedDateLabel.text = dateFormatter.format(cal.time)
        viewModel.handleEvent(NewSelectionForm2Events.DateOfJoiningSelected(cal.time.toLocalDate()))

        selectCityLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SelectCityClicked)
        }

        selectReportingLocationLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SelectReportingLocationClicked)
        }

        selectClientTlLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SelectClientTLClicked)
        }


        changeDateBtn.setOnClickListener {
            expectedStartDatePicker.show()
        }

        bindProgressButton(nextButton)
        nextButton.attachTextChangeAnimator()
        nextButton.setOnClickListener {
            validateDataAndSubmitData()
        }

        pickContactsButton.setOnClickListener {

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
            val dynamicScreenFieldsData = validateDynamicScreenFieldsReturnFieldValueIfValid(viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer) ?: return@apply
            val dynamicFieldsData =
                dynamicFieldsInflaterHelper.validateDynamicFieldsReturnFieldValueIfValid(this)
                    ?: return@apply
            viewModel.handleEvent(NewSelectionForm2Events.SubmitButtonPressed(dynamicFieldsData.toMutableList(), dynamicScreenFieldsData.toMutableList()))
        }
//
//    private fun validateDynamicScreenData() =
//        viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer.apply {
//
//        }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm2Binding
    ) = viewBinding.toolbar.apply {

        this.setBackButtonListener {
            navigation.navigateUp()
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
                NewSelectionForm2ViewState.LoadingLocationAndTLData -> loadingBusinessAndJobProfiles()
                is NewSelectionForm2ViewState.LocationAndTlDataLoaded -> showMainForm(
                    state.shiftAndTls,
                    state.selectedCity,
                    state.selectedReportingLocation,
                    state.locationType,
                    state.doesUserHaveToUploadAnyVerificationDocuments
                )
                is NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData -> showErrorInLoadingBusinessAndJobProfiles(
                    state.error
                )

                //Validation error states
                is NewSelectionForm2ViewState.ValidationError -> handleValidationError(state)

                //Open data selection screen states
                is NewSelectionForm2ViewState.OpenSelectCityScreen -> openSelectCityScreen(
                    ArrayList(state.cities),
                    state.locationType.toString()
                )
                is NewSelectionForm2ViewState.OpenSelectOtherCityScreen -> openSelectOtherCityScreen(
                    ArrayList(state.otherCities),
                    state.locationType.toString()
                )
                is NewSelectionForm2ViewState.OpenSelectClusterScreen -> openSelectClusterScreen(
                    ArrayList(state.clusters),
                    state.locationType.toString()
                )
                is NewSelectionForm2ViewState.OpenInputSalaryScreen -> openInputSalaryScreen(
                    state.businessId,
                    state.salaryData
                )
                is NewSelectionForm2ViewState.OpenSelectReportingScreen -> openSelectReportingLocationScreen(
                    state.selectedCity,
                    ArrayList(state.reportingLocations)
                )
                is NewSelectionForm2ViewState.OpenSelectClientTlScreen -> openSelectBusinessTlScreen(
                    ArrayList(state.tls)
                )

                is NewSelectionForm2ViewState.ErrorWhileSubmittingJoiningData -> {
                    viewBinding.mainForm.nextButton.hideProgress(getString(R.string.submit_lead))
                    viewBinding.mainForm.nextButton.isEnabled = true
                    navigation.navigateTo(LeadManagementNavDestinations.BOTTOM_SHEET_JOINING_ERROR, bundleOf("message" to state.error))
//                    MaterialAlertDialogBuilder(requireContext())
//                        .setTitle(getString(R.string.unable_to_submit_joining_request_lead))
//                        .setMessage(state.error)
//                        .setPositiveButton(getString(R.string.okay_common_ui)) { _, _ -> }
//                        .show()
                }
                is NewSelectionForm2ViewState.JoiningDataSubmitted -> {
                    try {
                        val whatsAppIntentData = WhatsappTemplateModel(
                            state.shareLink,
                            state.businessName,
                            userinfo.getData().profileName,
                            state.jobProfileName,
                            userinfo.sharedPreAndCommonUtilInterface.getLoggedInMobileNumber()
                        )
                        navigation.navigateTo(
                            LeadManagementNavDestinations.FRAGMENT_SELECT_FORM_SUCCESS,
                            bundleOf(
                                SelectionFormSubmitSuccessFragment.INTENT_EXTRA_WHATSAPP_DATA to whatsAppIntentData,
                                AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE to cameFromAttendace
                                )
                        )
                    } catch (e: Exception) {

                    }
                }
                NewSelectionForm2ViewState.SubmittingJoiningData -> {

                    viewBinding.mainForm.nextButton.showProgress {
                        buttonText = "Processing..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.mainForm.nextButton.isEnabled = false
                }
                is NewSelectionForm2ViewState.EnteredPhoneNumberSanitized -> setMobileNoOnEditText(
                    state.sanitizedPhoneNumber
                )
                is NewSelectionForm2ViewState.NavigateToJoiningVerificationForm -> openDocumentVerificationPage(
                    state.joiningRequest,
                    state.userId,
                    state.verificationDynamicFields
                )
            }
        })

    private fun openDocumentVerificationPage(
        joiningRequest: SubmitJoiningRequest,
        userId: String,
        verificationDynamicFields: List<DynamicVerificationField>
    ) {
        viewBinding.mainForm.nextButton.hideProgress(getString(R.string.next_lead))
        viewBinding.mainForm.nextButton.isEnabled = true

        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_3,
            bundleOf(
                NewSelectionVerificationDocumentsForm3Fragment.INTENT_EXTRA_JOINING_DATA to joiningRequest,
                NewSelectionVerificationDocumentsForm3Fragment.INTENT_EXTRA_USER_UID to userId,
                NewSelectionVerificationDocumentsForm3Fragment.INTENT_EXTRA_VERIFICATION_DYNAMIC_FIELDS to verificationDynamicFields,
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE to cameFromAttendace
            ),
            getNavOptions()
        )
    }

    private fun openSelectReportingLocationScreen(
        selectedCity: ReportingLocationsItem,
        reportingLocations: ArrayList<ReportingLocationsItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_REPORTING_LOCATION,
            bundleOf(
                SelectReportingLocationFragment.INTENT_EXTRA_REPORTING_LOCATIONS to reportingLocations,
                SelectReportingLocationFragment.INTENT_EXTRA_SELECTED_CITY to selectedCity
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openSelectBusinessTlScreen(
        businessTls: ArrayList<BusinessTeamLeadersItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_CLIENT_TL,
            bundleOf(SelectClientTlFragment.INTENT_EXTRA_CLIENT_TLS to businessTls),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openSelectCityScreen(
        cities: ArrayList<ReportingLocationsItem>,
        locationType: String
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_CITY,
            bundleOf(
                SelectCityFragment.INTENT_EXTRA_CITY_LIST to cities,
                SelectCityFragment.INTENT_ONSITE_OFFSITE to locationType
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openSelectOtherCityScreen(
        otherCities: ArrayList<OtherCityClusterItem>,
        locationType: String
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_OTHER_CITY,
            bundleOf(
                SelectOtherCitiesFragment.INTENT_EXTRA_SELECTED_OTHER_CITIES to otherCities
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openSelectClusterScreen(
        otherCities: ArrayList<OtherCityClusterItem>,
        locationType: String
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_CLUSTER,
            bundleOf(
                SelectClusterFragment.INTENT_EXTRA_CLUSTER to otherCities
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun openInputSalaryScreen(
        businessId: String,
        salaryData: InputSalaryResponse?
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_INPUT_SALARY,
            bundleOf(
                InputSalaryComponentsFragment.INTENT_EXTRA_BUSINESS_ID to businessId,
                InputSalaryComponentsFragment.INTENT_EXTRA_SALARY_DATA to salaryData
            )
        )
        hideSoftKeyboard()
    }

    private fun handleValidationError(
        errorState: NewSelectionForm2ViewState.ValidationError
    ) = viewBinding.mainForm.apply {

        if (errorState.assignGigsFromError != null) {

            viewBinding.mainForm.expectedDateOfJoiningError.root.visible()
            viewBinding.mainForm.expectedDateOfJoiningError.errorTextview.text =
                errorState.assignGigsFromError
        } else {
            viewBinding.mainForm.expectedDateOfJoiningError.errorTextview.text = null
            viewBinding.mainForm.expectedDateOfJoiningError.root.gone()
        }

        if (errorState.cityError != null) {

            viewBinding.mainForm.cityError.root.visible()
            viewBinding.mainForm.cityError.errorTextview.text = errorState.cityError
        } else {
            viewBinding.mainForm.cityError.errorTextview.text = null
            viewBinding.mainForm.cityError.root.gone()
        }

        if (errorState.reportingLocationError != null) {
            viewBinding.mainForm.reportingLocationError.root.visible()
            viewBinding.mainForm.reportingLocationError.errorTextview.text =
                errorState.reportingLocationError
        } else {
            viewBinding.mainForm.reportingLocationError.errorTextview.text = null
            viewBinding.mainForm.reportingLocationError.root.gone()
        }

        if (errorState.secondaryPhoneNumberError != null) {
            viewBinding.mainForm.contactNoError.root.visible()
            viewBinding.mainForm.contactNoError.errorTextview.text =
                errorState.secondaryPhoneNumberError
        } else {
            viewBinding.mainForm.contactNoError.errorTextview.text = null
            viewBinding.mainForm.contactNoError.root.gone()
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
        shiftAndTls: JoiningLocationTeamLeadersShifts,
        selectedCity: String?,
        selectedReportingLocation: String?,
        locationType: String?,
        doesUserHaveToUploadAnyVerificationDocuments: Boolean
    ) = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()

        if (selectedCity != null) {
            mainForm.citySelectedLabel.text = selectedCity
            mainForm.citySelectedLabel.setTypeface(
                mainForm.citySelectedLabel.typeface,
                Typeface.BOLD
            )
        } else {
            mainForm.citySelectedLabel.text = getString(R.string.click_to_select_city_lead)
        }

        if (locationType == "On Site") {
            mainForm.reportingLocationLabelLayout.visible()
            mainForm.selectReportingLocationCardlayout.visible()
        } else {
            mainForm.reportingLocationLabelLayout.gone()
            mainForm.selectReportingLocationCardlayout.gone()
        }

        if (selectedReportingLocation != null) {
            mainForm.reportingLocationSelectedLabel.text = selectedReportingLocation
            mainForm.reportingLocationSelectedLabel.setTypeface(
                mainForm.reportingLocationSelectedLabel.typeface,
                Typeface.BOLD
            )
        } else {
            mainForm.reportingLocationSelectedLabel.text =
                getString(R.string.click_to_select_location_lead)
        }

        viewBinding.mainForm.nextButton.text = if (doesUserHaveToUploadAnyVerificationDocuments) {
            getString(R.string.next_camel_case_common_ui)
        } else {
            getString(R.string.submit_lead)
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

    private fun initSharedViewModel() {
        leadMgmtSharedViewModel
            .viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    is LeadManagementSharedViewModelState.CitySelected -> showSelectedCity(it.city)
                    is LeadManagementSharedViewModelState.ReportingLocationSelected -> showSelectedReportingLocation(
                        it.citySelected,
                        it.reportingLocation
                    )
                    is LeadManagementSharedViewModelState.OtherCitySelected -> showSelectedOtherCities(
                        it.otherCity
                    )
                    is LeadManagementSharedViewModelState.ClusterSelected -> showSelectedCluster(
                        it.cluster
                    )
                    is LeadManagementSharedViewModelState.SalaryAmountEntered -> showSalaryAmountEntered(
                        it.salaryData
                    )
                }
            })
    }

    private fun showSalaryAmountEntered(salaryData: InputSalaryResponse) {
        //get other cities layout from container
        val view = viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer.findViewById<View>(2)
        if (view != null){
            val dynamicView = view as DynamicScreenFieldView
            salaryData.data?.let { dynamicView.setData(it) }
            val salaryLabelTextView = view.findViewById<TextView>(R.id.salary_amount_entered_label)

            viewModel.handleEvent(NewSelectionForm2Events.SalaryAmountEntered(salaryData))

            if (salaryLabelTextView != null){
                salaryLabelTextView.setText("Salary Components Filled")
                salaryLabelTextView.setTypeface(salaryLabelTextView.typeface, Typeface.BOLD)
            }
        }
    }

    private fun showSelectedCluster(cluster: OtherCityClusterItem) {
        //get other cities layout from container
        val view = viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer.findViewById<View>(1)
        if (view != null){
            val dynamicView = view as DynamicScreenFieldView
            dynamicView.setData(cluster)
            val clusterLabelTextView = view.findViewById<TextView>(R.id.cluster_selected_label)

            viewModel.handleEvent(NewSelectionForm2Events.ClusterSelected(cluster))

            if (clusterLabelTextView != null){
                clusterLabelTextView.setText(cluster.name)
                clusterLabelTextView.setTypeface(clusterLabelTextView.typeface, Typeface.BOLD)
            }
        }
    }

    private fun showSelectedOtherCities(otherCity: List<OtherCityClusterItem>) {
        //make string from other cities
        var otherCities = ""
        otherCity.forEachIndexed { index, otherCityClusterItem ->
            if (index != otherCity.size - 1){
                otherCities = otherCities +  otherCityClusterItem.name + ", "
            } else {
                otherCities = otherCities +  otherCityClusterItem.name
            }

        }
        //get other cities layout from container
        val view = viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer.findViewById<View>(0)
        if (view != null){
            val dynamicView = view as DynamicScreenFieldView
            dynamicView.setData(otherCity)
            val otherCityLabelTextView = view.findViewById<TextView>(R.id.other_city_selected_label)

            viewModel.handleEvent(NewSelectionForm2Events.OtherCitySelected(otherCity))

            if (otherCityLabelTextView != null){
                otherCityLabelTextView.setText(otherCities)
                otherCityLabelTextView.setTypeface(otherCityLabelTextView.typeface, Typeface.BOLD)
            }
        }
    }


    private fun showSelectedCity(
        citySelected: ReportingLocationsItem
    ) = viewBinding.mainForm.apply {
        citySelectedLabel.text = citySelected.name
        citySelectedLabel.setTypeface(citySelectedLabel.typeface, Typeface.BOLD)

        viewModel.handleEvent(NewSelectionForm2Events.CitySelected(citySelected))

        reportingLocationSelectedLabel.text = getString(R.string.click_to_select_location_lead)
        reportingLocationSelectedLabel.typeface = Typeface.DEFAULT

        viewBinding.mainForm.cityError.errorTextview.text = null
        viewBinding.mainForm.cityError.root.gone()
    }

    private fun showSelectedReportingLocation(
        citySelected: ReportingLocationsItem,
        reportingLocationSelected: ReportingLocationsItem
    ) = viewBinding.mainForm.apply {

        reportingLocationSelectedLabel.text = reportingLocationSelected.name
        reportingLocationSelectedLabel.typeface = Typeface.DEFAULT_BOLD

        citySelectedLabel.text = citySelected.name
        citySelectedLabel.typeface = Typeface.DEFAULT_BOLD


        viewModel.handleEvent(
            NewSelectionForm2Events.ReportingLocationSelected(
                citySelected,
                reportingLocationSelected
            )
        )

        viewBinding.mainForm.reportingLocationError.errorTextview.text = null
        viewBinding.mainForm.reportingLocationError.root.gone()
    }


    private fun showJobProfileRelatedDynamicFields(
        dynamicFields: List<DynamicField>
    ) = dynamicFieldsInflaterHelper.apply {

        inflateScreenDynamicFields(
            requireContext(),
            viewBinding.mainForm.jobProfileScreenDynamicFieldsContainer,
            dynamicFields
        )

        inflateDynamicFields(
            requireContext(),
            viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer,
            dynamicFields
        )
    }

    private fun readContactsPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun inflateScreenDynamicFields(
        context: Context,
        containerLayout: LinearLayout,
        fields: List<DynamicField>
    ) = fields.apply {
        containerLayout.removeAllViews()

        fields.forEach {

            compareFieldTypeAndInflateRequiredLayout(
                context,
                containerLayout,
                it
            )
        }
    }

    private fun compareFieldTypeAndInflateRequiredLayout(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        when (it.fieldType) {
            FieldTypes.OTHER_CITIES -> inflateSelectOtherCityView(
                context,
                containerLayout,
                it
            )
            FieldTypes.SELECT_CLUSTER -> inflateSelectClusterView(
                context,
                containerLayout,
                it
            )
            FieldTypes.INPUT_SALARY -> inflateInputSalaryView(
                context,
                containerLayout,
                it
            )
            else -> {
                logger.d(
                    DynamicFieldsInflaterHelper.TAG,
                    "skipping inflating ${it.id},${it.title} as it lacks fieldtype ${it.fieldType} doesnt match any present in app"
                )
            }
        }
    }

    private fun inflateSelectClusterView(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicSelectClusterView(context, null)
        view.id = 1
        containerLayout.addView(view)
        view.setOnClickListener {
            viewModel.handleEvent(
                NewSelectionForm2Events.SelectClusterClicked
            )
        }
        view.bind(it)
    }

    private fun inflateInputSalaryView(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicInputSalaryComponentView(
            context,
            null
        )
        view.id = 2
        containerLayout.addView(view)
        view.setOnClickListener {
            viewModel.handleEvent(
                NewSelectionForm2Events.InputSalaryComponentsClicked
            )
        }
        view.bind(it)
    }

    private fun inflateSelectOtherCityView(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicSelectOtherCitiesView(
            context,
            null
        )
        view.id = 0
        containerLayout.addView(view)
        view.setOnClickListener {
            viewModel.handleEvent(
                NewSelectionForm2Events.SelectOtherCityClicked
            )
        }
        view.bind(it)
    }

    private fun validateDynamicScreenFieldsReturnFieldValueIfValid(
        container: LinearLayout
    ) : List<DataFromDynamicScreenField>? {
        val dynamicScreenFieldsData = mutableListOf<DataFromDynamicScreenField>()
        for (i in 0 until container.childCount) {

            val dynamicFieldView = container.getChildAt(i) as DynamicScreenFieldView
            val dataFromField = dynamicFieldView.validateDataAndReturnDataElseNull() ?: return null
            dynamicScreenFieldsData.add(dataFromField)
        }

        return dynamicScreenFieldsData
    }

}