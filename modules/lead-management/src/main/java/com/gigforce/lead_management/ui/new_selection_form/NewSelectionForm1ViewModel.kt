package com.gigforce.lead_management.ui.new_selection_form

import android.content.Context
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
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
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.DataFromDynamicInputField
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.lead_management.R
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2Fragment
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class NewSelectionForm1ViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val leadManagementRepository: LeadManagementRepository,
    private val gigforceLogger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
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
                gigerName = event.name.capitalize(Locale.getDefault())
            }
            is NewSelectionForm1Events.BusinessSelected -> {
                selectedBusiness = event.business
                selectedJobProfile = null
            }
            is NewSelectionForm1Events.JobProfileSelected -> {
                selectedJobProfile = event.jobProfile
                inflateDynamicFieldsRelatedToSelectedJobProfile(event.jobProfile)
            }
            NewSelectionForm1Events.OpenSelectBusinessScreenSelected -> openSelectBusinessScreen()
            NewSelectionForm1Events.OpenSelectJobProfileScreenSelected -> openJobProfilesScreen()
            is NewSelectionForm1Events.SubmitButtonPressed -> validateDataAndNavigateToForm2(
                event.dataFromDynamicFields
            )
        }

       // checkForDataAndEnabledOrDisableSubmitButton()
    }

    private fun inflateDynamicFieldsRelatedToSelectedJobProfile(jobProfile: JobProfilesItem) = viewModelScope.launch{

        val selectedJobProfileDependentDynamicFields = jobProfile.dynamicInputFields.filter {
            it.screenIdToShowIn == NewSelectionForm1Fragment.SCREEN_ID
        }

        _viewState.value = NewSelectionForm1ViewState.ShowJobProfileRelatedField(
            selectedJobProfileDependentDynamicFields
        )
        delay(1000)
        _viewState.value = null
    }

    private fun checkForDataAndEnabledOrDisableSubmitButton() {
        if(gigerName.isNullOrBlank()){
            _viewState.value = NewSelectionForm1ViewState.DisableSubmitButton
            return
        }

        if (mobilePhoneNumber.isNullOrBlank() || !ValidationHelper.isValidIndianMobileNo(
                mobilePhoneNumber!!.substring(3)
            )
        ) {
            _viewState.value = NewSelectionForm1ViewState.DisableSubmitButton
            return
        }

        if(mobilePhoneNumber.isNullOrBlank()){
            _viewState.value = NewSelectionForm1ViewState.DisableSubmitButton
            return
        }
    }

    private fun openJobProfilesScreen() {
        if (selectedBusiness == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                businessError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.please_select_business_first)
                    )
                }
            )
            _viewState.value = null
        } else {

            selectedBusiness!!.jobProfiles.onEach {
                it.selected = it.id == selectedJobProfile?.id
            }

            _viewState.value = NewSelectionForm1ViewState.OpenSelectedJobProfileScreen(
                selectedBusiness!!.jobProfiles.sortedBy {
                    it.name
                }
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
            joiningBusinessAndJobProfiles.sortedBy {
                it.name
            }
        )
        _viewState.value = null
    }

    private fun validateDataAndNavigateToForm2(
        dataFromDynamicFields: MutableList<DataFromDynamicInputField>
    ) {

        if (mobilePhoneNumber.isNullOrBlank() || !ValidationHelper.isValidIndianMobileNo(
                mobilePhoneNumber!!.substring(3)
            )
        ) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                invalidMobileNoMessage = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.number_you_entered_is_invalid_number)
                    )
                }
            )
            return
        }

        if (gigerName.isNullOrBlank()) {

            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                gigerNameError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.please_enter_name)
                    )
                }
            )
            return
        }


        if (selectedBusiness == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                businessError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.please_select_business)
                    )
                }
            )
            return
        }

        if (selectedJobProfile == null) {
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                jobProfilesError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.please_select_job_profile)
                    )
                }
            )
            return
        }

        val dynamicFieldsForNextForm = selectedJobProfile!!.dynamicInputFields.filter {
            it.screenIdToShowIn == NewSelectionForm2Fragment.SCREEN_ID
        }

        _viewState.value = NewSelectionForm1ViewState.NavigateToForm2(
            submitJoiningRequest = SubmitJoiningRequest(
                business = selectedBusiness!!,
                jobProfile = selectedJobProfile!!,
                gigerClientId = clientId,
                gigerName = gigerName!!,
                gigerMobileNo = mobilePhoneNumber!!,
                dataFromDynamicFields = dataFromDynamicFields
            ),
            dynamicInputsFields = dynamicFieldsForNextForm
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
                invalidMobileNoMessage = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.number_you_entered_is_invalid_number)
                    )
                }
            )
            return@launch
        }

        if(mobileNo == firebaseAuthStateListener.getCurrentSignInInfo()?.phoneNumber){
            _viewState.value = NewSelectionForm1ViewState.ValidationError(
                invalidMobileNoMessage = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon))
                    }
                    append(
                        appContext.getString(R.string.you_cannot_user_your_own_number)
                    )
                }
            )
            return@launch
        }

        _viewState.value = NewSelectionForm1ViewState.CheckingForUserDetailsFromProfiles

        try {
            gigforceLogger.d(TAG, "checking phone-number in profile '$mobileNo'")

            val userInfo = leadManagementRepository.getUserInfoFromMobileNumber(mobileNo.substring(3))

            if(userInfo.name == null){
                gigforceLogger.d(TAG, "null received in name for mobile no '$mobileNo'")
                _viewState.value = NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile(
                    error = appContext.getString(R.string.no_match_found_for_this_no),
                    shouldShowErrorButton = false
                )
            } else {
                gigforceLogger.d(TAG, "User profile found for '$mobileNo'")

                gigerName = userInfo.name
                _viewState.value = NewSelectionForm1ViewState.UserDetailsFromProfiles(
                    profile = ProfileData(name = userInfo.name!!)
                )
            }
        } catch (e: Exception) {
            gigforceLogger.d(TAG, "Error in checking User profile for '$mobileNo'", e)

            _viewState.value = NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile(
                error = appContext.getString(R.string.unable_to_check_user),
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
                " ${joiningBusinessAndJobProfiles.size} business received from server"
            )

            _viewState.value = NewSelectionForm1ViewState.JobProfilesAndBusinessLoadSuccess
        } catch (e: Exception) {
            gigforceLogger.e(
                TAG,
                "while loading joining form1 data",
                e
            )

            _viewState.value = NewSelectionForm1ViewState.ErrorWhileLoadingBusinessAndJobProfiles(
                error = e.message ?: appContext.getString(R.string.unable_to_load_business_and_job_profiles),
                shouldShowErrorButton = false
            )
        }
    }
}