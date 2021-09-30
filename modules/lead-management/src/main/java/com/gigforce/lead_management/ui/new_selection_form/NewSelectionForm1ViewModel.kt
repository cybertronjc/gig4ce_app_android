package com.gigforce.lead_management.ui.new_selection_form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.SubmitJoiningRequest
import com.gigforce.core.ValidationHelper
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewSelectionForm1ViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val leadManagementRepository: LeadManagementRepository,
    private val gigforceLogger: GigforceLogger,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NewSelectionForm1ViewModel"
    }

    //State Observables
    private val _viewState = MutableLiveData<NewSelectionForm1ViewState?>()
    val viewState: LiveData<NewSelectionForm1ViewState?> = _viewState

    //Data
    private var mobilePhoneNumber: String? = null
    private var gigerName: String? = null
    private var clientId: String? = null
    private var selectedBusiness: JoiningBusinessAndJobProfilesItem? = null
    private var selectedJobProfile: JobProfilesItem? = null
    private lateinit var joiningBusinessAndJobProfiles: List<JoiningBusinessAndJobProfilesItem>

    init {
        fetchJoiningForm1Data()
    }

    fun handleEvent(
        event: NewSelectionForm1Events
    ) {
        when (event) {
            is NewSelectionForm1Events.ContactNoChanged -> checkIfChangedContactNoIsDifferentElseFetchProfileDetails(
                event.mobileNo
            )
            is NewSelectionForm1Events.GigerClientIdChanged -> {
                clientId = event.clientId
            }
            is NewSelectionForm1Events.GigerNameChanged -> {
                gigforceLogger.d(TAG, "Name changed : ${event.name}")
                gigerName = event.name
            }
            is NewSelectionForm1Events.BusinessSelected -> {
                selectedBusiness = event.business
                selectedJobProfile = null
            }
            is NewSelectionForm1Events.JobProfileSelected -> {
                selectedJobProfile = event.jobProfile
            }
            NewSelectionForm1Events.OpenSelectBusinessScreenSelected -> openSelectBusinessScreen()
            NewSelectionForm1Events.OpenSelectJobProfileScreenSelected -> openJobProfilesScreen()
            NewSelectionForm1Events.SubmitButtonPressed -> validateDataAndNavigateToForm2()
        }
    }

    private fun openJobProfilesScreen() {
        if (selectedBusiness == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                businessError = "Select Business first"
            )
            _viewState.value = null
        } else {

            selectedBusiness!!.jobProfiles.onEach {
                it.selected = it.id == selectedJobProfile?.id
            }

            _viewState.value = NewSelectionForm1ViewState.OpenSelectedJobProfileScreen(
                selectedBusiness!!.jobProfiles
            )
            _viewState.value = null
        }
    }

    private fun openSelectBusinessScreen() {

        if (selectedBusiness != null) {

            joiningBusinessAndJobProfiles.onEach {
                it.selected = it.id == selectedBusiness?.id
            }
        }

        _viewState.value = NewSelectionForm1ViewState.OpenSelectedBusinessScreen(
            joiningBusinessAndJobProfiles
        )
        _viewState.value = null
    }

    private fun validateDataAndNavigateToForm2() {

        if (mobilePhoneNumber.isNullOrBlank() || !ValidationHelper.isValidIndianMobileNo(
                mobilePhoneNumber!!.substring(3)
            )
        ) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                invalidMobileNoMessage = "Invalid mobile number"
            )
            return
        }

        if (gigerName.isNullOrBlank()) {

            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                gigerNameError = "Please provide name"
            )
            return
        }


        if (selectedBusiness == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                businessError = "Please select business"
            )
            return
        }

        if (selectedJobProfile == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                jobProfilesError = "Please select job profile"
            )
            return
        }

        _viewState.value = NewSelectionForm1ViewState.NavigateToForm2(
            submitJoiningRequest = SubmitJoiningRequest(
                business = selectedBusiness!!,
                jobProfile = selectedJobProfile!!,
                gigerClientId = clientId,
                gigerName = gigerName!!,
                gigerMobileNo = mobilePhoneNumber!!
            )
        )
        _viewState.value = null
    }

    private fun checkIfChangedContactNoIsDifferentElseFetchProfileDetails(
        mobileNo: String
    ) = viewModelScope.launch {
        gigforceLogger.d(TAG, "Mobile no changed : $mobileNo")

        mobilePhoneNumber = mobileNo
        val doesMobileNoContains10digits = mobileNo.length == 13
        if (!doesMobileNoContains10digits)
            return@launch

        if (!ValidationHelper.isValidIndianMobileNo(mobileNo.substring(3))) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                invalidMobileNoMessage = "Invalid mobile number"
            )

            return@launch
        }

        _viewState.value = NewSelectionForm1ViewState.CheckingForUserDetailsFromProfiles

        try {
            gigforceLogger.d(TAG, "checking phone-number in profile '$mobileNo'")

            val profileInfo = profileFirebaseRepository.getFirstProfileWithPhoneNumber(mobileNo)
            if (profileInfo == null) {
                gigforceLogger.d(TAG, "no profile matched in profile for '$mobileNo'")

                _viewState.value = NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile(
                    error = "No match found for this no",
                    shouldShowErrorButton = false
                )
            } else {
                gigforceLogger.d(TAG, "User profile found for '$mobileNo'")

                gigerName = profileInfo.name
                _viewState.value = NewSelectionForm1ViewState.UserDetailsFromProfiles(
                    profile = profileInfo
                )
            }
        } catch (e: Exception) {
            gigforceLogger.d(TAG, "Error in checking User profile for '$mobileNo'", e)

            _viewState.value = NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile(
                error = "Unable to check user",
                shouldShowErrorButton = false
            )
        }
    }

    fun fetchJoiningForm1Data() = viewModelScope.launch {
        _viewState.value = NewSelectionForm1ViewState.LoadingBusinessAndJobProfiles

        gigforceLogger.d(
            TAG,
            "fetching business and job profiles..."
        )

        try {
            val businessAndTeamLeaders = leadManagementRepository.getBusinessAndJobProfiles()
            joiningBusinessAndJobProfiles = businessAndTeamLeaders

            gigforceLogger.d(
                TAG,
                " ${joiningBusinessAndJobProfiles?.size} business received from server"
            )

            _viewState.value = NewSelectionForm1ViewState.JobProfilesAndBusinessLoadSuccess
        } catch (e: Exception) {
            gigforceLogger.e(
                TAG,
                "while loading joining form1 data",
                e
            )

            _viewState.value = NewSelectionForm1ViewState.ErrorWhileLoadingBusinessAndJobProfiles(
                error = e.message ?: "Unable to load business and job profiles",
                shouldShowErrorButton = false
            )
        }
    }
}