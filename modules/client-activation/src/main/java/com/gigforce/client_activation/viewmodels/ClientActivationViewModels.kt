package com.gigforce.client_activation.viewmodels

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientActivationViewModels @Inject constructor(private val repository: IClientActivationDataRepository) :ViewModel() {
    var state : Parcelable?=null

    private val _liveData : MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData<List<FeatureItemCardDVM>>()
    var liveData : LiveData<List<FeatureItemCardDVM>> = _liveData


    fun requestLiveData(priorityVal : Long) = viewModelScope.launch{
        try {
            _liveData.value =  repository.getData(priorityVal)
        }catch (e:Exception){
        }
    }

}