package com.gigforce.client_activation.client_activation

import com.gigforce.core.datamodels.learning.CourseContent
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PlayDialogUrlRepository {

    suspend fun getVideoDetails(
            lessonId: String
    ): List<CourseContent> {

        return getVideoDetailsC(lessonId).filter {
            it.isActive
        }
    }



    private suspend fun getVideoDetailsC(
            lessonId: String
    ): List<CourseContent> = suspendCoroutine { cont ->
        FirebaseFirestore.getInstance().collection("Course_blocks")

                .whereEqualTo(LESSON_ID, lessonId)
                .whereEqualTo(TYPE, TYPE_TOPIC)
                .whereEqualTo(TOPIC_TYPE, TOPIC_TYPE_VIDEO_WITH_TEXT)
                .get()
                .addOnSuccessListener { querySnap ->

                    val modules = querySnap.documents
                            .map {
                                val videoDetails = it.toObject(CourseContent::class.java)!!
                                videoDetails.id = it.id
                                videoDetails
                            }
                    cont.resume(modules)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }
    companion object {

        const val TYPE = "type"
        const val TOPIC_TYPE = "topictype"
        const val LESSON_TYPE = "lesson_type"
        const val COURSE_ID = "course_id"
        const val MODULE_ID = "module_id"
        const val LESSON_ID = "lesson_id"

        const val TYPE_COURSE = "course"
        const val TYPE_MODULE = "module"
        const val TYPE_LESSON = "lesson"
        const val TYPE_TOPIC = "topic"

        const val TOPIC_TYPE_VIDEO_WITH_TEXT = "video_with_text"

        const val LESSON_TYPE_VIDEO = "video"
        const val LESSON_TYPE_SLIDES = "slides"
        const val LESSON_TYPE_ASSESSMENT = "assessment"
    }
}