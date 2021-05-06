package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.core.utils.Lce
import com.gigforce.core.datamodels.ambassador.CreateUserResponse
import com.gigforce.core.datamodels.ambassador.RegisterMobileNoResponse
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.UserEnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyUserMobileViewModel @Inject constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private var buildConfig: IBuildConfigVM
) : ViewModel() {
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val userEnrollmentRepository: UserEnrollmentRepository =
        UserEnrollmentRepository(buildConfig = buildConfig)
    private val _checkMobileNo = MutableLiveData<Lce<RegisterMobileNoResponse>>()
    val checkMobileNo: LiveData<Lce<RegisterMobileNoResponse>> = _checkMobileNo

    fun checkMobileNo(
        mobileNo: String
    ) = viewModelScope.launch {

        _checkMobileNo.postValue(Lce.loading())
        try {
            val repsonse =
                userEnrollmentRepository.checkMobileForExistingRegistrationElseSendOtp(mobileNo)
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

            var ambassadorMasterOtp = ""
            ambassadorMasterOtp = firebaseRemoteConfig.getString("Ambassador_master_otp")

            if (otp == ambassadorMasterOtp && mode == EnrollmentConstants.MODE_EDIT) {
                //
                _createProfile.value = Lce.content(CreateUserResponse(
                        phoneNumber = mobile,
                        uid = null,
                        error = null
                ))

                if (userId != null) {
                    userEnrollmentRepository.addEditLocationInLocationLogs(
                            userId = userId,
                            latitude = latitude,
                            longitude = longitude,
                            fullAddress = fullAddress,
                            editedUsingMasterOtp = true
                    )
                }

            } else {
                val verifyOtpResponse = userEnrollmentRepository.verifyOtp(token, otp)
                if (mode == EnrollmentConstants.MODE_EDIT) {
                    if (verifyOtpResponse.isVerified) {
                        _createProfile.value = Lce.content(CreateUserResponse(
                                phoneNumber = mobile,
                                uid = null,
                                error = null
                        ))

                    if (userId != null) {
                        userEnrollmentRepository.addEditLocationInLocationLogs(
                                userId = userId,
                                latitude = latitude,
                                longitude = longitude,
                                fullAddress = fullAddress
                        )
                    }
                } else {
                    _createProfile.value = Lce.error("Otp does not match")
                }
            } else {
                if (verifyOtpResponse.isVerified) {
                    val profile = profileFirebaseRepository.getProfileData()
                    val response = userEnrollmentRepository.createUser(
                        createUserUrl = buildConfig.getCreateUserUrl(),
                            mobile = mobile,
                            enrolledByName = profile.name,
                            latitude = latitude,
                            longitude = longitude,
                            fullAddress = fullAddress
                    )
                    if (!profile.isUserAmbassador)
                        profileFirebaseRepository.setUserAsAmbassador()
                    _createProfile.value = Lce.content(response)
                } else {
                    _createProfile.value = Lce.error("Otp does not match")
                }
            }

        } catch (e: Exception) {
            _createProfile.value = Lce.error(e.message ?: "Unable to create user")
        }
    }
}