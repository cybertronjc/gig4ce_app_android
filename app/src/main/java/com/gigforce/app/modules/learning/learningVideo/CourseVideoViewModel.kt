package com.gigforce.app.modules.learning.learningVideo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.models.Module
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class CourseVideoViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mCourseContent: CourseContent? = null

    private val _videoDetails = MutableLiveData<Lce<CourseContent>>()
    val videoDetails: LiveData<Lce<CourseContent>> = _videoDetails

    fun getVideoDetails(
        lessonId: String
    ) = viewModelScope.launch {
        _videoDetails.postValue(Lce.loading())

        try {
            val videoLessons = learningRepository.getVideoDetails(
                lessonId = lessonId
            )

            if (videoLessons.isEmpty())
                _videoDetails.postValue(Lce.error("No Video Lesson Found"))
            else
                _videoDetails.postValue(Lce.content(videoLessons.first()))
        } catch (e: Exception) {
            _videoDetails.postValue(Lce.error(e.toString()))
        }
    }
}