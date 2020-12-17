package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.BuildConfig
import com.gigforce.app.modules.ambassador_user_enrollment.AmbassadorEnrollmentRepository
import com.gigforce.app.modules.ambassador_user_enrollment.models.CheckPhoneNumberAndSendOtpResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.CreateUserRequest
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.verification.service.CreateUserAccEnrollmentAPi
import com.gigforce.app.modules.verification.service.RetrofitFactory
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

    private val _createProfile = MutableLiveData<Lce<String>>()
    val createProfile: LiveData<Lce<String>> = _createProfile

    fun otpMatchedCreateProfile(
        mobile: String
    ) = viewModelScope.launch {

        try {

        } catch (e: Exception) {
            _createProfile.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}