package com.gigforce.giger_gigs.tl_login_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.models.LoginSummaryBusiness
import com.gigforce.giger_gigs.models.LoginSummaryCity
import com.gigforce.giger_gigs.repositories.TlLoginSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TLDailyReportListViewModel @Inject constructor(
    private val iBuildConfig: IBuildConfigVM
) : ViewModel() {

    companion object {
        private const val TAG = "TeamLeaderLoginDetailsViewModel"
    }
    private val tlLoginSummaryRepository= TlLoginSummaryRepository(iBuildConfig)
    private val firebaseAuthStateListener= FirebaseAuthStateListener.getInstance()
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    //data
    private var _cities = MutableLiveData<Lce<List<LoginSummaryCity>>>()
    var cities : LiveData<Lce<List<LoginSummaryCity>>> = _cities

    private var _loginListing = MutableLiveData<Lce<List<ListingTLModel>>>()
    var loginListing : LiveData<Lce<List<ListingTLModel>>> = _loginListing


    private var _businesses = MutableLiveData<Lce<List<LoginSummaryBusiness>>>()
    var businesses : LiveData<Lce<List<LoginSummaryBusiness>>> = _businesses

    private var lastVisibleItem: ListingTLModel? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = true
    var pastGigs: Boolean = true
    var isInitialDataLoaded = false
    private val limit: Int = 10

    fun getListingForTL( searchCity: String, searchDate: String) = viewModelScope.launch {
        _loginListing.postValue(Lce.loading())

        try {
            val response = tlLoginSummaryRepository.fetchTLDailyLoginReportListingForTL(searchCity,searchDate,0, 100)
            _loginListing.value = Lce.content(response)

        }catch (e: Exception){
            e.printStackTrace()
            _loginListing.value = Lce.error(e.message ?: "Unable to fetch cities")
        }
    }

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