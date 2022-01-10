package com.gigforce.learning.viewmodels

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.repo.ILearningDataRepository
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class LearningComponentViewModel @Inject constructor(val repository: ILearningDataRepository) : ViewModel() {
    var state : Parcelable?=null

    var allCourses: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()

    fun requestLearningData() = viewModelScope.launch{
        try {
            allCourses.value = repository.requestData()
        }catch (e:Exception){

        }

    }

}