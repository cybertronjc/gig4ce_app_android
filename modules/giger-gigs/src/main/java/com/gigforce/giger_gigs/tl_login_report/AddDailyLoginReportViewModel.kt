package com.gigforce.giger_gigs.tl_login_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.repositories.TlLoginSummaryRepository
import com.google.gson.annotations.SerializedName
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
        val businessList: List<BusinessData>
    ) : BusinessAppViewState()

}

@HiltViewModel
class AddDailyLoginReportViewModel @Inject constructor (
    private val iBuildConfig: IBuildConfigVM
) : ViewModel() {

    companion object {
        private const val TAG = "AddDailyLoginReportViewModel"
    }

    private val logger = GigforceLogger()
    private val tlLoginSummaryRepository= TlLoginSummaryRepository(iBuildConfig)
    private val firebaseAuthStateListener= FirebaseAuthStateListener.getInstance()
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    //data
    private var _cities = MutableLiveData<Lce<List<LoginSummaryCity>>>()
    var cities : LiveData<Lce<List<LoginSummaryCity>>> = _cities

    private var _businesses = MutableLiveData<List<BusinessListRecyclerItemData>>()
    var businesses : LiveData<List<BusinessListRecyclerItemData>> = _businesses

    var businessListForView = mutableListOf<BusinessListRecyclerItemData>()

    private var businessList: List<BusinessData> = emptyList()
    private var businessListShown: List<BusinessData> = emptyList()

    private val _viewState = MutableLiveData<BusinessAppViewState>()
    val viewState: LiveData<BusinessAppViewState> = _viewState

    private val _submitDataState = MutableLiveData<Lce<String>>()
    val submitDataState: LiveData<Lce<String>> = _submitDataState

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
            businessList = tlLoginSummaryRepository.getBusinessByCity(cityId).map {
                BusinessData(
                    legalName = it.legalName,
                    businessId = it.business_id,
                    businessName = it.businessName,
                    jobProfileId = it.jobProfileId,
                    jobProfileName = it.jobProfileName
                )
            }
            businessListShown = businessList

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

    fun processBusinessList(businessListShown: List<BusinessData>) {

        businessListForView.clear()

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



    fun submitLoginReportData(
        addNewSummaryReqModel:  List<DailyTlAttendanceReport>
    ) = viewModelScope.launch{
        _submitDataState.postValue(Lce.loading())
        try {
            logger.d(TAG,"Submitting login report ...${addNewSummaryReqModel}")

            val response = tlLoginSummaryRepository.submitLoginReport(
                addNewSummaryReqModel
            )

            logger.d(TAG,"Submitting login report submitted")
            _submitDataState.postValue(Lce.content(
                response.message ?: "Record submitted"
            ))
        }catch (e: Exception){
            logger.e(TAG,"Submitting login report submitted",e)

            e.printStackTrace()
            _submitDataState.postValue(Lce.error(
                e.message ?: "Unable to submit, please try again"
            ))
        }
    }

}