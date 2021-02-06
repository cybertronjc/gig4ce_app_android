package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.giger_app.repo.IHomeCardsFBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val homeCardsFBRepository: IHomeCardsFBRepository
) : ViewModel() {
    private var allLandingData: MutableLiveData<List<Any>> = MutableLiveData<List<Any>>()

    var _allLandingData: LiveData<List<Any>> = allLandingData

    init {
        homeCardsFBRepository.getData().observeForever {
            it?.let {
                this.allLandingData.value = it
            }
        }
    }
}