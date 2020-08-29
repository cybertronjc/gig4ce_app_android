package com.gigforce.app.modules.learning.courseDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.data.Course
import com.gigforce.app.modules.learning.data.CourseContent
import com.gigforce.app.modules.learning.data.Module
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class CourseDetailsViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mLastReqCourseDetails: Course? = null

    private val _courseDetails = MutableLiveData<Lce<Course>>()
    val courseDetails: LiveData<Lce<Course>> = _courseDetails

    fun getCourseDetails(courseId: String) = viewModelScope.launch {

        if (mLastReqCourseDetails != null) {
            _courseDetails.postValue(Lce.content(mLastReqCourseDetails!!))
            return@launch
        }

        _courseDetails.postValue(Lce.loading())

        try {
            val course = learningRepository.getCourseDetails(courseId)
            mLastReqCourseDetails = course

            _courseDetails.postValue(Lce.content(course))
        } catch (e: Exception) {
            _courseDetails.postValue(Lce.error(e.toString()))
        }
    }


    //Getting Course Modules

    private val _courseModules = MutableLiveData<Lce<List<Module>>>()
    val courseModules: LiveData<Lce<List<Module>>> = _courseModules

    fun getCourseModules(
        courseId: String
    ) = viewModelScope.launch {
        _courseModules.postValue(Lce.loading())

        try {
            val courseModules = learningRepository.getModules(
                courseId = courseId
            )
            _courseModules.postValue(Lce.content(courseModules))

            if (courseModules.isNotEmpty()) {

                //TODO change logic
                val activeModule = courseModules.first()
                getCourseLessonsAndAssessments(
                    courseId = courseId,
                    moduleId = activeModule.id
                )
            } else {
                _courseLessons.postValue(Lce.content(emptyList()))
                _courseAssessments.postValue(Lce.content(emptyList()))
            }
        } catch (e: Exception) {
            _courseModules.postValue(Lce.error(e.toString()))
        }
    }


    //Getting Course Lessons

    private val _courseLessons = MutableLiveData<Lce<List<CourseContent>>>()
    val courseLessons: LiveData<Lce<List<CourseContent>>> = _courseLessons

    fun getCourseLessonsAndAssessments(
        courseId: String,
        moduleId: String
    ) = viewModelScope.launch {
        _courseLessons.postValue(Lce.loading())
        _courseAssessments.postValue(Lce.loading())

        try {
            val courseLessons = learningRepository.getModuleLessons(
                courseId = courseId,
                moduleId = moduleId
            )

            _courseLessons.postValue(Lce.content(courseLessons))

            val assessments = courseLessons.filter {
                it.type == CourseContent.TYPE_ASSESSMENT
            }
            _courseAssessments.postValue(Lce.content(assessments))

        } catch (e: Exception) {
            _courseLessons.postValue(Lce.error(e.toString()))
        }
    }


    //Getting Course Assessments

    private val _courseAssessments = MutableLiveData<Lce<List<CourseContent>>>()
    val courseAssessments: LiveData<Lce<List<CourseContent>>> = _courseAssessments

    fun getCourseAssessments(
        courseId: String,
        moduleId: String
    ) = viewModelScope.launch {
        _courseAssessments.postValue(Lce.loading())

        try {
            val courseAssessments = learningRepository.getModuleAssessments(
                courseId = courseId,
                moduleId = moduleId
            )

            _courseAssessments.postValue(Lce.content(courseAssessments))
        } catch (e: Exception) {
            _courseAssessments.postValue(Lce.error(e.toString()))
        }
    }
}