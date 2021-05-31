package com.gigforce.ambassador

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.utils.Lce
import com.gigforce.core.datamodels.ambassador.EnrolledUser
import com.gigforce.core.datamodels.ambassador.RegisterMobileNoResponse
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.UserEnrollmentRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmbassadorEnrollViewModel @Inject constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {
    private val ambassadorEnrollmentRepository: com.gigforce.ambassador.AmbassadorEnrollmentRepository =
        com.gigforce.ambassador.AmbassadorEnrollmentRepository()
    private val profileFirebaseRepository: ProfileFirebaseRepository =
        ProfileFirebaseRepository()
    private val userEnrollmentRepository: UserEnrollmentRepository =
        UserEnrollmentRepository(buildConfig = buildConfig)
    private val _enrolledUsers = MutableLiveData<List<EnrolledUser>>()
    val enrolledUsers: LiveData<List<EnrolledUser>> = _enrolledUsers

    private var enrolledUserListener: ListenerRegistration? = null

    init {
        startWatchingEnrolledUsersList()
    }

    private fun startWatchingEnrolledUsersList() {
        enrolledUserListener = ambassadorEnrollmentRepository
            .getEnrolledUsersQuery()
            .addSnapshotListener { value, error ->
                error?.printStackTrace()

                value?.let {
                    val enrolledUsers = it.documents.map {
                        it.toObject(EnrolledUser::class.java)!!.apply {
                            this.id = it.id
                        }
                    }

                        _enrolledUsers.postValue(
                                enrolledUsers.sortedByDescending {
                                    it.enrolledOn.seconds
                                })
//                        _enrolledUsers.postValue(null)
                    }
                }
    }

    private val _sendOtpToPhoneNumber = MutableLiveData<Lce<SendOtpResponseData>>()
    val sendOtpToPhoneNumber: LiveData<Lce<SendOtpResponseData>> = _sendOtpToPhoneNumber

    fun getMobileNumberAndSendOtpInfo(
        enrolledUser: EnrolledUser
    ) = viewModelScope.launch {

        try {
            _sendOtpToPhoneNumber.value = Lce.loading()
            var userMobileNo = enrolledUser.mobileNumber

            if (userMobileNo.isEmpty()) {
                val profileData = profileFirebaseRepository.getProfileData(enrolledUser.uid)
                userMobileNo = profileData.loginMobile
                enrolledUser.mobileNumber = userMobileNo
            }

            if (userMobileNo.startsWith("+91")) {
                userMobileNo = userMobileNo.substring(3)
            }

            val response =
                userEnrollmentRepository.checkMobileForExistingRegistrationElseSendOtp(userMobileNo)
            _sendOtpToPhoneNumber.value = Lce.content(
                SendOtpResponseData(
                    enrolledUser = enrolledUser,
                    checkMobileResponse = response
                )
            )
            _sendOtpToPhoneNumber.value = null
        } catch (e: Exception) {
            _sendOtpToPhoneNumber.value = Lce.error(e.message ?: "Unable to send otp")
            _sendOtpToPhoneNumber.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        enrolledUserListener?.remove()
    }

    fun getUID(): String {
        return ambassadorEnrollmentRepository.getUID()
    }
}

data class SendOtpResponseData(
    val enrolledUser: EnrolledUser,
    val checkMobileResponse: RegisterMobileNoResponse
)