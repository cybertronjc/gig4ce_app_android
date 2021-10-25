package com.gigforce.giger_gigs.tl_login_details

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningNew
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
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
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

    private var listingRaw: List<ListingTLModel>? = null

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
    var filterDaysVM: Int? = 30

    fun getListingForTL(page: Int) = viewModelScope.launch {
        _loginListing.postValue(Lce.loading())

        try {

            logger.d(TAG,"loading data for page $page")
            val response = tlLoginSummaryRepository.fetchListingForTL(page, 10)
            listingRaw = response
            processListing(listingRaw!!)

        }catch (e: Exception){
            e.printStackTrace()
            _loginListing.value = Lce.error(e.message ?: "Unable to fetch cities")
        }
    }

    private fun processListing(response: List<ListingTLModel>) {
        Log.d("filterDaysVM", "days $filterDaysVM")
        val filteredList = response.filter {
            if (filterDaysVM == null || filterDaysVM == -1)
                true
            else if (filterDaysVM in 0..1){
                getDateDifference(it.date) == filterDaysVM!!
            }
            else {
                getDateDifference(it.date) <= filterDaysVM!!
            }
        }

        _loginListing.value = Lce.content(filteredList)
        _loginListing.value = null
    }

    fun filterDaysLoginSummary(
        filterDays: Int
    ){
        logger.d(TAG, "new filter click received $filterDays")
        filterDaysVM = filterDays

//        if(listingRaw != null)
//            processListing(listingRaw!!)

    }
    private fun getDateDifference(createdAt: String): Int {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
        val date = LocalDate.parse(createdAt, formatter)
        return if (currentDate.isEqual(date)){
            0
        } else {
            val daysDiff = Duration.between(
                date.atStartOfDay(),
                currentDate.atStartOfDay()
            ).toDays()
            daysDiff.toInt()
        }
    }

}