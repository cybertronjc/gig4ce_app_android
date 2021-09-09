package com.gigforce.giger_gigs.tl_login_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.repositories.TlLoginSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BusinessAppViewState {

    object LoadingDataFromServer : BusinessAppViewState()

    object NoGigAppsFound : BusinessAppViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : BusinessAppViewState()

    data class BusinessListLoaded(
        val businessList: List<LoginSummaryBusiness>
    ) : BusinessAppViewState()

}

@HiltViewModel
class AddNewLoginSummaryViewModel @Inject constructor (
    private val iBuildConfig: IBuildConfigVM,
    private val tlLoginSummaryRepository: TlLoginSummaryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AddNewLoginSummaryViewModel"
    }

    private val firebaseAuthStateListener= FirebaseAuthStateListener.getInstance()
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    //data
    private var _cities = MutableLiveData<Lce<List<LoginSummaryCity>>>()
    var cities : LiveData<Lce<List<LoginSummaryCity>>> = _cities

    private var _businesses = MutableLiveData<List<BusinessListRecyclerItemData>>()
    var businesses : LiveData<List<BusinessListRecyclerItemData>> = _businesses

    private var _checkinMarked = MutableLiveData<CheckMark>()
    var checkinMarked : LiveData<CheckMark> = _checkinMarked

    private var _totalCount = MutableLiveData<Int>()
    var totalCount : LiveData<Int> = _totalCount

    var businessListForView = mutableListOf<BusinessListRecyclerItemData>()

    private var businessList: List<LoginSummaryBusiness> = emptyList()
    private var businessListShown: List<LoginSummaryBusiness> = emptyList()

    private val _viewState = MutableLiveData<BusinessAppViewState>()
    val viewState: LiveData<BusinessAppViewState> = _viewState


    private val _submitDataState = MutableLiveData<String>()
    val submitDataState: LiveData<String> = _submitDataState

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
        _viewState.postValue(BusinessAppViewState.LoadingDataFromServer)

        try {
            businessList = tlLoginSummaryRepository.getBusinessByCity(cityId)
            businessListShown = businessList
            //processBusinessList(businessListShown)
            _viewState.postValue(
                BusinessAppViewState.BusinessListLoaded(
                    businessList
                )
            )

        }catch (e: Exception){
            e.printStackTrace()
            _viewState.value = BusinessAppViewState.ErrorInLoadingDataFromServer(e.message ?: "Unable to fetch businesses by city", false)
        }
    }



    fun submitLoginSummaryData(addNewSummaryReqModel: AddNewSummaryReqModel) = viewModelScope.launch{
        _submitDataState.postValue("Loading")
        try {
            val res = tlLoginSummaryRepository.submitLoginSummary(addNewSummaryReqModel)
            if (res.code() == 201){
                _submitDataState.postValue("Created")
            }  else if (res.code() == 400) {
                _submitDataState.postValue(res.errorBody()?.string() ?: "Already Exists")
            }
            else if (res.code() == 500){
                _submitDataState.postValue("Error")
            } else {
                _submitDataState.postValue("Error")
            }
        }catch (e: Exception){
            e.printStackTrace()
            _submitDataState.postValue("Error")
        }
    }

     fun processBusinessList(businessListShown: List<LoginSummaryBusiness>) {

        try {

            businessList = businessListShown
            _viewState.postValue(
                BusinessAppViewState.BusinessListLoaded(
                    businessList
                )
            )


        } catch (e: Exception){

        }
    }

    fun getBusinessListForProcessingData(): List<LoginSummaryBusiness> {
        var list = arrayListOf<LoginSummaryBusiness>()

        businessListForView.forEachIndexed { index, itemData ->
            val data = itemData as BusinessListRecyclerItemData.BusinessRecyclerItemData
            list.add(LoginSummaryBusiness(data.businessId, data.businessName, data.legalName, data.jobProfileId, data.jobProfileName, data.loginCount))
        }

        return list.toList()
    }

    fun updateList(businessId: String, text: String){
        var count = 0
        businessListForView.forEachIndexed { index, itemData ->
            val data = itemData as BusinessListRecyclerItemData.BusinessRecyclerItemData
            if (businessId.equals(data.businessId)){
                if (text.isEmpty()){
                    itemData.loginCount = null
                } else {
                    itemData.loginCount = text.toInt()
                }
            }
            data?.let {
                it.loginCount?.let {
                    count += it
                }

            }
        }
        _totalCount.postValue(count)
    }

     fun checkIfTLAttendanceMarked() = viewModelScope.launch {
        _checkinMarked.value = CheckMark()
        try {
            val attMarked = tlLoginSummaryRepository.checkIfAttendanceMarked()
            _checkinMarked.value = attMarked
        }catch (e: Exception){
            Log.d("Error", "Error while checking attendance ${e}")
        }
    }
}