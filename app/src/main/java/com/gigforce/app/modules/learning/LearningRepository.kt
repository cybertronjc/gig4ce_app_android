package com.gigforce.app.modules.learning

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.learning.models.*
import com.gigforce.app.modules.learning.models.progress.CourseProgress
import com.gigforce.app.modules.learning.models.progress.LessonProgress
import com.gigforce.app.modules.learning.models.progress.ModuleProgress
import com.gigforce.app.modules.learning.models.progress.ProgressConstants
import com.gigforce.app.utils.addOrThrow
import com.gigforce.app.utils.getOrThrow
import com.gigforce.app.utils.setOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LearningRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    fun courseModuleProgressInfo(courseId: String): Query {
        return db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("course_id", courseId)
            .whereEqualTo(TYPE, TYPE_MODULE)
    }

    suspend fun getUserCourses(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_COURSE)
            .get()
            .addOnSuccessListener { querySnap ->

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

    suspend fun getCourseProgress(courseId: String): CourseProgress {

        val querySnap = db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("course_id", courseId)
            .getOrThrow()

        if (querySnap.isEmpty) {
            //No data in Progress DB
            addInitalProgressDataForCourse(courseId)
            return CourseProgress(
                uid = getUID(),
                courseId = courseId,
                courseStartDate = Timestamp.now(),
                courseCompletionDate = null,
                ongoing = true,
                completed = false
            )
        } else {
            return querySnap.documents.map {
                it.toObject(CourseProgress::class.java)!!
            }.first()
        }
    }

    suspend fun getCourseModulesProgress(courseId: String): List<ModuleProgress> {

        val querySnap = db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("course_id", courseId)
            .whereEqualTo("type", ProgressConstants.TYPE_MODULE)
            .getOrThrow()

        if (querySnap.isEmpty) {
            return emptyList()
        } else {
            return querySnap.documents.map {
                it.toObject(ModuleProgress::class.java)!!
            }
        }
    }

    suspend fun getLessonsProgress(courseId: String, moduleId: String): List<LessonProgress> {

        val querySnap = db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("course_id", courseId)
            .whereEqualTo("module_id", moduleId)
            .whereEqualTo("type", ProgressConstants.TYPE_LESSON)
            .getOrThrow()

        if (querySnap.isEmpty) {
            return emptyList()
        } else {
            return querySnap.documents.map {
                val lessonProgress = it.toObject(LessonProgress::class.java)!!
                lessonProgress.progressTrackingId = it.id
                lessonProgress
            }
        }
    }

    suspend fun getLessonProgress(progressTrackingId: String): LessonProgress {
        val docRef = db.collection(COURSE_PROGRESS_NAME)
            .document(progressTrackingId)
            .getOrThrow()

        val lessonProgress = docRef.toObject(LessonProgress::class.java)
            ?: throw IllegalArgumentException("unable to parse db learning progress model")
        lessonProgress.progressTrackingId = docRef.id
        return lessonProgress
    }

    suspend fun updateLessonProgress(progressTrackingId: String, lessonProgress: LessonProgress) {
        db.collection(COURSE_PROGRESS_NAME)
            .document(progressTrackingId)
            .setOrThrow(lessonProgress)
    }

    suspend fun updateModuleProgress(progressTrackingId: String, moduleProgress: ModuleProgress) {
        db.collection(COURSE_PROGRESS_NAME)
            .document(progressTrackingId)
            .setOrThrow(moduleProgress)
    }

    suspend fun markCurrentLessonAsCompleteAndEnableNextOne(
        moduleId: String
    ): CourseContent? {
        val moduleProgress = getModuleProgress(moduleId)
        var nextLessonProgress: LessonProgress? = null

        if (moduleProgress != null) {
            var currentLessonIndex = -10

            val lessons = moduleProgress.lessonsProgress.sortedBy { it.priority }

            for (i in lessons.indices) {
                if (lessons[i].ongoing) {
                    currentLessonIndex = i

                    lessons[i].apply {
                        ongoing = false
                        completed = true
                        lessonCompletionDate = Timestamp.now()
                    }
                }

                if (i < lessons.size) {
                    if (currentLessonIndex + 1 == i) {
                        moduleProgress.lessonsProgress[i].apply {
                            ongoing = true
                            completed = false
                            lessonCompletionDate = null
                            lessonStartDate = Timestamp.now()
                        }

                        nextLessonProgress = moduleProgress.lessonsProgress[i]
                    }
                }
            }

            updateModuleProgress(moduleProgress.progressId, moduleProgress)
        }

        return if (nextLessonProgress == null)
            null
        else
            getLessonInfo(nextLessonProgress.lessonId)
    }

    private suspend fun getLessonInfo(lessonId: String) = suspendCoroutine<CourseContent> { cont ->
        getCollectionReference()
            .document(lessonId)
            .get()
            .addOnSuccessListener { docRef ->
                val courseContent = docRef.toObject(CourseContent::class.java)!!
                courseContent.id = docRef.id
                cont.resume(courseContent)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }

    }


    private suspend fun addInitalProgressDataForCourse(courseId: String) {

        val progress = CourseProgress(
            uid = getUID(),
            courseId = courseId,
            courseStartDate = Timestamp.now(),
            courseCompletionDate = null,
            ongoing = true,
            completed = false
        )
        db.collection(COURSE_PROGRESS_NAME).addOrThrow(progress)

        val modules = getModules(courseId)
        modules.map {

            val lessonProgress =
                getModuleLessons(courseId, it.id).sortedBy { courseContent ->
                    courseContent.priority
                }.map { cc ->
                    LessonProgress(
                        uid = getUID(),
                        courseId = courseId,
                        moduleId = cc.moduleId,
                        lessonId = cc.id,
                        lessonStartDate = Timestamp.now(),
                        lessonCompletionDate = null,
                        ongoing = false,
                        priority = cc.priority,
                        completed = false,
                        lessonType = cc.type
                    )
                }

            if (lessonProgress.isNotEmpty()) {
                lessonProgress.get(0).ongoing = true
            }

            ModuleProgress(
                uid = getUID(),
                courseId = courseId,
                moduleId = it.id,
                moduleStartDate = Timestamp.now(),
                moduleCompletionDate = null,
                ongoing = false,
                completed = false,
                lessonsProgress = lessonProgress
            )
        }.forEach {
            db.collection(COURSE_PROGRESS_NAME).addOrThrow(it)
        }
    }

    suspend fun getRoleBasedCourses(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(TYPE, TYPE_COURSE)
            .get()
            .addOnSuccessListener { querySnap ->

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
            .addOnSuccessListener { querySnap ->

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

    suspend fun getCourseDetails(courseId: String): Course = suspendCoroutine { cont ->
        getCollectionReference()
            .document(courseId)
            .get()
            .addOnSuccessListener { docSnap ->

                val course = docSnap.toObject(Course::class.java)!!
                course.id = docSnap.id
                cont.resume(course)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getModuleLessons(
        courseId: String,
        moduleId: String
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
        courseId: String,
        moduleId: String
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
        lessonId: String
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

    suspend fun getSlideContent(
        lessonId: String
    ): List<SlideContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(LESSON_ID, lessonId)
            .whereEqualTo(TYPE, TYPE_TOPIC)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val videoDetails = it.toObject(SlideContentRemote::class.java)!!
                        videoDetails.id = it.id
                        videoDetails
                    }
                    .filter {
                        it.isActive
                    }
                    .map {
                        mapToSlideContent(it)
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    private fun mapToSlideContent(it: SlideContentRemote): SlideContent {
        return SlideContent(
            slideId = it.id,
            lessonId = it.lessonId,
            image = it.coverPicture,
            isActive = it.isActive,
            type = it.type,
            assessmentId = it.lessonId,
            videoPath = it.videoUrl
        )
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


    suspend fun getModules(courseId: String): List<Module> = suspendCoroutine { cont ->
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

    suspend fun getModulesWithCourseContent(courseId: String): List<Module> =
        suspendCoroutine { cont ->
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

    suspend fun getModuleProgress(moduleId: String): ModuleProgress? {
        val querySnap = db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("module_id", moduleId)
            .whereEqualTo("type", ProgressConstants.TYPE_MODULE)
            .getOrThrow()

        if (querySnap.isEmpty) {
            return null
        } else {
            val moduleProgress = querySnap.documents.map {
                val module = it.toObject(ModuleProgress::class.java)!!
                module.progressId = it.id
                module
            }

            if (moduleProgress.isEmpty())
                return null
            else
                return moduleProgress[0]
        }


    }

    companion object {
        private const val COLLECTION_NAME = "Course_blocks"
        private const val COURSE_PROGRESS_NAME = "Course_Progress"

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