package com.gigforce.client_activation.client_activation

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

                .whereEqualTo(LearningRepository.LESSON_ID, lessonId)
                .whereEqualTo(LearningRepository.TYPE, LearningRepository.TYPE_TOPIC)
                .whereEqualTo(LearningRepository.TOPIC_TYPE, LearningRepository.TOPIC_TYPE_VIDEO_WITH_TEXT)
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
}