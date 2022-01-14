package com.gigforce.giger_app.vm

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.giger_app.repo.IHelpVideosDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpVideosViewModels @Inject constructor(val repository: IHelpVideosDataRepository): ViewModel() {
    var state : Parcelable?=null

    var data: MutableLiveData<List<Any>> = MutableLiveData()

    fun requestData(limit : Long) = viewModelScope.launch {
        try {
            data.value = repository.requestData(limit)
        }catch (e:Exception){

        }
    }
}