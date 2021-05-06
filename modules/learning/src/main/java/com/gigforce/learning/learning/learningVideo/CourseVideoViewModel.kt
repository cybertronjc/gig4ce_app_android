package com.gigforce.learning.learning.learningVideo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.learning.learning.LearningRepository
import com.gigforce.core.datamodels.learning.CourseContent
//import com.gigforce.app.utils.Lce
//import com.gigforce.app.utils.Lse
import com.gigforce.core.SingleLiveEvent2
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
//import com.gigforce.core.utils.Lce
import kotlinx.coroutines.launch

sealed class VideoSaveState {

    object VideoStateSaved : VideoSaveState()
    object VideoMarkedComplete : VideoSaveState()
}

class CourseVideoViewModel constructor(
        private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var videoLesson: CourseContent? = null
    val currentVideoLesson: CourseContent? get() = videoLesson

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
                videoLesson = videoLessons.first()

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

                    videoLesson?.currentlyOnGoing = videoProgress.ongoing
                    videoLesson?.completed = videoProgress.completed
                    videoLesson?.completionProgress = videoProgress.completionProgress
                    videoLesson?.lessonTotalLength = videoProgress.lessonTotalLength
                }
                _videoDetails.postValue(Lce.content(videoLesson!!))

            }
        } catch (e: Exception) {
            _videoDetails.postValue(Lce.error(e.toString()))
        }
    }

    private val _videoSaveState = MutableLiveData<Lce<VideoSaveState>>()
    val videoSaveState: LiveData<Lce<VideoSaveState>> = _videoSaveState

    private val _openNextDestination =
        SingleLiveEvent2<CourseContent>()
    val openNextDestination: LiveData<CourseContent> = _openNextDestination

    fun markVideoAsComplete(moduleId: String, lessonId: String) = viewModelScope.launch {

        _videoSaveState.value = Lce.loading()
        try {

            val nextLesson =
                    learningRepository.markCurrentLessonAsComplete(moduleId, lessonId,true)
            _videoSaveState.value = Lce.content(VideoSaveState.VideoMarkedComplete)

            _openNextDestination.value = nextLesson
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
                    if (it.lessonId == lessonId && playBackPosition > it.completionProgress) {
                        it.ongoing = true
                        it.completed = it.completed
                        it.completionProgress = if (playBackPosition >= fullVideoLength) 0L else playBackPosition
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

    private val _saveLessonFeedbackState = MutableLiveData<Lse>()
    val saveLessonFeedbackState: LiveData<Lse> = _saveLessonFeedbackState

    fun saveVideoFeedback(
            lessonId: String,
            lessonRating: Float? = null,
            explanation: Boolean? = null,
            completeness: Boolean? = null,
            easyToUnderStand: Boolean? = null,
            videoQuality: Boolean? = null,
            soundQuality: Boolean? = null
    ) = viewModelScope.launch {

        _saveLessonFeedbackState.value = Lse.loading()
        try {

            learningRepository.recordLessonFeedback(
                    lessonId,
                    lessonRating,
                    explanation,
                    completeness,
                    easyToUnderStand,
                    videoQuality,
                    soundQuality
            )
            _saveLessonFeedbackState.value = Lse.success()
        } catch (e: Exception) {
            _saveLessonFeedbackState.value = Lse.error(e.message!!)
        }
    }
}