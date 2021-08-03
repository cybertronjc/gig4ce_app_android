package com.gigforce.giger_gigs.tl_login_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.models.LoginSummaryBusiness
import com.gigforce.giger_gigs.models.LoginSummaryCity
import com.gigforce.giger_gigs.repositories.TlLoginSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamLeaderLoginDetailsViewModel @Inject constructor(
    private val tlLoginSummaryRepository: TlLoginSummaryRepository,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "TeamLeaderLoginDetailsViewModel"
    }

    //data
    private var _cities = MutableLiveData<Lce<List<LoginSummaryCity>>>()
    var cities : LiveData<Lce<List<LoginSummaryCity>>> = _cities

    private var _businesses = MutableLiveData<Lce<List<LoginSummaryBusiness>>>()
    var businesses : LiveData<Lce<List<LoginSummaryBusiness>>> = _businesses

    fun getCities() = viewModelScope.launch {
        _cities.postValue(Lce.loading())

        try {
            val response = tlLoginSummaryRepository.getCities()
            _cities.value = Lce.content(response)

        }catch (e: Exception){
            e.printStackTrace()
            _cities.value = Lce.error(e.message ?: "Unable to fetch cities")
        }
    }

    fun getBusinessByCity(cityId: String) = viewModelScope.launch {
        _businesses.postValue(Lce.loading())

        try {
            val response = tlLoginSummaryRepository.getBusinessByCity(cityId)
            _businesses.value = Lce.content(response)

        }catch (e: Exception){
            e.printStackTrace()
            _businesses.value = Lce.error(e.message ?: "Unable to fetch businesses by city")
        }
    }
}