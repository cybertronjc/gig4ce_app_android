package com.gigforce.giger_app.vm

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val repository: IMainNavDataRepository, application: Application
) :
    AndroidViewModel(application) {
    var state: Parcelable? = null
    private val _liveData: MutableLiveData<List<FeatureItemCard2DVM>> =
        MutableLiveData<List<FeatureItemCard2DVM>>()
    var liveData: LiveData<List<FeatureItemCard2DVM>> = _liveData

//    init {
//        requestData()
//    }

    fun requestData(currentVersionCode:Int) = viewModelScope.launch {
        try {
            repository.getData(currentVersionCode).catch {
            }.collect {
                if(it.isNotEmpty())
                _liveData.value = it
                repository.notifyToServer()
            }
        } catch (e: Exception) {
        }
    }

    fun getDefaultData() = repository.getDefaultData(getApplication())


}