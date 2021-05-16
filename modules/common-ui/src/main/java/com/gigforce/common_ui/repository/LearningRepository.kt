package com.gigforce.common_ui.repository

//import com.gigforce.common_ui.viewdatamodels.models.*
import com.gigforce.common_ui.datamodels.CourseProgress
import com.gigforce.common_ui.datamodels.datamodels.UserInterestsAndRolesDM
import com.gigforce.common_ui.repository.repo.LearningDataRepository
import com.gigforce.common_ui.viewdatamodels.models.LessonFeedback
import com.gigforce.common_ui.viewdatamodels.models.Module
import com.gigforce.common_ui.viewdatamodels.models.SlideContent
import com.gigforce.common_ui.viewdatamodels.models.SlideContentRemote
import com.gigforce.common_ui.viewdatamodels.models.progress.CourseMapping
import com.gigforce.common_ui.viewdatamodels.models.progress.ModuleProgress
import com.gigforce.common_ui.viewdatamodels.models.progress.ProgressConstants
//import com.gigforce.common_ui.viewdatamodels.models.progress.ModuleProgress
//import com.gigforce.common_ui.viewdatamodels.models.progress.ProgressConstants
import com.gigforce.core.datamodels.learning.LessonProgress
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.core.utils.EventLogs.addOrThrow
import com.gigforce.core.utils.EventLogs.getOrThrow
import com.gigforce.core.utils.EventLogs.setOrThrow
//import com.gigforce.learning.datamodels.UserInterestsAndRolesDM
//import com.gigforce.learning.repo.LearningDataRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LearningRepository : BaseFirestoreDBRepository() {
    private val profileFirebaseRepository = LearningDataRepository()
    private var mProfile: UserInterestsAndRolesDM? = null
    override fun getCollectionName(): String =
        COLLECTION_NAME

    fun courseModuleProgressInfo(courseId: String): Query {
        return db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("course_id", courseId)
            .whereEqualTo("uid", getUID())
            .whereEqualTo(
                TYPE,
                TYPE_MODULE
            )
    }

    fun courseProgressInfo(): Query {
        return db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
    }

    suspend fun getUserCourses(): List<Course> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }
        return getUserCoursesC().filter {
            it.isActive && doesCourseFullFillsCondition(it)
        }.sortedBy {
            it.priority
        }
    }

    private suspend fun getUserCoursesC(): List<Course> = suspendCoroutine { cont ->

        getCollectionReference()
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }


    private suspend fun getCourseCompanyMappings(courseId: String): List<CourseMapping> =
        suspendCoroutine { cont ->
            val companies: List<String> = mProfile?.companies?.map {
                it.companyId
            } ?: emptyList()

            if (companies.isEmpty())
                cont.resume(emptyList())
            else {

                db.collection("Course_company_mapping")
                    .whereIn("companyId", companies)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .addOnSuccessListener {

                        val courseMappings = it.documents.map {
                            it.toObject(CourseMapping::class.java)!!
                        }
                        cont.resume(courseMappings)
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }

    private suspend fun getModuleCompanyMappings(moduleId: String): List<CourseMapping> =
        suspendCoroutine { cont ->
            val companies: List<String> = mProfile?.companies?.map {
                it.companyId
            } ?: emptyList()

            if (companies.isEmpty())
                cont.resume(emptyList())
            else {

                db.collection("Course_company_mapping")
                    .whereIn("companyId", companies)
                    .whereEqualTo("moduleId", moduleId)
                    .get()
                    .addOnSuccessListener {

                        val courseMappings = it.documents.map {
                            it.toObject(CourseMapping::class.java)!!
                        }
                        cont.resume(courseMappings)
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }


    private suspend fun getLessonCompanyMappings(lessonId: String): List<CourseMapping> =
        suspendCoroutine { cont ->
            val companies: List<String> = mProfile?.companies?.map {
                it.companyId
            } ?: emptyList()

            if (companies.isEmpty())
                cont.resume(emptyList())
            else {

                db.collection("Course_company_mapping")
                    .whereIn("companyId", companies)
                    .whereEqualTo("lessonId", lessonId)
                    .get()
                    .addOnSuccessListener {

                        val courseMappings = it.documents.map {
                            it.toObject(CourseMapping::class.java)!!
                        }
                        cont.resume(courseMappings)
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }


    private suspend fun getSlideCompanyMappings(slideId: String): List<CourseMapping> =
        suspendCoroutine { cont ->
            val companies: List<String> = mProfile?.companies?.map {
                it.companyId
            } ?: emptyList()

            if (companies.isEmpty())
                cont.resume(emptyList())
            else {

                db.collection("Course_company_mapping")
                    .whereIn("companyId", companies)
                    .whereEqualTo("slideId", slideId)
                    .get()
                    .addOnSuccessListener {

                        val courseMappings = it.documents.map {
                            it.toObject(CourseMapping::class.java)!!
                        }
                        cont.resume(courseMappings)
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }


    private suspend fun doesCourseFullFillsCondition(it: Course): Boolean {
        if (it.isOpened) {
            return true
        } else {

            val courseAndMappings = getCourseCompanyMappings(it.id)

            if (courseAndMappings.isEmpty()) {
                return false
            }

            courseAndMappings.forEach {

                if (it.isopened) {
                    return true
                }

                if (it.userIdsRequired) {
                    val userMatched = it.userUids.contains(getUID())
                    if (userMatched) return true
                }


                if (it.rolesRequired) {

                    if (mProfile?.role_interests != null) {
                        for (role in mProfile!!.role_interests!!) {
                            for (courseRoles in it.roles) {
                                if (courseRoles == role.interestID) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            return false
        }
    }

    suspend fun courseProgressDataGenerated(courseId: String): Boolean {
        return db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("course_id", courseId)
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
            .getOrThrow()
            .isEmpty
            .not()
    }

    suspend fun getCourseProgress(courseId: String): CourseProgress {

        val querySnap = db.collection(COURSE_PROGRESS_NAME)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("course_id", courseId)
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
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
                val courseProgress = it.toObject(CourseProgress::class.java)!!
                courseProgress.progressId = it.id
                courseProgress
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
                val moduleProgress = it.toObject(ModuleProgress::class.java)!!
                moduleProgress.progressId = it.id
                moduleProgress
            }
        }
    }

//    suspend fun getLessonsProgress(courseId: String, moduleId: String): List<LessonProgress> {
//
//        val querySnap = db.collection(COURSE_PROGRESS_NAME)
//            .whereEqualTo("uid", getUID())
//            .whereEqualTo("course_id", courseId)
//            .whereEqualTo("module_id", moduleId)
//            .whereEqualTo("type", ProgressConstants.TYPE_LESSON)
//            .getOrThrow()
//
//        if (querySnap.isEmpty) {
//            return emptyList()
//        } else {
//            return querySnap.documents.map {
//                val lessonProgress = it.toObject(LessonProgress::class.java)!!
//                lessonProgress.progressTrackingId = it.id
//                lessonProgress
//            }
//        }
//    }

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

    suspend fun updateCourseProgress(progressTrackingId: String, courseProgress: CourseProgress) {
        db.collection(COURSE_PROGRESS_NAME)
            .document(progressTrackingId)
            .setOrThrow(courseProgress)
    }


    suspend fun markCurrentLessonAsComplete(
        moduleId: String,
        lessonId: String,
        isPassed:Boolean?
    ): CourseContent? {

        val moduleProgress = getModuleProgress(moduleId) ?: return null
        val lessonProgress = moduleProgress.lessonsProgress.find { it.lessonId == lessonId }

        if (lessonProgress == null) {

            val lesson = getLessonInfo(lessonId)

            //Add lesson Progress
            val lessonProgressList = moduleProgress.lessonsProgress.toMutableList()
            lessonProgressList.add(
                LessonProgress(
                    uid = getUID(),
                    courseId = moduleProgress.courseId,
                    moduleId = moduleId,
                    lessonId = lessonId,
                    lessonStartDate = Timestamp.now(),
                    lessonCompletionDate = null,
                    ongoing = false,
                    priority = lesson.priority,
                    completed = false,
                    lessonType = lesson.type
                )
            )

            moduleProgress.lessonsProgress = lessonProgressList
        }

        val updatedLessonProgressList =
            moduleProgress.lessonsProgress.filter { it.isActive }.sortedBy { it.priority }
        var nextLessonProgress: LessonProgress? = null
        var currentLessonFound = false
        for (i in updatedLessonProgressList.indices) {
            if (updatedLessonProgressList[i].lessonId == lessonId) {
                currentLessonFound = true
            }
            if (currentLessonFound == false && updatedLessonProgressList[i].lessonType == "assessment" && updatedLessonProgressList[i].completed == false) {
                nextLessonProgress = updatedLessonProgressList[i]
            }
        }



        for (i in updatedLessonProgressList.indices) {
            if (updatedLessonProgressList[i].lessonId == lessonId) {

                var _isPassed = false
                isPassed?.let { _isPassed = it }
                updatedLessonProgressList[i].apply {
                    ongoing = false
                    completed = _isPassed
                    completionProgress = 0L
                    lessonCompletionDate = Timestamp.now()
                }
                if (nextLessonProgress == null && i < updatedLessonProgressList.size - 1) {
                    nextLessonProgress = updatedLessonProgressList[i + 1]
                }
                break
            }
        }

        var completedLesson = 0
        var totalLessons = 0

        moduleProgress.lessonsProgress.filter { it.isActive }.forEach {
            totalLessons++
            if (it.completed) completedLesson++
        }

        moduleProgress.lessonsCompleted = completedLesson
        moduleProgress.lessonsTotal = totalLessons

        if (totalLessons != 0)
            moduleProgress.completed = completedLesson == totalLessons

        updateModuleProgress(moduleProgress.progressId, moduleProgress)

        if (moduleProgress.completed) {
            //Check And Update Course Progress
            val courseId = moduleProgress.courseId

            var totalModules = 0
            var completedModules = 0
            getCourseModulesProgress(courseId)
                .filter {
                    it.isActive
                }
                .forEach {
                    if (it.completed)
                        completedModules++

                    totalModules++
                }

            val courseProgress = getCourseProgress(courseId)

            if (totalModules != 0 && totalModules == completedModules) {
                courseProgress.completed = true
                courseProgress.courseCompletionDate = Timestamp.now()
                courseProgress.totalModules = totalModules
                courseProgress.completedModules = completedModules
            } else {
                courseProgress.completed = false
                courseProgress.courseCompletionDate = null
                courseProgress.courseStartDate
                courseProgress.totalModules = totalModules
                courseProgress.completedModules = completedModules
            }

            updateCourseProgress(courseProgress.progressId, courseProgress)
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
                lessonsTotal = lessonProgress.size,
                lessonsProgress = lessonProgress
            )
        }.forEach {
            db.collection(COURSE_PROGRESS_NAME).addOrThrow(it)
        }
    }

    suspend fun getRoleBasedCourses(): List<Course> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getRoleBasedCoursesC().filter {
            it.isActive && doesCourseFullFillsCondition(it)
        }.sortedBy {
            it.priority
        }
    }


    private suspend fun getRoleBasedCoursesC(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getAllCourses(): List<Course> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getAllCoursesC().filter {
            it.isActive && doesCourseFullFillsCondition(it)
        }.sortedBy {
            it.priority
        }
    }

    private suspend fun getAllCoursesC(): List<Course> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
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
    ): List<CourseContent> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getModuleLessonsC(courseId, moduleId).filter {
            it.isActive && doesLessonFullFillsCondition(it)
        }
    }

    suspend fun getModuleLessons(
        moduleId: String
    ): List<CourseContent> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getModuleLessonsC(moduleId).filter {
            it.isActive && doesLessonFullFillsCondition(it)
        }.sortedBy { it.priority }
    }

    private suspend fun getModuleLessonsC(
        courseId: String,
        moduleId: String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(COURSE_ID, courseId)
            .whereEqualTo(MODULE_ID, moduleId)
            .whereEqualTo(
                TYPE,
                TYPE_LESSON
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    private suspend fun getModuleLessonsC(
        moduleId: String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(MODULE_ID, moduleId)
            .whereEqualTo(
                TYPE,
                TYPE_LESSON
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    private suspend fun doesLessonFullFillsCondition(it: CourseContent): Boolean {
        if (it.isOpened) {
            return true
        } else {

            val lessonMapping = getLessonCompanyMappings(it.id)

            if (lessonMapping.isEmpty()) {
                return false
            }

            lessonMapping.forEach {

                if (it.isopened) {
                    return true
                }

                if (it.userIdsRequired) {
                    val userMatched = it.userUids.contains(getUID())
                    if (userMatched) return true
                }


                if (it.rolesRequired) {

                    if (mProfile?.role_interests != null) {
                        for (role in mProfile!!.role_interests!!) {
                            for (courseRoles in it.roles) {
                                if (courseRoles == role.interestID) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            return false
        }
    }


    suspend fun getModuleAssessments(
        courseId: String,
        moduleId: String
    ): List<CourseContent> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getModuleAssessmentsC(courseId, moduleId).filter {
            it.isActive && doesLessonFullFillsCondition(it)
        }
    }

    private suspend fun getModuleAssessmentsC(
        courseId: String,
        moduleId: String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(COURSE_ID, courseId)
            .whereEqualTo(MODULE_ID, moduleId)
            .whereEqualTo(
                TYPE,
                TYPE_LESSON
            )
            .whereEqualTo(
                LESSON_TYPE,
                LESSON_TYPE_ASSESSMENT
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val lesson = it.toObject(CourseContent::class.java)!!
                        lesson.id = it.id
                        lesson
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getVideoDetails(
        lessonId: String
    ): List<CourseContent> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getVideoDetailsC(lessonId).filter {
            it.isActive
        }
    }


    private suspend fun getVideoDetailsC(
        lessonId: String
    ): List<CourseContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(LESSON_ID, lessonId)
            .whereEqualTo(
                TYPE,
                TYPE_TOPIC
            )
            .whereEqualTo(
                TOPIC_TYPE,
                TOPIC_TYPE_VIDEO_WITH_TEXT
            )
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

    private suspend fun doesSlideFullFillsCondition(it: SlideContent): Boolean {
        if (it.isOpened) {
            return true
        } else {

            val slideMappings = getSlideCompanyMappings(it.slideId)

            if (slideMappings.isEmpty()) {
                return false
            }

            slideMappings.forEach {

                if (it.isopened) {
                    return true
                }

                if (it.userIdsRequired) {
                    val userMatched = it.userUids.contains(getUID())
                    if (userMatched) return true
                }


                if (it.rolesRequired) {

                    if (mProfile?.role_interests != null) {
                        for (role in mProfile!!.role_interests!!) {
                            for (courseRoles in it.roles) {
                                if (courseRoles == role.interestID) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            return false
        }
    }

    suspend fun getSlideContent(
        lessonId: String
    ): List<SlideContent> {
        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getSlideContentC(lessonId).filter {
            it.isActive
        }
    }

    private suspend fun getSlideContentC(
        lessonId: String
    ): List<SlideContent> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(LESSON_ID, lessonId)
            .whereEqualTo(
                TYPE,
                TYPE_TOPIC
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val videoDetails = it.toObject(SlideContentRemote::class.java)!!
                        videoDetails.id = it.id
                        videoDetails
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

    suspend fun getAssessmentsFromAllCourses(): List<CourseContent> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getAssessmentsFromAllCoursesC().filter {
            it.isActive && doesLessonFullFillsCondition(it)
        }
    }

    private suspend fun getAssessmentsFromAllCoursesC(): List<CourseContent> =
        suspendCoroutine { cont ->
            getCollectionReference()
                .whereEqualTo(
                    TYPE,
                    TYPE_LESSON
                )
                .whereEqualTo(
                    LESSON_TYPE,
                    LESSON_TYPE_ASSESSMENT
                )
                .get()
                .addOnSuccessListener { querySnap ->

                    val modules = querySnap.documents
                        .map {
                            val lesson = it.toObject(CourseContent::class.java)!!
                            lesson.id = it.id
                            lesson
                        }
                    cont.resume(modules)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    suspend fun getModulesFromAllCourses(): List<Module> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getModulesFromAllCoursesC().filter {
            it.isActive && doesModuleFullFillsCondition(it)
        }
    }

    private suspend fun getModulesFromAllCoursesC(): List<Module> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(
                TYPE,
                TYPE_MODULE
            )
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val modules = it.toObject(Module::class.java)!!
                        modules.id = it.id
                        modules
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    private suspend fun doesModuleFullFillsCondition(it: Module): Boolean {
        if (it.isOpened) {
            return true
        } else {

            val moduleMapping = getModuleCompanyMappings(it.id)

            if (moduleMapping.isEmpty()) {
                return false
            }

            moduleMapping.forEach {

                if (it.isopened) {
                    return true
                }

                if (it.userIdsRequired) {
                    val userMatched = it.userUids.contains(getUID())
                    if (userMatched) return true
                }


                if (it.rolesRequired) {

                    if (mProfile?.role_interests != null) {
                        for (role in mProfile!!.role_interests!!) {
                            for (courseRoles in it.roles) {
                                if (courseRoles == role.interestID) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            return false
        }
    }


    suspend fun getModules(courseId: String): List<Module> {

        if (mProfile == null) {
            mProfile = profileFirebaseRepository.getProfileData()
        }

        return getModulesC(courseId).filter {
            it.isActive && doesModuleFullFillsCondition(it)
        }.sortedBy { it.priority }
    }

    private suspend fun getModulesC(courseId: String): List<Module> = suspendCoroutine { cont ->
        getCollectionReference()
            .whereEqualTo(
                TYPE,
                TYPE_MODULE
            )
            .whereEqualTo(COURSE_ID, courseId)
            .get()
            .addOnSuccessListener { querySnap ->

                val modules = querySnap.documents
                    .map {
                        val modules = it.toObject(Module::class.java)!!
                        modules.id = it.id
                        modules
                    }
                cont.resume(modules)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    private suspend fun getModuleC(moduleId: String): Module = suspendCoroutine { cont ->
        getCollectionReference()
            .document(moduleId)
            .get()
            .addOnSuccessListener { docRef ->

                val module = docRef.toObject(Module::class.java)!!
                module.id = docRef.id

                cont.resume(module)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }


    suspend fun getModulesWithCourseContent(courseId: String): List<Module> =
        suspendCoroutine { cont ->
            getCollectionReference()
                .whereEqualTo(
                    TYPE,
                    TYPE_LESSON
                )
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

    suspend fun getLesson(lessonId: String): CourseContent = suspendCoroutine { cont ->
        getCollectionReference()
            .document(lessonId)
            .get()
            .addOnSuccessListener {

                val lesson = it.toObject(CourseContent::class.java)!!
                lesson.id = it.id
                cont.resume(lesson)
            }
            .addOnFailureListener {

                cont.resumeWithException(it)
            }
    }

    suspend fun syncCourseProgressData(courseId: String) {
        val courseProgress = getCourseProgress(courseId)

        val modules = getModules(courseId)
        val moduleProgress = getCourseModulesProgress(courseId).toMutableList()

        //Marking Progress as inactive of modules which have been removed
        moduleProgress.forEach { progress ->
            progress.isActive = modules.find { progress.moduleId == it.id } != null
        }

        //marking progress inactive which have been removed from module
        for (i in 0 until moduleProgress.size) {
            val moduleProgressData = moduleProgress[i]
            val moduleLessons = getModuleLessons(moduleProgressData.moduleId)

            moduleProgressData.lessonsProgress.forEach { lessonProg ->
                lessonProg.isActive = moduleLessons.find { it.id == lessonProg.lessonId } != null
            }
        }

        //Adding newly Added modules
        val newsModulesAdded = modules.filter { module ->
            moduleProgress.find { it.moduleId == module.id } == null
        }

        val newlyModulesProgressData = newsModulesAdded.map {

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

            val moduleProg =
                ModuleProgress(
                    uid = getUID(),
                    courseId = courseId,
                    moduleId = it.id,
                    moduleStartDate = Timestamp.now(),
                    moduleCompletionDate = null,
                    ongoing = false,
                    completed = false,
                    lessonsTotal = lessonProgress.size,
                    lessonsProgress = lessonProgress
                )

            addNewModuleProgress(moduleProg)
        }
        moduleProgress.addAll(newlyModulesProgressData)


        //adding progress of lessons which have been added to modules newly
        for (moduleData in modules) {

            val moduleLessons = getModuleLessons(moduleData.id)
            val moduleLessonsProgress = moduleProgress.find { it.moduleId == moduleData.id }!!

            val newlyAddedLessons = moduleLessons.filter { lesson ->
                moduleLessonsProgress.lessonsProgress.find { it.lessonId == lesson.id } == null
            }

            //Adding Newly Added lessons to module lesson progress list
            moduleProgress.forEach {

                if (it.moduleId == moduleData.id) {
                    val lessonList = it.lessonsProgress.toMutableList()

                    newlyAddedLessons.forEach {
                        lessonList.add(
                            LessonProgress(
                                uid = getUID(),
                                courseId = it.courseId,
                                moduleId = it.moduleId,
                                lessonId = it.id,
                                lessonStartDate = Timestamp.now(),
                                lessonCompletionDate = null,
                                ongoing = false,
                                priority = it.priority,
                                completed = false,
                                lessonType = it.type
                            )
                        )
                    }

                    it.lessonsProgress = lessonList
                }
            }
        }

        //Updating total lessons and other stuff
        moduleProgress
            .filter {
                it.isActive
            }.forEach {
                var totalLessons = 0
                var completedLessons = 0

                it.lessonsProgress.filter { it.isActive }.forEach {

                    totalLessons++
                    if (it.completed) completedLessons++
                }

                //Updating count only if it differs
                it.lessonsTotal = totalLessons
                it.lessonsCompleted = completedLessons

                if (it.lessonsTotal == it.lessonsCompleted) {
                    it.completed = true

                    if (it.moduleCompletionDate != null) {
                        it.moduleCompletionDate = Timestamp.now()
                    }
                } else {
                    it.completed = false
                    it.moduleCompletionDate = null
                }

                updateModuleProgress(it.progressId, it)
            }


        //Updating course progress info
        var modulesCompleted = 0
        var totalModules = 0

        moduleProgress.forEach {
            totalModules++
            if (it.completed) modulesCompleted++
        }

        if (courseProgress.totalModules != totalModules || courseProgress.completedModules != modulesCompleted) {
            courseProgress.completed = modulesCompleted == totalModules

            if (courseProgress.courseCompletionDate != null) {
                courseProgress.courseCompletionDate = Timestamp.now()
            }

            updateCourseProgress(courseProgress.progressId, courseProgress)
        }
    }

    private suspend fun addNewModuleProgress(moduleProgress: ModuleProgress): ModuleProgress {
        val docRef = db.collection(COURSE_PROGRESS_NAME)
            .addOrThrow(moduleProgress)

        moduleProgress.progressId = docRef.id
        return moduleProgress
    }

    suspend fun recordLessonFeedback(
        lessonId: String,
        lessonRating: Float? = null,
        explanation: Boolean? = null,
        completeness: Boolean? = null,
        easyToUnderStand: Boolean? = null,
        videoQuality: Boolean? = null,
        soundQuality: Boolean? = null
    ) {

        val querySnap = db.collection(COLLECTION_LESSON_FEEDBACK)
            .whereEqualTo("uid", getUID())
            .whereEqualTo("lessonId", lessonId)
            .getOrThrow()

        if (querySnap.isEmpty) {
            //Create new Entry

            db.collection(COLLECTION_LESSON_FEEDBACK).add(
                LessonFeedback(
                    lessonId = lessonId,
                    uid = getUID(),
                    lessonRating = lessonRating,
                    explanation = explanation,
                    completeness = completeness,
                    easyToUnderStand = easyToUnderStand,
                    videoQuality = videoQuality,
                    soundQuality = soundQuality
                )
            )
        } else {
            //Update
            val docSnap = querySnap.documents.first()
            val feedback = docSnap.toObject(LessonFeedback::class.java)!!

            feedback.lessonRating = lessonRating
            feedback.explanation = explanation
            feedback.completeness = completeness
            feedback.easyToUnderStand = easyToUnderStand
            feedback.videoQuality = videoQuality
            feedback.soundQuality = soundQuality

            db.collection(COLLECTION_LESSON_FEEDBACK)
                .document(docSnap.id)
                .setOrThrow(feedback)
        }
    }

    companion object {
        private const val COLLECTION_NAME = "Course_blocks"
        private const val COURSE_PROGRESS_NAME = "Course_Progress"
        private const val COLLECTION_LESSON_FEEDBACK = "Course_lesson_feedback"

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