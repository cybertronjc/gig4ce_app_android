package com.gigforce.app.modules.learning

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.models.Module
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LearningRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    suspend fun getUserCourses(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_COURSE)
            .get()
            .addOnSuccessListener {querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }.filter {
                        it.isActive
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    suspend fun getRoleBasedCourses(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_COURSE)
            .get()
            .addOnSuccessListener {querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }.filter {
                        it.isActive
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    suspend fun getAllCourses(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_COURSE)
            .get()
            .addOnSuccessListener {querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }.filter {
                        it.isActive
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getCourseDetails(courseId : String): Course = suspendCoroutine { cont ->
        getCollectionReference()
            .document(courseId)
            .get()
            .addOnSuccessListener {docSnap ->

                val course = docSnap.toObject(Course::class.java)!!
                course.id = docSnap.id
                cont.resume(course)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getModuleLessons(
        courseId : String,
        moduleId : String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(COURSE_ID, courseId)
            .whereEqualTo(MODULE_ID, moduleId)
            .whereEqualTo(TYPE, TYPE_LESSON)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }


    suspend fun getModuleAssessments(
        courseId : String,
        moduleId : String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(COURSE_ID, courseId)
            .whereEqualTo(MODULE_ID, moduleId)
            .whereEqualTo(TYPE, TYPE_LESSON)
            .whereEqualTo(LESSON_TYPE, LESSON_TYPE_ASSESSMENT)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }


    suspend fun getVideoDetails(
        lessonId : String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
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
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getAssessmentsFromAllCourses(): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_LESSON)
            .whereEqualTo(LESSON_TYPE, LESSON_TYPE_ASSESSMENT)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getModulesFromAllCourses(): List<Module> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_MODULE)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val modules = it.toObject(Module::class.java)!!
                        modules.id = it.id
                        modules
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }



    suspend fun getModules(courseId : String) : List<Module> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_MODULE)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val modules = it.toObject(Module::class.java)!!
                        modules.id = it.id
                        modules
                    }.filter {
                        it.isActive
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    suspend fun getModulesWithCourseContent(courseId : String) : List<Module> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_LESSON)
            .get()
            .addOnSuccessListener {

               TODO("not implemented")
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    companion object {
        private const val COLLECTION_NAME = "Course_blocks"

        private const val TYPE = "type"
        private const val TOPIC_TYPE = "topictype"
        private const val LESSON_TYPE = "lesson_type"
        private const val COURSE_ID = "course_id"
        private const val MODULE_ID = "module_id"
        private const val LESSON_ID = "lesson_id"

        private const val TYPE_COURSE = "course"
        private const val TYPE_MODULE = "module"
        private const val TYPE_LESSON = "lesson"
        private const val TYPE_TOPIC = "topic"

        private const val TOPIC_TYPE_VIDEO_WITH_TEXT = "video_with_text"

        private const val LESSON_TYPE_VIDEO = "video"
        private const val LESSON_TYPE_SLIDES = "slides"
        private const val LESSON_TYPE_ASSESSMENT = "assessment"
    }
}