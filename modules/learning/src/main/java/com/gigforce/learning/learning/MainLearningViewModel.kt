package com.gigforce.learning.learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import com.gigforce.core.datamodels.learning.CourseContent
//import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class MainLearningViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mAllAssessments: List<CourseContent>? = null

    private val _allAssessments = MutableLiveData<Lce<List<CourseContent>>>()
    val allAssessments: LiveData<Lce<List<CourseContent>>> = _allAssessments

    fun getAssessmentsFromAllAssignedCourses() = viewModelScope.launch {

        if (mAllAssessments != null) {
            _allAssessments.postValue(Lce.content(mAllAssessments!!))
            return@launch
        }

        _allAssessments.postValue(Lce.loading())

        try {
            val courseAssessments = learningRepository.getAssessmentsFromAllCourses()
            mAllAssessments = courseAssessments

            _allAssessments.postValue(Lce.content(courseAssessments))
        } catch (e: Exception) {
            _allAssessments.postValue(Lce.error(e.toString()))
        }
    }
}