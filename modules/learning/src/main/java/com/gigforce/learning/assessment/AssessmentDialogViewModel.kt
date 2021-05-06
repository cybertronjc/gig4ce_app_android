package com.gigforce.learning.assessment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import com.gigforce.learning.learning.LearningRepository
import com.gigforce.core.datamodels.learning.CourseContent
import kotlinx.coroutines.launch


data class AssessmentResult(
    val state : Int,
    val nextDest : CourseContent? = null
)

class AssessmentDialogViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    var nextDest : CourseContent? = null

    private val _savingAssessmentState = MutableLiveData<Lce<AssessmentResult>>()
    val savingAssessmentState : LiveData<Lce<AssessmentResult>> = _savingAssessmentState

    fun saveAssessmentState(moduleId : String,lessonId : String, state : Int,isPassed:Boolean?) = viewModelScope.launch{
        _savingAssessmentState.value = Lce.loading()

        try {

            nextDest = learningRepository.markCurrentLessonAsComplete(moduleId,lessonId,isPassed)
            _savingAssessmentState.value = Lce.content(AssessmentResult(
                state = state,
                nextDest = nextDest
            ))
        } catch (e: Exception) {
            _savingAssessmentState.value = Lce.error(e.toString())
        }
    }
}