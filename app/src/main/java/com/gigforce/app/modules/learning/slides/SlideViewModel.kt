package com.gigforce.app.modules.learning.slides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.modules.SlideContent
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch


class SlideViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    val slides = listOf(
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_IMAGE_WITH_TEXT,
            title = "Some title",
            description = "Some long description",
            image = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Flearning1.jpg?alt=media&token=856f1858-3f79-4d9b-b1a7-1952c3940019"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_IMAGE_WITH_TEXT,
            title = "Some title 2",
            description = "Some long description 2",
            image = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_DOS_DONTS,
            doText = "Do Text title",
            doImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Flearning1.jpg?alt=media&token=856f1858-3f79-4d9b-b1a7-1952c3940019",
            dontText = "Dont Text title",
            dontImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_DOS_DONTS,
            doText = "Do Text title 2",
            doImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Flearning1.jpg?alt=media&token=856f1858-3f79-4d9b-b1a7-1952c3940019",
            dontText = "Dont Text title 2",
            dontImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        )
    )

    private val _slideContent = MutableLiveData<Lce<List<SlideContent>>>()
    val slideContent: LiveData<Lce<List<SlideContent>>> = _slideContent

    fun getSlideContent(
        courseId: String,
        moduleId: String,
        lessonId: String
    ) = viewModelScope.launch {
        _slideContent.postValue(Lce.loading())



    }
}