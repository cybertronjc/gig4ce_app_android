package com.gigforce.app.modules.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.BuildConfig
import com.gigforce.app.modules.common.models.SendSmsRequest
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.utils.Lse
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.retrofit.RetrofitFactory
import kotlinx.coroutines.launch

class SendSmsViewModel(
        private val sendSmsService: SendSmsWebService = RetrofitFactory.createService(SendSmsWebService::class.java),
        private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    private val _sendSms: MutableLiveData<Lse> = MutableLiveData()
    val sendSms: LiveData<Lse> = _sendSms

    fun sendEnrollmentCompleteSms(
            userId: String,
            userName: String
    ) = viewModelScope.launch {
        _sendSms.postValue(Lse.loading())

        try {

            val profile = profileFirebaseRepository.getProfileDataIfExist(userId = userId)
            if (profile == null) {
                _sendSms.postValue(Lse.success())
                return@launch
            }

            val sendSmsRequest = SendSmsRequest(
                    phoneNumber = profile!!.loginMobile,
                    message = "Hi",
                    type = "user_enrollment_completion",
                    userName = userName,
                    enrollAmbassadorShareLink = null
            )
            val sendMessageResponse = sendSmsService.sendSms(
                    BuildConfig.SEND_SMS_URL,
                    sendSmsRequest
            )

            if (!sendMessageResponse.isSuccessful) {
                throw Exception(sendMessageResponse.message())
            } else {
                val response = sendMessageResponse.body()!!
                if (response.error != null) {
                    throw Exception(response.error)
                } else {
                    _sendSms.postValue(Lse.success())
                }
            }
        } catch (e: Exception) {
            _sendSms.postValue(Lse.error(e.toString()))
            CrashlyticsLogger.e("SendSmsViewModel", "While sending sms by phone number", e)
        }
    }

    fun sendAmbassadorInviteLink(
            phoneNumber: String,
            enrollAmbassadorLink: String
    ) = viewModelScope.launch {
        _sendSms.postValue(Lse.loading())

        try {
            val serialisedNo = if (phoneNumber.startsWith("+91")) {
                phoneNumber
            } else {
                "+91$phoneNumber"
            }

//            val profile  = profileFirebaseRepository.getProfileDataIfExist(userId = userUid)
            val sendSmsRequest = SendSmsRequest(
                    phoneNumber = serialisedNo,
                    message = "Hi",
                    type = "ambassador_enroll_though_link",
                    userName = null,
                    enrollAmbassadorShareLink = enrollAmbassadorLink
            )
            val sendMessageResponse = sendSmsService.sendSms(
                    BuildConfig.SEND_SMS_URL,
                    sendSmsRequest
            )

            if (!sendMessageResponse.isSuccessful) {
                throw Exception(sendMessageResponse.message())
            } else {
                val response = sendMessageResponse.body()!!
                if (response.error != null) {
                    throw Exception(response.error)
                } else {
                    _sendSms.postValue(Lse.success())
                }
            }
        } catch (e: Exception) {
            _sendSms.postValue(Lse.error(e.toString()))
            CrashlyticsLogger.e("SendSmsViewModel", "While sending sms by uid", e)
        }
    }

}