package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigerProfileFirebaseRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.utils.Lce
import kotlinx.coroutines.launch

class GigerProfileViewModel : ViewModel(){

    var profileFirebaseRepository =
        GigerProfileFirebaseRepository()
    private val _profile = MutableLiveData<Lce<ProfileData?>>()
    val profile: LiveData<Lce<ProfileData?>> = _profile

    fun getProfileFromMobileNo(
        phoneNumber: String
    ) = viewModelScope.launch {
        _profile.value = Lce.loading()

        try {
            var finalPhoneNumber = phoneNumber
            if (phoneNumber.isBlank()) {
                _profile.value = Lce.error("getProfileFromMobileNo: Provide non empty phone number")
            } else if (!phoneNumber.startsWith("+91")) {
                finalPhoneNumber = "+91$phoneNumber"
            } else {
                finalPhoneNumber = phoneNumber
            }

            val profile = profileFirebaseRepository.getFirstProfileWithPhoneNumber(finalPhoneNumber)
            _profile.value = Lce.content(profile)
        } catch (e: Exception) {
            _profile.value = Lce.error(e.message ?: "Unable to fetch")
        }

    }
}