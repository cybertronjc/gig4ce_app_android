package com.gigforce.lead_management.ui.new_selection_form_2

import android.content.Context
import android.util.Log
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicScreenField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.repository.AuthRepository
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.ValidationHelper
import com.gigforce.lead_management.R
import com.gigforce.lead_management.viewModels.JoiningSubmissionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class NewSelectionForm2ViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val leadManagementRepository: LeadManagementRepository,
    private val profileFirebaseRepository: ProfileFirebaseRepository,
    private val authRepository: AuthRepository,
    private val logger: GigforceLogger
) : JoiningSubmissionViewModel(
    leadManagementRepository,
    logger
) {

    companion object {
        private const val TAG = "NewSelectionForm1ViewModel"
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

    //State Observables
    private val _viewState = MutableLiveData<NewSelectionForm2ViewState?>()
    val viewState: LiveData<NewSelectionForm2ViewState?> = _viewState

    //Data
    private var selectedDateOfJoining: LocalDate = LocalDate.now()
    private var selectedCity: ReportingLocationsItem? = null
    private var selectedOtherCities: List<OtherCityClusterItem>? = null
    private var selectedCluster: OtherCityClusterItem? = null
    private var salaryAmountEntered: InputSalaryResponse? = null
    private var selectedReportingLocation: ReportingLocationsItem? = null
    private var selectedTL: BusinessTeamLeadersItem? = null
    private var secondaryPhoneNumber : String? = null

    //Data from previous screen
    private lateinit var joiningLocationsAndTLs: JoiningLocationTeamLeadersShifts
    private lateinit var joiningRequest: SubmitJoiningRequest
    private var currentlySubmittingAnyJoiningRequuest = false
    private var dataFromDynamicFieldsFromPreviousPages : List<DataFromDynamicInputField> = emptyList()
    private var verificationDynamicFields  : List<DynamicVerificationField> = emptyList()

    fun handleEvent(
        event: NewSelectionForm2Events
    ) {
        when (event) {
            is NewSelectionForm2Events.DateOfJoiningSelected -> {
                selectedDateOfJoining = event.date
            }
            NewSelectionForm2Events.SelectCityClicked -> openSelectCityScreen()
            NewSelectionForm2Events.SelectOtherCityClicked -> openSelectOtherCityScreen()
            NewSelectionForm2Events.SelectClusterClicked -> openSelectClusterScreen()
            NewSelectionForm2Events.InputSalaryComponentsClicked -> openInputSalaryScreen(joiningRequest.business.id.toString(), salaryAmountEntered)
            NewSelectionForm2Events.SelectReportingLocationClicked -> openSelectReportingLocationsScreen()
            NewSelectionForm2Events.SelectClientTLClicked -> openSelectBusinessTLScreen()
            is NewSelectionForm2Events.ShiftSelected -> {
            }
            is NewSelectionForm2Events.CitySelected -> {
                selectedCity = event.city
                selectedReportingLocation = null
            }
            is NewSelectionForm2Events.OtherCitySelected -> {
                selectedOtherCities = event.otherCities
            }
            is NewSelectionForm2Events.ClusterSelected -> {
                selectedCluster = event.cluster
            }
            is NewSelectionForm2Events.SalaryAmountEntered -> {
                Log.d("InputSalary1", "${event.salaryData}")
                salaryAmountEntered = event.salaryData
            }
            is NewSelectionForm2Events.ClientTLSelected -> {
                selectedTL = event.teamLeader
            }
            is NewSelectionForm2Events.ReportingLocationSelected -> {
                selectedCity = event.citySelected
                selectedReportingLocation = event.reportingLocation
            }
            is NewSelectionForm2Events.SubmitButtonPressed -> {
                validateDataAndSubmit(
                    event.dataFromDynamicFields,
                    event.dataFromDynamicScreenFields
                )
            }
            is NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen -> {
                joiningRequest = event.submitJoiningRequest
                dataFromDynamicFieldsFromPreviousPages = event.submitJoiningRequest.dataFromDynamicFields
                joiningRequest.dataFromDynamicFields = emptyList()
                verificationDynamicFields = event.verificationRelatedDynamicInputsFields.toMutableList()

                fetchJoiningForm2Data(
                    joiningRequest.business.id!!,
                    joiningRequest.jobProfile.id!!,
                )
            }
            is NewSelectionForm2Events.SecondaryPhoneNumberChanged -> secondaryMobileNumberChanged(event.secondaryPhoneNumber)
        }
    }

    private fun secondaryMobileNumberChanged(
        mobileNumber: String
    ) = viewModelScope.launch {

        if( secondaryPhoneNumber == mobileNumber){
            return@launch
        }
        secondaryPhoneNumber = mobileNumber

        if(mobileNumber.length == 3) { //+91

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = null
            )
            _viewState.value = null
            return@launch
        }

        val doesMobileNoContainsMoreThan10digits = mobileNumber.length > 13
        if (doesMobileNoContainsMoreThan10digits) {
            logger.d(TAG,"alternate Mobile no received : '${mobileNumber}', checking if can be fixed..")
            tryToFixPhoneNumberEntered(mobileNumber)
            return@launch
        }

        val mobileNumber10Dig = mobileNumber.substring(3)
        val mobileNumberValid = ValidationHelper.isValidIndianMobileNo(mobileNumber10Dig)

        if(!mobileNumberValid) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.number_you_entered_is_invalid_number_lead)
                    )
                }
            )
            _viewState.value = null
        } else{

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = null
            )
            _viewState.value = null
        }
    }

    private fun tryToFixPhoneNumberEntered(
        mobileNo: String
    ) {
        if (mobileNo.length <= 13) {
            //Not fixing mobile number that have less than 11 (+91 + phone number)Digits
            return
        }

        //replacing 91 , starting 0s and spaces from text with empty string
        val sanitizedNumber = mobileNo.substring(3).replace(
            "(\\\\s|^0+|^91)".toRegex(), ""
        )

        logger.d(TAG,"phone number sanitized : $sanitizedNumber")

        if (ValidationHelper.isValidIndianMobileNo(sanitizedNumber)) {
            _viewState.value = NewSelectionForm2ViewState.EnteredPhoneNumberSanitized(sanitizedNumber)
        }
    }

    private fun openSelectCityScreen() {
        val cities = joiningLocationsAndTLs.reportingLocations.filter {
            it.type == "city"
        }

        cities.onEach {
            it.selected = it.id == selectedCity?.id
            it.reportingLocations = getReportingLocations(it.id)
        }

        _viewState.value = NewSelectionForm2ViewState.OpenSelectCityScreen(
            cities = cities.sortedBy {
                it.name
            },
            joiningRequest.jobProfile.locationType
        )
        _viewState.value = null
    }

    private fun getReportingLocations(
        cityId: String?
    ): List<ReportingLocationsItem> {
        val cityIdNonNull = cityId ?: return emptyList()

        val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
            it.cityId == cityIdNonNull && it.type == "office"
        }

        reportingLocations.onEach {
            it.selected = it.id == cityIdNonNull
        }

        return reportingLocations
    }


    private fun openSelectReportingLocationsScreen() {
        if (selectedCity == null) {
            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.select_city_to_select_reporting_location_lead)
                    )
                }
            )
            _viewState.value = null
        } else {
            val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
                it.cityId == selectedCity?.cityId && it.type == "office"
            }

            reportingLocations.onEach {
                it.selected = it.id == selectedReportingLocation?.id
            }

            _viewState.value = NewSelectionForm2ViewState.OpenSelectReportingScreen(
                selectedCity!!,
                reportingLocations.sortedBy {
                    it.name
                }
            )
            _viewState.value = null
        }
    }

    private fun openSelectOtherCityScreen() {
        if (selectedCity == null) {
            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.select_city_to_select_reporting_location_lead)
                    )
                }
            )
            _viewState.value = null
        } else {
            val otherCities = joiningLocationsAndTLs.otherCities

            otherCities?.forEach { it1 ->
                it1.selected = selectedOtherCities?.find { it.id == it1.id }?.selected == true
            }
            _viewState.value = otherCities?.sortedBy {
                it.name
            }?.let {
                NewSelectionForm2ViewState.OpenSelectOtherCityScreen(
                    it,
                    ""
                )
            }
            _viewState.value = null
        }
    }

    private fun openSelectClusterScreen() {
        if (selectedCity == null) {
            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.select_city_to_select_reporting_location_lead)
                    )
                }
            )
            _viewState.value = null
        } else {
            val clusters = joiningLocationsAndTLs.reportingLocations.find { it.cityId == selectedCity?.cityId }?.clusters

            clusters?.forEach {
                it.selected = it.id == selectedCluster?.id
            }

            _viewState.value = clusters?.sortedBy {
                it.name
            }?.let {
                NewSelectionForm2ViewState.OpenSelectClusterScreen(
                    it,
                    ""
                )
            }
            _viewState.value = null
        }
    }

    private fun openInputSalaryScreen(
        businessId: String,
        salaryResponse: InputSalaryResponse?
    ) {
        _viewState.value =
            NewSelectionForm2ViewState.OpenInputSalaryScreen(
                businessId,
                salaryResponse
            )
        _viewState.value = null
    }

    private fun openSelectBusinessTLScreen() {

        joiningLocationsAndTLs.businessTeamLeaders.onEach {
            it.selected = it.id == selectedTL?.id
        }
        _viewState.value = NewSelectionForm2ViewState.OpenSelectClientTlScreen(
            joiningLocationsAndTLs.businessTeamLeaders.sortedBy {
                it.name
            }
        )
        _viewState.value = null
    }

    fun fetchJoiningForm2Data(
        businessId: String,
        jobProfileId: String
    ) = viewModelScope.launch {

        if (::joiningLocationsAndTLs.isInitialized) {

            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value = NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                selectedCity?.name,
                selectedReportingLocation?.name,
                joiningLocationsAndTLs,
                joiningRequest.jobProfile.locationType,
                verificationDynamicFields.isNotEmpty()
            )
            return@launch
        }

        _viewState.value = NewSelectionForm2ViewState.LoadingLocationAndTLData
        logger.d(
            TAG,
            "fetching location and tls..."
        )

        try {
            val locationAndTlsData = leadManagementRepository.getBusinessLocationsAndTeamLeaders(
                businessId = businessId,
                jobProfileId = jobProfileId
            )
            joiningLocationsAndTLs = locationAndTlsData
            if (locationAndTlsData.shiftTiming.isEmpty()) {
                _viewState.value = NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData(
                    error = "Cannot make joining, Please contact your manager",
                    shouldShowErrorButton = false
                )
                return@launch
            }

            logger.d(
                TAG,
                "location and tl data received from server"
            )
            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value =
                NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                    selectedCity?.name,
                    selectedReportingLocation?.name,
                    locationAndTlsData,
                    joiningRequest.jobProfile.locationType,
                    verificationDynamicFields.isNotEmpty()
                )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "while loading joining form2 data",
                e
            )

            _viewState.value = NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData(
                error = e.message ?: "Unable to load locations and tl data",
                shouldShowErrorButton = false
            )
        }
    }

    private fun checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne() {
        if (selectedCity == null) {

            val cities = joiningLocationsAndTLs.reportingLocations.filter {
                it.type == "city"
            }
            if (cities.size == 1) {
                cities.onEach {
                    it.selected = it.id == selectedCity?.id
                    it.reportingLocations = getReportingLocations(it.id)
                }
                selectedCity = cities.first()

                if (selectedReportingLocation == null) {
                    val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
                        it.cityId == selectedCity?.cityId && it.type == "office"
                    }

                    if (reportingLocations.size == 1) {
                        reportingLocations.onEach {
                            it.selected = it.id == selectedReportingLocation?.id
                        }
                        selectedReportingLocation = reportingLocations.first()
                    }
                }
            }
        }


    }

    private fun validateDataAndSubmit(
        dataFromDynamicFields: MutableList<DataFromDynamicInputField>,
        dataFromDynamicScreenFields: MutableList<DataFromDynamicScreenField>
    ) {

        if (selectedCity == null) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.please_select_city_lead)
                    )
                }
            )
            return
        }
        joiningRequest.city = selectedCity!!

        if (selectedCity!!.reportingLocations.isNotEmpty() && selectedReportingLocation == null && joiningRequest.jobProfile.locationType == "On Site") {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                reportingLocationError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.please_select_reporting_location_lead)
                    )
                }
            )
            return
        }
        if (joiningRequest.jobProfile.locationType == "On Site"){
            joiningRequest.reportingLocation = selectedReportingLocation
        }else{
            joiningRequest.reportingLocation = null
        }

        if(!secondaryPhoneNumber.isNullOrBlank() && secondaryPhoneNumber!!.length > 3) { //+91

            val mobileNumber10Dig = secondaryPhoneNumber!!.substring(3)
            val mobileNumberValid = ValidationHelper.isValidIndianMobileNo(mobileNumber10Dig)

            if(!mobileNumberValid) {

                _viewState.value = NewSelectionForm2ViewState.ValidationError(
                    secondaryPhoneNumberError = buildSpannedString {
                        bold {
                            append(appContext.getString(R.string.note_with_colon_lead))
                        }
                        append(
                            appContext.getString(R.string.number_you_entered_is_invalid_number_lead)
                        )
                    }
                )
                _viewState.value = null
                return
            }

            joiningRequest.secondaryMobileNumber = secondaryPhoneNumber
        } else{
            joiningRequest.secondaryMobileNumber = null
        }

        joiningRequest.assignGigsFrom = dateFormatter.format(selectedDateOfJoining)

        val selectedShift = joiningLocationsAndTLs.shiftTiming.firstOrNull()
        if (selectedShift != null)
            joiningRequest.shifts = listOf(selectedShift)

        //Cleaning up Final JSON
        joiningRequest.business.jobProfiles = emptyList()
        joiningRequest.jobProfile.dynamicFields = emptyList()

        joiningRequest.dataFromDynamicFields = dataFromDynamicFieldsFromPreviousPages + dataFromDynamicFields
        joiningRequest.dataFromDynamicScreenFields = dataFromDynamicScreenFields

        if(verificationDynamicFields.isEmpty()){
            submitJoiningData(joiningRequest)
        } else{
            addUserToAuthAndProfileIfNotExist(joiningRequest)
        }
    }

    private fun addUserToAuthAndProfileIfNotExist(
        joiningRequest: SubmitJoiningRequest
    ) = viewModelScope.launch {

        _viewState.value = NewSelectionForm2ViewState.SubmittingJoiningData

        try {
            val createOrGetUserResult = authRepository.getOrCreateUserInAuthAndProfile(
                joiningRequest.gigerMobileNo,
                joiningRequest.gigerName
            )

            verificationDynamicFields.onEach {

                it.jobProfileId = joiningRequest.jobProfile.id!!
                it.userId = createOrGetUserResult.uId!!
            }

            _viewState.value = NewSelectionForm2ViewState.NavigateToJoiningVerificationForm(
                joiningRequest = joiningRequest,
                userId = createOrGetUserResult.uId!!,
                verificationDynamicFields = verificationDynamicFields
            )
            _viewState.value = null
        } catch (e: Exception) {

            _viewState.value = NewSelectionForm2ViewState.ErrorWhileSubmittingJoiningData(
                error = e.message ?: "Unable to submit joining request, please try again later",
                shouldShowErrorButton = false
            )
            _viewState.value = null
        }
    }


    private fun submitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) = viewModelScope.launch {

        currentlySubmittingAnyJoiningRequuest = true
        logger.d(
            TAG,
            "Assigning gigs....",
        )

        try {
            _viewState.value = NewSelectionForm2ViewState.SubmittingJoiningData

            val shareLink = cleanUpJoiningDataAndSubmitJoiningData(
                joiningRequest
            )

            _viewState.value = NewSelectionForm2ViewState.JoiningDataSubmitted(
                shareLink = shareLink,
                businessName = joiningRequest.business.name.toString(),
                jobProfileName = joiningRequest.jobProfile.name.toString()
            )
            _viewState.value = null

            currentlySubmittingAnyJoiningRequuest = false
        } catch (e: Exception) {
            currentlySubmittingAnyJoiningRequuest = false

            _viewState.value = NewSelectionForm2ViewState.ErrorWhileSubmittingJoiningData(
                error = e.message ?: "Unable to submit joining request, please try again later",
                shouldShowErrorButton = false
            )
            _viewState.value = null
        }

    }
}