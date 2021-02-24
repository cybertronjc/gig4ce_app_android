package com.gigforce.learning.learning.myLearning.journey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import com.gigforce.learning.learning.LearningRepository
import com.gigforce.learning.learning.models.Module
//import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class JourneyViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mAllModules: List<Module>? = null

    private val _allModules = MutableLiveData<Lce<List<Module>>>()
    val allModules: LiveData<Lce<List<Module>>> = _allModules

    fun getModulesFromAllAssignedCourses() = viewModelScope.launch {

        if (mAllModules != null) {
            _allModules.postValue(Lce.content(mAllModules!!))
            return@launch
        }

        _allModules.postValue(Lce.loading())

        try {
            val allModules = learningRepository.getModulesFromAllCourses()
            mAllModules = allModules

            _allModules.postValue(Lce.content(mAllModules!!))
        } catch (e: Exception) {
            _allModules.postValue(Lce.error(e.toString()))
        }
    }
}