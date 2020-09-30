package com.gigforce.app.modules.assessment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch


data class AssessmentResult(
    val state : Int,
    val nextDest : CourseContent? = null
)

class AssessmentDialogViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private val _savingAssessmentState = MutableLiveData<Lce<AssessmentResult>>()
    val savingAssessmentState : LiveData<Lce<AssessmentResult>> = _savingAssessmentState

    fun saveAssessmentState(moduleId : String,lessonId : String, state : Int) = viewModelScope.launch{
        _savingAssessmentState.value = Lce.loading()

        try {

            val nextDest = learningRepository.markCurrentLessonAsComplete(moduleId,lessonId)
            _savingAssessmentState.value = Lce.content(AssessmentResult(
                state = state,
                nextDest = nextDest
            ))
        } catch (e: Exception) {
            _savingAssessmentState.value = Lce.error(e.toString())
        }
    }
}