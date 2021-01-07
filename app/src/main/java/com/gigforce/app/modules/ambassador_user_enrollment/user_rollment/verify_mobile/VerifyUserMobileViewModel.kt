package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.models.CreateUserResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.RegisterMobileNoResponse
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class VerifyUserMobileViewModel constructor(
        private val userEnrollmentRepository: UserEnrollmentRepository = UserEnrollmentRepository(),
        private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    private val _checkMobileNo = MutableLiveData<Lce<RegisterMobileNoResponse>>()
    val checkMobileNo: LiveData<Lce<RegisterMobileNoResponse>> = _checkMobileNo

    fun checkMobileNo(
            mobileNo: String
    ) = viewModelScope.launch {

        _checkMobileNo.postValue(Lce.loading())
        try {
            val repsonse = userEnrollmentRepository.registerUser(mobileNo)
            _checkMobileNo.value = Lce.content(repsonse)
            _checkMobileNo.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _checkMobileNo.value = Lce.error(e.message ?: "Unable to check mobile number")
            _checkMobileNo.value = null
        }
    }


    private val _createProfile = MutableLiveData<Lce<CreateUserResponse>>()
    val createProfile: LiveData<Lce<CreateUserResponse>> = _createProfile

    fun checkOtpAndCreateProfile(
            token: String,
            otp: String,
            mobile: String, location: Location
    ) = viewModelScope.launch {

        try {
            val verifyOtpResponse = userEnrollmentRepository.verifyOtp(token, otp)

            if (verifyOtpResponse.isVerified) {
                val response = userEnrollmentRepository.createUser(mobile,location)
                _createProfile.value = Lce.content(response)
            } else {
                _createProfile.value = Lce.error("Otp does not match")
            }

        } catch (e: Exception) {
            _createProfile.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}