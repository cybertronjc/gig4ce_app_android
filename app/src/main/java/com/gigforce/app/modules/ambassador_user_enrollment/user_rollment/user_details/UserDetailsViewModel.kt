package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.utils.Lse
import kotlinx.coroutines.launch
import java.util.*

class UserDetailsViewModel constructor(
        private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    private val _submitUserDetailsState = MutableLiveData<Lse>()
    val submitUserDetailsState: LiveData<Lse> = _submitUserDetailsState

    fun updateUserDetails(
            uid: String,
            name: String,
            dateOfBirth: Date,
            gender: String,
            highestQualification: String
    ) = viewModelScope.launch {

        _submitUserDetailsState.postValue(Lse.loading())
        try {
            profileFirebaseRepository.updateUserDetails(
                    uid = uid,
                    name = name,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    highestQualification = highestQualification
            )
            _submitUserDetailsState.value = Lse.success()
            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
            _submitUserDetailsState.value = null
        }
    }


    fun updateUserCurrentAddressDetails(
            uid: String,
            pinCode: String,
            addressLine1: String,
            addressLine2: String,
            state: String,
            city: String,
            preferredDistanceInKm: Int,
            readyToChangeLocationForWork: Boolean
    ) = viewModelScope.launch {

        _submitUserDetailsState.postValue(Lse.loading())
        try {

            profileFirebaseRepository.updateCurrentAddressDetails(
                    uid = uid,
                    pinCode = pinCode,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    state = state,
                    city = city,
                    preferredDistanceInKm = preferredDistanceInKm,
                    readyToChangeLocationForWork = readyToChangeLocationForWork
            )
            _submitUserDetailsState.value = Lse.success()
            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
            _submitUserDetailsState.value = null
        }
    }
}