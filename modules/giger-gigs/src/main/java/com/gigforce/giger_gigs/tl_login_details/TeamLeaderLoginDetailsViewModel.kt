package com.gigforce.giger_gigs.tl_login_details

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
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
class TeamLeaderLoginDetailsViewModel @Inject constructor(
    private val iBuildConfig: IBuildConfigVM,
    private val tlLoginSummaryRepository : TlLoginSummaryRepository,
    private val logger : GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "TeamLeaderLoginDetailsViewModel"
    }

    private val firebaseAuthStateListener= FirebaseAuthStateListener.getInstance()
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    //data
    private val _observerProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>()
    }
    val observerShowProgress: SingleLiveEvent<Int> get() = _observerProgress

    private var _cities = MutableLiveData<Lce<List<LoginSummaryCity>>>()
    var cities : LiveData<Lce<List<LoginSummaryCity>>> = _cities

    private var _loginListing = MutableLiveData<Lce<List<ListingTLModel>>?>()
    var loginListing : LiveData<Lce<List<ListingTLModel>>?> = _loginListing


    private var _businesses = MutableLiveData<Lce<List<LoginSummaryBusiness>>>()
    var businesses : LiveData<Lce<List<LoginSummaryBusiness>>> = _businesses

    private var lastVisibleItem: ListingTLModel? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = true
    var pastGigs: Boolean = true
    var isInitialDataLoaded = false
    private val limit: Int = 10

    fun getListingForTL(page: Int) = viewModelScope.launch {
        _loginListing.postValue(Lce.loading())

        try {

            logger.d(TAG,"loading data for page $page")
            val response = tlLoginSummaryRepository.fetchListingForTL(page, 10)
            _loginListing.value = Lce.content(response)
            _loginListing.value = null

        }catch (e: Exception){
            e.printStackTrace()
            _loginListing.value = Lce.error(e.message ?: "Unable to fetch cities")
        }
    }


}