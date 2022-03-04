package com.gigforce.giger_app.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.giger_app.repo.IFeatureIconsDataRepository
import com.gigforce.giger_app.repo.IMainNavDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubiconFolderBSViewModel  @Inject constructor(private val repository: IFeatureIconsDataRepository) : ViewModel()  {

    private var _allIconsLiveData =  MutableLiveData<List<FeatureItemCard2DVM>>()
    var allIconsLiveData : LiveData<List<FeatureItemCard2DVM>> = _allIconsLiveData

    fun requestData(currentVersionCode: Int) = viewModelScope.launch{
        try {
            repository.getDataBS(currentVersionCode).catch {  }.collect {
                if(it.isNotEmpty())
                _allIconsLiveData.value = it
                repository.notifyToServer()
            }
        }catch (e:Exception){}
    }
}