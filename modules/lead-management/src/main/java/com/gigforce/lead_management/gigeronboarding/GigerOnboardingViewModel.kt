package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.datamodels.ambassador.CreateUserResponse
import com.gigforce.core.datamodels.ambassador.RegisterMobileNoResponse
import com.gigforce.core.datamodels.login.LoginResponse
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementRepo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class GigerOnboardingViewModel constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {

    companion object {
        private const val TAG = "GigerOnboardingViewModel"
    }

    private val leadManagementRepo: LeadManagementRepo = LeadManagementRepo(buildConfig)
    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()
    private val _checkMobileNo = MutableLiveData<Lce<RegisterMobileNoResponse>>()
    val checkMobileNo: LiveData<Lce<RegisterMobileNoResponse>> = _checkMobileNo

    private val _numberRegistered = MutableLiveData<Lce<UserAuthStatusModel>>()
    val numberRegistered: LiveData<Lce<UserAuthStatusModel>> = _numberRegistered

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
            val repsonse =
                leadManagementRepo.getUserAuthStatus(mobileNo)
            _numberRegistered.value = Lce.content(repsonse)
        } catch (e: Exception) {
            e.printStackTrace()
            _numberRegistered.value = Lce.error(e.message ?: "Unable to check if number already registered")
        }
    }

    fun sendOtp(
        mobileNo: String
    ) = viewModelScope.launch {

        _checkMobileNo.postValue(Lce.loading())
        try {
            val repsonse =
                leadManagementRepo.checkMobileForExistingRegistrationElseSendOtp(mobileNo)
            _checkMobileNo.value = Lce.content(repsonse)
        } catch (e: Exception) {
            e.printStackTrace()
            _checkMobileNo.value = Lce.error(e.message ?: "Unable to check mobile number")
        }
    }

    private val _verifyOtp = MutableLiveData<Lce<CreateUserResponse>>()
    val verifyOtp: LiveData<Lce<CreateUserResponse>> = _verifyOtp

    fun checkOtp(
        userId: String?,
        mode: Int,
        token: String,
        otp: String,
        mobile: String
    ) = viewModelScope.launch {

        try {
            _verifyOtp.value = Lce.loading()

                val verifyOtpResponse = leadManagementRepo.verifyOtp(token, otp)

                if (verifyOtpResponse.isVerified) {
                    //val profile = profileFirebaseRepository.getProfileData()
                    val response = leadManagementRepo.createUser(
                        createUserUrl = buildConfig.getCreateUserUrl(),
                        mobile = mobile,
                        enrolledByName = ""
                    )
                    _verifyOtp.value = Lce.content(response)
                } else {
                    _verifyOtp.value = Lce.error("Otp does not match")
                }


        } catch (e: Exception) {
            _verifyOtp.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}