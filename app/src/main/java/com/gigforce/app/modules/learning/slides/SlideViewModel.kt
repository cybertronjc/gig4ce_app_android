package com.gigforce.app.modules.learning.slides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.LearningRepository
import com.gigforce.app.modules.learning.data.SlideContent
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SlideViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private val slides = listOf(
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_IMAGE_WITH_TEXT,
            title = "How to make Coffee",
            description = "As for the best coffee-to-water ratio, a general guideline is a 1:17 ratio of coffee to water — or in other words, for every 1 gram of coffee, use 17 grams of water (use a digital scale to measure your ground coffee for better results). With these tips, you'll soon have a great cup of coffee no matter which brewing method you choose!",
            image = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig_images%2Findustry1.jpg?alt=media&token=5aeefc03-9cb8-4b4d-a283-6bacc35f165d"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_IMAGE_WITH_TEXT,
            title = "How to Capture Photos",
            description = "Pour coarse ground coffee into the carafe, then fill it with boiling water before giving it a quick stir. After waiting for four minutes for the coffee to steep, put the lid on and slowly press the plunger down to separate the grounds from the coffee.",
            image = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_DOS_DONTS,
            doText = "continue to add boiling water, pouring in a circular motion as to wet all the grounds evenly until you reach your desired final brew weight",
            doImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Flearning1.jpg?alt=media&token=856f1858-3f79-4d9b-b1a7-1952c3940019",
            dontText = "Place the Chemex filter inside the flask, lining up the side with multiple folds with the spout.",
            dontImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_DOS_DONTS,
            doText = "the pour over method involves pouring hot water through coffee grounds",
            doImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Flearning1.jpg?alt=media&token=856f1858-3f79-4d9b-b1a7-1952c3940019",
            dontText = "Placed in a filter to allow the coffee to drip slowly into a vessel",
            dontImage = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_images%2Findustry3.jpg?alt=media&token=4fd6848c-6507-448f-b593-bbef1028101c"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_ASSESSMENT,
            assessmentId = "323",
            title = "Retail Sales Associate",
            description = "Placed in a filter to allow the coffee to drip slowly into a vessel"
        ),
        SlideContent(
            slideId= "021",
            lessonId = "34",
            slideNo = 1,
            type = SlideContent.TYPE_VIDEO_WITH_TEXT,
            videoPath = "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_videos%2FM1L2_2.mp4?alt=media&token=7cbacffa-6d28-431d-bf0f-04ce388af935",
            title = "How to approach customer",
            description = "continue to add boiling water, pouring in a circular motion as to wet all the grounds evenly until you reach your desired final brew weight"
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
        delay(300)
        _slideContent.value = Lce.content(slides)

    }
}