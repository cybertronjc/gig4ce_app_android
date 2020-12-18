package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.models.CheckPhoneNumberAndSendOtpResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.CreateUserResponse
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class VerifyUserMobileViewModel constructor(
    private val userEnrollmentRepository: UserEnrollmentRepository = UserEnrollmentRepository(),
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    private val _checkMobileNo = MutableLiveData<Lce<CheckPhoneNumberAndSendOtpResponse>>()
    val checkMobileNo: LiveData<Lce<CheckPhoneNumberAndSendOtpResponse>> = _checkMobileNo

    fun checkMobileNo(
        mobileNo: String
    ) = viewModelScope.launch {

        _checkMobileNo.postValue(Lce.loading())
        try {
            //todo
            _checkMobileNo.value = Lce.content(
                CheckPhoneNumberAndSendOtpResponse(
                    isUserAlreadyRegistered = false,
                    otpSent = "000000"
                )
            )
            _checkMobileNo.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _checkMobileNo.value = Lce.error(e.message ?: "Unable to check mobile number")
            _checkMobileNo.value = null
        }
    }

    private val _createProfile = MutableLiveData<Lce<CreateUserResponse>>()
    val createProfile: LiveData<Lce<CreateUserResponse>> = _createProfile

    fun otpMatchedCreateProfile(
        mobile: String
    ) = viewModelScope.launch {

        try {
            _createProfile.value = Lce.loading()
            val response = userEnrollmentRepository.createUser(mobile)
            _createProfile.value = Lce.content(response)
        } catch (e: Exception) {
            _createProfile.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}