package com.gigforce.lead_management.ui.new_selection_form_2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.DatePicker
import android.widget.LinearLayout
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
import com.gigforce.common_ui.dynamic_fields.DynamicFieldsInflaterHelper
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm2Binding
import com.gigforce.lead_management.models.WhatsappTemplateModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1Events
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1Fragment
import com.gigforce.lead_management.ui.new_selection_form_submittion_success.SelectionFormSubmitSuccessFragment
import com.gigforce.lead_management.ui.select_city.SelectCityFragment
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
    }

    @Inject
    lateinit var navigation: INavigation
    @Inject
    lateinit var userinfo: UserInfoImp
    @Inject
    lateinit var dynamicFieldsInflaterHelper: DynamicFieldsInflaterHelper

    private val viewModel: NewSelectionForm2ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val dateFormatter =  SimpleDateFormat("dd/MMM/yy",Locale.getDefault())

    //Data from previous screen
    private lateinit var joiningRequest: SubmitJoiningRequest
    private lateinit var dynamicInputsFields : ArrayList<DynamicField>

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
        datePickerDialog.datePicker.minDate = cal.timeInMillis
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
            dynamicInputsFields = it.getParcelableArrayList(INTENT_EXTRA_DYNAMIC_FIELDS) ?: arrayListOf()
        }

        savedInstanceState?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
            dynamicInputsFields = it.getParcelableArrayList(INTENT_EXTRA_DYNAMIC_FIELDS) ?: arrayListOf()
        }

        viewModel.handleEvent(
            NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen(
                joiningRequest
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
            showJobProfileRelatedDynamicFields(dynamicInputsFields)
            setTextWatchers()
        }

        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun setTextWatchers() = viewBinding.mainForm.apply{

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

        viewBinding.mainForm.selectedDateLabel.text = dateFormatter.format(Date())

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

    private fun validateDataAndSubmitData() = viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer.apply{

        val dynamicFieldsData = dynamicFieldsInflaterHelper.validateDynamicFieldsReturnFieldValueIfValid(this) ?: return@apply
        viewModel.handleEvent(NewSelectionForm2Events.SubmitButtonPressed(dynamicFieldsData.toMutableList()))
    }

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
                    state.locationType
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

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.unable_to_submit_joining_request_lead))
                        .setMessage(state.error)
                        .setPositiveButton(getString(R.string.okay_common_ui)) { _, _ -> }
                        .show()
                }
                is NewSelectionForm2ViewState.JoiningDataSubmitted -> {
                    try {
                        val whatsAppIntentData = WhatsappTemplateModel(state.shareLink, state.businessName, userinfo.getData().profileName, state.jobProfileName, userinfo.sharedPreAndCommonUtilInterface.getLoggedInMobileNumber())
                        navigation.navigateTo(
                            LeadManagementNavDestinations.FRAGMENT_SELECT_FORM_SUCCESS,
                            bundleOf(
                                SelectionFormSubmitSuccessFragment.INTENT_EXTRA_WHATSAPP_DATA to whatsAppIntentData,

                                )
                        )
                    }catch (e: Exception){

                    }
                }
                NewSelectionForm2ViewState.SubmittingJoiningData -> {

                    viewBinding.mainForm.nextButton.showProgress {
                        buttonText = getString(R.string.submitting_data_lead)
                        progressColor = Color.WHITE
                    }
                    viewBinding.mainForm.nextButton.isEnabled = false
                }
                is NewSelectionForm2ViewState.EnteredPhoneNumberSanitized -> setMobileNoOnEditText(
                    state.sanitizedPhoneNumber
                )
            }
        })

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
            bundleOf(SelectCityFragment.INTENT_EXTRA_CITY_LIST to cities,
                SelectCityFragment.INTENT_ONSITE_OFFSITE to locationType
            ),
            getNavOptions()
        )
        hideSoftKeyboard()
    }

    private fun handleValidationError(
        errorState: NewSelectionForm2ViewState.ValidationError
    ) = viewBinding.mainForm.apply {

        if (errorState.assignGigsFromError != null) {

            viewBinding.mainForm.expectedDateOfJoiningError.root.visible()
            viewBinding.mainForm.expectedDateOfJoiningError.errorTextview.text = errorState.assignGigsFromError
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
            viewBinding.mainForm.reportingLocationError.errorTextview.text = errorState.reportingLocationError
        } else {
            viewBinding.mainForm.reportingLocationError.errorTextview.text = null
            viewBinding.mainForm.reportingLocationError.root.gone()
        }

        if (errorState.secondaryPhoneNumberError != null) {
            viewBinding.mainForm.contactNoError.root.visible()
            viewBinding.mainForm.contactNoError.errorTextview.text = errorState.secondaryPhoneNumberError
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
        locationType: String?
    ) = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()

        if(selectedCity != null) {
            mainForm.citySelectedLabel.text = selectedCity
            mainForm.citySelectedLabel.setTypeface(mainForm.citySelectedLabel.typeface,Typeface.BOLD)
        } else{
            mainForm.citySelectedLabel.text = getString(R.string.click_to_select_city_lead)
        }

        if (locationType == "On Site"){
            mainForm.reportingLocationLabelLayout.visible()
            mainForm.selectReportingLocationCardlayout.visible()
        }else{
            mainForm.reportingLocationLabelLayout.gone()
            mainForm.selectReportingLocationCardlayout.gone()
        }

        if(selectedReportingLocation != null) {
            mainForm.reportingLocationSelectedLabel.text = selectedReportingLocation
            mainForm.reportingLocationSelectedLabel.setTypeface(mainForm.reportingLocationSelectedLabel.typeface,Typeface.BOLD)
        } else{
            mainForm.reportingLocationSelectedLabel.text = getString(R.string.click_to_select_location_lead)
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
                    is LeadManagementSharedViewModelState.ClientTLSelected -> showSelectedTL(it.tlSelected)
                    is LeadManagementSharedViewModelState.ReportingLocationSelected -> showSelectedReportingLocation(
                        it.citySelected,
                        it.reportingLocation
                    )
                }
            })
    }

    private fun showSelectedCity(
        citySelected: ReportingLocationsItem
    ) = viewBinding.mainForm.apply {
        citySelectedLabel.text = citySelected.name
        citySelectedLabel.setTypeface(citySelectedLabel.typeface,Typeface.BOLD)

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

    private fun showSelectedTL(
        tlSelected: BusinessTeamLeadersItem
    ) = viewBinding.mainForm.apply {
        selectedClientTlLabel.text = tlSelected.name
        viewModel.handleEvent(NewSelectionForm2Events.ClientTLSelected(tlSelected))

        selectedClientTlLabel.setTypeface(selectedClientTlLabel.typeface,Typeface.BOLD)
    }

    private fun showJobProfileRelatedDynamicFields(
        dynamicFields: List<DynamicField>
    ) = dynamicFieldsInflaterHelper.inflateDynamicFields(
        requireContext(),
        viewBinding.mainForm.jobProfileDependentDynamicFieldsContainer,
        dynamicFields
    )

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

}