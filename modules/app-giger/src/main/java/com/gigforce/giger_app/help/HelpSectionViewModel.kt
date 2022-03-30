package com.gigforce.giger_app.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpSectionViewModel @Inject constructor(val helpSectionRepository : HelpSectionRepository): ViewModel() {

    private val _helpSectionLiveData = MutableLiveData<Lce<ArrayList<HelpSectionDM>>>()
    val helpSectionLiveData: LiveData<Lce<ArrayList<HelpSectionDM>>> = _helpSectionLiveData

    init {
        getHelpSectionData()
    }

    fun getHelpSectionData() = viewModelScope.launch {
        _helpSectionLiveData.value = Lce.Loading
        try {
            val data = helpSectionRepository.getHelpSectionData()
            data.let {
                _helpSectionLiveData.value = Lce.content(it)
            }

        }catch (e:Exception){
            _helpSectionLiveData.value = Lce.error(e.toString())
        }
    }


}