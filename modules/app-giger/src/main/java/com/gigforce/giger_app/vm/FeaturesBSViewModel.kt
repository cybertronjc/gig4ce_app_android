package com.gigforce.giger_app.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.giger_app.repo.BSDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeaturesBSViewModel @Inject constructor(
    private val bsDataRepository : BSDataRepository
) : ViewModel() {
    private var allBSData: MutableLiveData<List<Any>> = MutableLiveData<List<Any>>()

    var _allBSData : LiveData<List<Any>> = allBSData

    init {

        bsDataRepository.getData().observeForever {
            it?.let {
                this.allBSData.value = it
                Log.d("dataFeatureVM", it.toString())
            }
        }
    }

}