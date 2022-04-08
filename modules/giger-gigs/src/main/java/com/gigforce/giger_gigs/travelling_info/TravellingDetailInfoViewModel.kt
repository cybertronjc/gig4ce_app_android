package com.gigforce.giger_gigs.travelling_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TravellingDetailInfoViewModel @Inject constructor(val travellingRepository : TravellingRepository) :
    ViewModel() {
    private val _travellingInfoLiveData = MutableLiveData<Lce<TravellingResponseDM>>()
    val travellingInfoLiveData: LiveData<Lce<TravellingResponseDM>> = _travellingInfoLiveData

//    init {
//        getAllTravellingInfo()
//    }

    fun getAllTravellingInfo(fromDate: String,toDate: String) = viewModelScope.launch {
        _travellingInfoLiveData.value = Lce.loading()
        try {
            val response = travellingRepository.getAllTravellingInfo(fromDate,toDate)
            response?.let {
                _travellingInfoLiveData.value = Lce.content(it)
            }?:run{
                _travellingInfoLiveData.value = Lce.error("No data found!!")
            }
        } catch (e: Exception) {
            _travellingInfoLiveData.value = Lce.error(e.toString())
        }
    }
}