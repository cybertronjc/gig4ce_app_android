package com.gigforce.common_ui.configrepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigViewModel constructor(
    private val appConfigurationRepository: AppConfigurationRepository = AppConfigurationRepository()
) : ViewModel(){

    private val _activeLanguages : MutableLiveData<List<String>> = MutableLiveData()
    val activeLanguages : LiveData<List<String>> = _activeLanguages

    init {
        getActiveLanguages()
    }

    private fun getActiveLanguages() = viewModelScope.launch{
        try {
            val activeLanguages = appConfigurationRepository.getActiveLanguages()
            _activeLanguages.value = activeLanguages
        } catch (e: Exception) {

        }
    }
}