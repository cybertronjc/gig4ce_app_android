package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
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

@HiltViewModel
class GigerOnboardingViewModel @Inject constructor(
    private val leadManagementRepo: LeadManagementRepo,
    private val buildConfig: IBuildConfigVM

) : ViewModel() {

    private val profileFirebaseRepository: ProfileFirebaseRepository =
        ProfileFirebaseRepository()
    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()
    private val _checkMobileNo = MutableLiveData<Lce<RegisterMobileNoResponse>>()
    val checkMobileNo: LiveData<Lce<RegisterMobileNoResponse>> = _checkMobileNo

    init {
        FirebaseAuth.getInstance().currentUser.let {
        }
    }



    fun checkMobileNo(
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

    private val _createProfile = MutableLiveData<Lce<CreateUserResponse>>()
    val createProfile: LiveData<Lce<CreateUserResponse>> = _createProfile

    fun checkOtpAndCreateProfile(
        userId: String?,
        mode: Int,
        token: String,
        otp: String,
        mobile: String,
        latitude: Double,
        longitude: Double,
        fullAddress: String
    ) = viewModelScope.launch {

        try {
                _createProfile.value = Lce.loading()

                val verifyOtpResponse = leadManagementRepo.verifyOtp(token, otp)

                if (verifyOtpResponse.isVerified) {
                    val profile = profileFirebaseRepository.getProfileData()
                    val response = leadManagementRepo.createUser(
                        createUserUrl = buildConfig.getCreateUserUrl(),
                        mobile = mobile,
                        enrolledByName = profile.name,
                        latitude = latitude,
                        longitude = longitude,
                        fullAddress = fullAddress
                    )
                    _createProfile.value = Lce.content(response)
                } else {
                    _createProfile.value = Lce.error("Otp does not match")
                }


        } catch (e: Exception) {
            _createProfile.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}