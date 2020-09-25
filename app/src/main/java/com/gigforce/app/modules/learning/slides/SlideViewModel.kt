package com.gigforce.app.modules.learning.slides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.models.SlideContent
import com.gigforce.app.modules.learning.models.progress.SlideProgress
import com.gigforce.app.utils.Lce
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

data class SlideInfoAndDirection(
    val slideContent: List<SlideContent>,
    val activeSlideIndex: Int = 0
)

class SlideViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private val _slideContent = MutableLiveData<Lce<SlideInfoAndDirection>>()
    val slideContent: LiveData<Lce<SlideInfoAndDirection>> = _slideContent

    fun getSlideContent(
        moduleId: String,
        lessonId: String
    ) = viewModelScope.launch {
        _slideContent.postValue(Lce.loading())

        val slideContent = learningRepository.getSlideContent(lessonId)
        val slideProgressInfo = getOrCreateSlideProgressData(moduleId, slideContent)

        slideContent.forEach {

            val progressInfo = slideProgressInfo.find { progress ->
                it.slideId == progress.slideId
            }

            if (progressInfo != null) {
                it.completed = progressInfo.completed
                it.totalLength = progressInfo.totalLength
                it.completionProgress = progressInfo.completionProgress
            }
        }

        var pos = 0
        for (i in slideContent.indices) {
            if (!slideContent[i].completed) {
                pos = i
                break
            }
        }

        _slideContent.value = Lce.content(
            SlideInfoAndDirection(
                slideContent = slideContent,
                activeSlideIndex = pos
            )
        )

    }

    private suspend fun getOrCreateSlideProgressData(
        moduleId: String,
        slideContent: List<SlideContent>
    ): List<SlideProgress> {
        val moduleProgressInfo = learningRepository.getModuleProgress(moduleId)

        if (moduleProgressInfo!!.slidesProgress.isEmpty()) {

            val slideProgress = slideContent.map {
                SlideProgress(
                    progressId = "",
                    slideId = it.slideId,
                    slideStartDate = Timestamp.now(),
                    slideCompletionDate = null,
                    completed = false
                )
            }

            moduleProgressInfo.slidesProgress = slideProgress
            learningRepository.updateModuleProgress(
                moduleProgressInfo.progressId,
                moduleProgressInfo
            )
            return moduleProgressInfo.slidesProgress
        } else {
            return moduleProgressInfo.slidesProgress
        }
    }
}