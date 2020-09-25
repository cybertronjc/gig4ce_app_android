package com.gigforce.app.modules.learning.learningVideo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.SingleLiveEvent2
import kotlinx.coroutines.launch

sealed class VideoSaveState {

    object VideoStateSaved : VideoSaveState()
    object VideoMarkedComplete : VideoSaveState()
}

class CourseVideoViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mCourseContent: CourseContent? = null

    private val _videoDetails = MutableLiveData<Lce<CourseContent>>()
    val videoDetails: LiveData<Lce<CourseContent>> = _videoDetails

    fun getVideoDetails(
        moduleId: String,
        lessonId: String
    ) = viewModelScope.launch {
        _videoDetails.postValue(Lce.loading())

        try {
            val videoLessons = learningRepository.getVideoDetails(
                lessonId = lessonId
            )

            if (videoLessons.isEmpty())
                _videoDetails.postValue(Lce.error("No Video Lesson Found"))
            else {
                val moduleProgress = learningRepository.getModuleProgress(moduleId)
                val videoLesson = videoLessons.first()

                val videoProgress = moduleProgress?.lessonsProgress?.find {
                    it.lessonId == lessonId
                }

                if (videoProgress != null) {
                    if (!videoProgress.completed && !videoProgress.ongoing) {

                        moduleProgress.lessonsProgress.forEach {
                            if (it.lessonId == lessonId) {
                                it.ongoing = true
                            }
                        }

                        learningRepository.updateModuleProgress(
                            moduleProgress.progressId,
                            moduleProgress
                        )
                    }

                    videoLesson.currentlyOnGoing = videoProgress.ongoing
                    videoLesson.completed = videoProgress.completed
                    videoLesson.completionProgress = videoProgress.completionProgress
                    videoLesson.lessonTotalLength = videoProgress.lessonTotalLength
                }
                _videoDetails.postValue(Lce.content(videoLesson))

            }
        } catch (e: Exception) {
            _videoDetails.postValue(Lce.error(e.toString()))
        }
    }

    private val _videoSaveState = MutableLiveData<Lce<VideoSaveState>>()
    val videoSaveState: LiveData<Lce<VideoSaveState>> = _videoSaveState

    private val _openNextDestination = SingleLiveEvent2<CourseContent>()
    val openNextDestination: LiveData<CourseContent> = _openNextDestination

    fun markVideoAsComplete(moduleId: String, lessonId: String) = viewModelScope.launch {

        _videoSaveState.value = Lce.loading()
        try {

            val nextLesson =
                learningRepository.markCurrentLessonAsCompleteAndEnableNextOne(moduleId)
            _videoSaveState.value = Lce.content(VideoSaveState.VideoMarkedComplete)

            if (nextLesson != null)
                _openNextDestination.value = nextLesson
            else
                _openNextDestination.value = null

        } catch (e: Exception) {
            _videoSaveState.value = Lce.error(e.message!!)
        }
    }

    fun savedVideoState(
        moduleId: String,
        lessonId: String,
        playBackPosition: Long,
        fullVideoLength: Long
    ) = viewModelScope.launch {

        _videoSaveState.value = Lce.loading()
        try {
            val moduleProgress = learningRepository.getModuleProgress(moduleId)

            if (moduleProgress != null) {
                moduleProgress.lessonsProgress.forEach {
                    if (it.lessonId == lessonId) {
                        it.ongoing = true
                        it.completed = it.completed
                        it.completionProgress = playBackPosition
                        it.lessonTotalLength = fullVideoLength
                        it.lessonCompletionDate = null
                    }
                }
                learningRepository.updateModuleProgress(moduleProgress.progressId, moduleProgress)
            }

            _videoSaveState.value = Lce.content(VideoSaveState.VideoStateSaved)
        } catch (e: Exception) {
            _videoSaveState.value = Lce.error(e.message!!)
        }
    }
}