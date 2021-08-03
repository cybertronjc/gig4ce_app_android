package com.gigforce.lead_management.ui.giger_onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningSignUpInitiatedMode
import com.gigforce.core.datamodels.ambassador.CreateUserResponse
import com.gigforce.core.datamodels.ambassador.RegisterMobileNoResponse
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.datamodels.login.LoginResponse
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GigerOnboardingViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val buildConfig: IBuildConfigVM,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GigerOnboardingViewModel"
    }

    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()
    private val _checkMobileNo = MutableLiveData<Lce<RegisterMobileNoResponse>?>()
    val checkMobileNo: LiveData<Lce<RegisterMobileNoResponse>?> = _checkMobileNo

    private val _numberRegistered = MutableLiveData<Lce<UserAuthStatusModel>?>()
    val numberRegistered: LiveData<Lce<UserAuthStatusModel>?> = _numberRegistered

    var userAuthStatus: UserAuthStatusModel? = null

    init {
        FirebaseAuth.getInstance().currentUser.let {
        }
    }

    fun checkIfNumberAlreadyRegistered(
        mobileNo: String
    ) = viewModelScope.launch {
        _numberRegistered.postValue(Lce.loading())
        try {
            val response =
                leadManagementRepository.getUserAuthStatus(mobileNo)
            _numberRegistered.value = Lce.content(response)
            _numberRegistered.value = null

            logger.d(TAG, "Unable to check if number is already registered", response.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            logger.d(TAG, "Unable to check if number is already registered", e.toString())
            _numberRegistered.value =
                Lce.error(e.message ?: "Unable to check if number already registered")
            _numberRegistered.value = null
        }
    }

    fun sendOtp(
        mobileNo: String
    ) = viewModelScope.launch {

        _checkMobileNo.postValue(Lce.loading())
        try {
            val repsonse =
                leadManagementRepository.checkMobileForExistingRegistrationElseSendOtp(
                    mobileNo,
                    buildConfig.getCreateOrSendOTPUrl()
                )
            _checkMobileNo.value = Lce.content(repsonse)
            _checkMobileNo.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _checkMobileNo.value = Lce.error(e.message ?: "Unable to check mobile number")
            _checkMobileNo.value = null
        }
    }

    private val _verifyOtp = MutableLiveData<Lce<CreateUserResponse>>()
    val verifyOtp: LiveData<Lce<CreateUserResponse>> = _verifyOtp

    fun checkOtp(
        token: String,
        otp: String,
        mobile: String,
        cameFromJoining :Boolean
    ) = viewModelScope.launch {

        try {
            _verifyOtp.value = Lce.loading()

            val verifyOtpResponse = leadManagementRepository.verifyOtp(token, otp)

            if (verifyOtpResponse.isVerified) {
                val profile = profileFirebaseRepository.getProfileData()
                val response = leadManagementRepository.createUser(
                    mobile = mobile,
                    enrolledByName = profile.name
                )

                if(cameFromJoining) {
                    leadManagementRepository.createOrUpdateJoiningDocumentWithStatusSignUpPending(
                        userUid = response.uid!!,
                        name = "",
                        phoneNumber = "+91$mobile",
                        jobProfileId = "",
                        jobProfileName = "",
                        signUpMode = JoiningSignUpInitiatedMode.BY_AMBASSADOR_PROGRAM,
                        lastStatusChangeSource = "confirm_otp_screen",
                        tradeName = "",
                        jobProfileIcon = ""
                    )
                }

                _verifyOtp.value = Lce.content(response)
            } else {
                _verifyOtp.value = Lce.error("Otp does not match")
            }


        } catch (e: Exception) {
            _verifyOtp.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}