package com.gigforce.client_activation.client_activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.core.utils.Lce
import kotlinx.coroutines.launch

class PlayVideoDialogUrlViewmodel : ViewModel() {
    private val _videoDetails = MutableLiveData<Lce<CourseContent>>()
    val videoDetails: LiveData<Lce<CourseContent>> = _videoDetails
    private val learningRepository: PlayDialogUrlRepository = PlayDialogUrlRepository()
    private var videoLesson: CourseContent? = null
    val currentVideoLesson: CourseContent? get() = videoLesson
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
                _videoDetails.postValue(Lce.content(videoLessons.first()))

            }
        } catch (e: Exception) {
            _videoDetails.postValue(Lce.error(e.toString()))
        }
    }

}