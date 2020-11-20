package com.gigforce.app.modules.learning.courseDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.models.Module
import com.gigforce.app.modules.learning.models.progress.ModuleProgress
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class CourseDetailsViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    var mLastReqCourseDetails: Course? = null
    var currentModules: List<Module>? = null
    var currentlySelectedModule: Module? = null
    var currentLessons: List<CourseContent>? = null
    var currentlySelectedModulePosition = 0

    private var mCurrentModuleId: String? = null
    var mCurrentModulesProgressData: List<ModuleProgress>? = null
    private var courseDataSynced: Boolean = false


    private val _courseDetails = MutableLiveData<Lce<Course>>()
    val courseDetails: LiveData<Lce<Course>> = _courseDetails

    fun getCourseDetailsAndModules(mCourseId: String) = viewModelScope.launch {

        if (courseDataSynced) {
            getCourseDetails(mCourseId)
            getCourseModules(mCourseId)
        } else {
            _courseDetails.postValue(Lce.loading())
            _courseModules.postValue(Lce.loading())

            val courseProgressDataGenerated = learningRepository.courseProgressDataGenerated(mCourseId)

            if(!courseProgressDataGenerated){

                getCourseDetails(mCourseId)
                getCourseModules(mCourseId)
                courseDataSynced = true
            } else {
                syncCourseProgressData(mCourseId)

                getCourseDetails(mCourseId)
                getCourseModules(mCourseId)
            }
        }
    }

    private suspend fun syncCourseProgressData(courseId: String) {
        learningRepository.syncCourseProgressData(courseId)
        courseDataSynced = true
    }

    fun getCourseDetails(courseId: String) = viewModelScope.launch {

        if (mLastReqCourseDetails != null) {
            _courseDetails.postValue(Lce.content(mLastReqCourseDetails!!))
            return@launch
        }

        _courseDetails.postValue(Lce.loading())

        try {

            val course = learningRepository.getCourseDetails(courseId)
            val courseProgressDetails = learningRepository.getCourseProgress(courseId)

            course.courseStartDate = courseProgressDetails.courseStartDate
            course.courseCompletionDate = courseProgressDetails.courseCompletionDate
            course.completed = courseProgressDetails.completed

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

        if (currentModules != null) {
            _courseModules.postValue(Lce.content(currentModules!!))
            return@launch
        }

        _courseModules.postValue(Lce.loading())

        try {
            val courseModules = learningRepository.getModules(
                courseId = courseId
            )
            currentModules = appendProgressInfoToModules(courseId, courseModules)
            _courseModules.postValue(Lce.content(courseModules))

            if (courseModules.isNotEmpty()) {

                currentlySelectedModule = courseModules.first()
                mCurrentModuleId = currentlySelectedModule!!.id
                getCourseLessonsAndAssessments(
                    courseId = courseId,
                    moduleId = currentlySelectedModule!!.id
                )
            } else {
                _courseLessons.postValue(Lce.content(emptyList()))
                _courseAssessments.postValue(Lce.content(emptyList()))
            }
        } catch (e: Exception) {
            _courseModules.postValue(Lce.error(e.toString()))
        }
    }

    private suspend fun appendProgressInfoToModules(
        courseId: String,
        courseModules: List<Module>
    ): List<Module> {
        val moduleProgress = learningRepository.getCourseModulesProgress(courseId)

        courseModules.forEach { module ->
            val progressItem = moduleProgress.find {
                module.id == it.moduleId
            }

            if (progressItem != null) {
                module.lessonsCompleted = progressItem.lessonsCompleted
                module.totalLessons = progressItem.lessonsTotal
                module.moduleStartDate = progressItem.moduleStartDate
                module.moduleCompletionDate = progressItem.moduleCompletionDate
                module.ongoing = progressItem.ongoing
                module.completed = progressItem.completed
            }
        }
        return courseModules
    }


    //Getting Course Lessons

    private val _courseLessons = MutableLiveData<Lce<List<CourseContent>>>()
    val courseLessons: LiveData<Lce<List<CourseContent>>> = _courseLessons

    fun getCourseLessonsAndAssessments(
        courseId: String,
        moduleId: String
    ) = viewModelScope.launch {

        if (mCurrentModuleId == moduleId && currentLessons != null) {
            _courseLessons.postValue(Lce.content(currentLessons!!))
            _courseAssessments.postValue(Lce.content(currentAssessments!!))
            return@launch
        }

        _courseLessons.postValue(Lce.loading())
        _courseAssessments.postValue(Lce.loading())

        try {
            val courseLessons = learningRepository.getModuleLessons(
                courseId = courseId,
                moduleId = moduleId
            )

            mCurrentModuleId = moduleId

            if (mCurrentModulesProgressData != null) {
                currentLessons = appendLessonProgressInfo(courseId, moduleId, courseLessons)

                _courseLessons.postValue(Lce.content(courseLessons))

                val assessments = courseLessons.filter {
                    it.type == CourseContent.TYPE_ASSESSMENT
                }
                currentAssessments = assessments
                _courseAssessments.postValue(Lce.content(assessments))

            } else {
                currentLessons = courseLessons
                startWatchingForUpdates(courseId, moduleId)
            }
        } catch (e: Exception) {
            _courseLessons.postValue(Lce.error(e.toString()))
        }
    }

    private fun startWatchingForUpdates(courseId: String, moduleId: String) {
        learningRepository.courseModuleProgressInfo(courseId)
            .addSnapshotListener { querySnap, error ->

                if (querySnap != null) {

                    mCurrentModulesProgressData = querySnap.documents.map {
                        it.toObject(ModuleProgress::class.java)!!
                    }.filter {
                        it.isActive
                    }

                    if (currentModules != null && mCurrentModulesProgressData != null) {
                        currentModules!!.forEach { module ->
                            val progressItem = mCurrentModulesProgressData!!.find {
                                module.id == it.moduleId
                            }

                            if (progressItem != null) {
                                module.lessonsCompleted = progressItem.lessonsCompleted
                                module.totalLessons = progressItem.lessonsTotal
                                module.moduleStartDate = progressItem.moduleStartDate
                                module.moduleCompletionDate = progressItem.moduleCompletionDate
                                module.ongoing = progressItem.ongoing
                                module.completed = progressItem.completed
                            }
                        }

                        _courseModules.postValue(Lce.content(currentModules!!))
                    }

                    if (currentLessons != null) {
                        currentLessons =
                            appendLessonProgressInfo(courseId, moduleId, currentLessons!!)

                        _courseLessons.postValue(Lce.content(currentLessons!!))

                        val assessments = currentLessons!!.filter {
                            it.type == CourseContent.TYPE_ASSESSMENT
                        }.sortedBy {
                            it.priority
                        }
                        currentAssessments = assessments
                        _courseAssessments.postValue(Lce.content(assessments))
                    }
                }

                if (currentAssessments != null) {
                    currentAssessments =
                        appendLessonProgressInfo(courseId, moduleId, currentAssessments!!)
                    _courseAssessments.postValue(Lce.content(currentAssessments!!))
                }
            }
    }

    private fun appendLessonProgressInfo(
        courseId: String,
        moduleId: String,
        courseLessons: List<CourseContent>
    ): List<CourseContent> {
        val moduleProgress = mCurrentModulesProgressData!!.find {
            it.moduleId == mCurrentModuleId
        }

        if (moduleProgress != null) {

            val lessons = courseLessons
                .sortedBy {
                    it.priority
                }
            lessons.forEach { lesson ->

                val progressItem = moduleProgress.lessonsProgress.find {
                    lesson.id == it.lessonId
                }

                if (progressItem != null) {
                    lesson.progressTrackingId = progressItem.progressTrackingId
                    lesson.lessonTotalLength = progressItem.lessonTotalLength
                    lesson.completionProgress = progressItem.completionProgress
                    lesson.currentlyOnGoing = progressItem.ongoing
                    lesson.completed = progressItem.completed
                }
            }

            return lessons
        }
        return courseLessons
    }


    //Getting Course Assessments

    private val _courseAssessments = MutableLiveData<Lce<List<CourseContent>>>()
    val courseAssessments: LiveData<Lce<List<CourseContent>>> = _courseAssessments

    var currentAssessments: List<CourseContent>? = null

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

            mCurrentModuleId = moduleId

            if (mCurrentModulesProgressData != null) {
                currentAssessments = appendLessonProgressInfo(courseId, moduleId, courseAssessments)

                _courseAssessments.postValue(Lce.content(courseAssessments))
            } else {
                currentAssessments = courseAssessments
                startWatchingForUpdates(courseId, moduleId)
            }
        } catch (e: Exception) {
            _courseAssessments.postValue(Lce.error(e.toString()))
        }
    }
}