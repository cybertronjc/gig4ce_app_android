package com.gigforce.common_ui.repository.repo

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.datamodels.datamodels.UserInterestsAndRolesDM
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.common_ui.viewdatamodels.models.progress.CourseMapping
import com.gigforce.core.StringConstants
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface ILearningDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>

}

class LearningDataRepository @Inject constructor() :
    ILearningDataRepository, BaseFirestoreDBRepository() {
    private var allCourses: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()
    private var allAssessments: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()

    init {
        loadData()

    }

    private fun loaLearningData() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val _data = ArrayList<FeatureItemCardDVM>()
            val allCourses1 = getAllCourses().forEach { item->_data.add(
                    FeatureItemCardDVM(
                            id = item.id,
                            title = item.name,
                            subtitle = item.level,
                            image = item.coverPicture,
                            navPath = "learning/main",
                            args = bundleOf(
                                    StringConstants.COURSE_ID.value to item?.id
                            ),
                            priority = item.priority
                    )
            ) }
            allCourses.value = _data
        }
//        GlobalScope.launch {
//            val _data = ArrayList<FeatureItemCardDVM>()
//            val allCourses1 = getAllCourses().forEach { item->_data.add(
//                FeatureItemCardDVM(
//                    id = item.id,
//                    title = item.name,
//                    subtitle = item.level,
//                    image = item.coverPicture,
//                    navPath = "learning/main",
//                    args = bundleOf(
//                        StringConstants.COURSE_ID.value to item?.id
//                    ),
//                    priority = item.priority
//                )
//            ) }
//            allCourses.value = _data
//
//        }
    }

    companion object {
        private const val TAG = "LearningRepository"
        const val TYPE = "type"
        const val TYPE_COURSE = "course"
    }

    override fun loadData() {
        loaLearningData()

//        FirebaseFirestore.getInstance().collection("Course_blocks")
//            .whereEqualTo("type", "course").whereEqualTo("isopened", true)
//            .addSnapshotListener { value, error ->
//                val doc = value?.documents
//                doc?.let {
//                    val _data = ArrayList<FeatureItemCardDVM>()
//                    for (item in it) {
//                        val title = item?.get("Name") as? String ?: "-"
//                        val subtitle = item?.get("Level") as? String ?: "-"
////                    val title = name + level
////                        val priority = (item?.get("priority") as? Int) ?: 500
//                        val coverPic = item?.get("cover_pic") as? String
//                        val nav_path = "learning/main"//item?.get("nav_path") as? String
//                        val priority = item?.get("priority") as? Int ?: 0
//                        _data.add(
//                            FeatureItemCardDVM(
//                                id = item.id,
//                                title = title,
//                                subtitle = subtitle,
//                                image = coverPic,
//                                navPath = nav_path,
//                                args = bundleOf(
//                                    StringConstants.COURSE_ID.value to item?.id
//                                ),
//                                priority = priority
//                            )
//                        )
//                    }
//                    _data.sortBy { it.priority }
//                    allCourses.value = _data
//                } ?: run {
//                    allCourses.value = ArrayList<FeatureItemCardDVM>()
//                }
//
//            }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return allCourses
    }

    suspend fun getAllCourses(): List<Course> //List<CourseDM>
    {
        getProfileData()
        return getRoleBasedCoursesC().filter {
            it.isActive && doesCourseFullFillsCondition(it)
        }.sortedBy {
            it.priority
        }
    }

    var mProfile: UserInterestsAndRolesDM? = null
    suspend fun getProfileData(): UserInterestsAndRolesDM = suspendCoroutine { cont ->
        FirebaseAuth.getInstance().currentUser?.uid?.let { useruid->
            FirebaseFirestore.getInstance().collection("Profiles")
                .document(useruid).get()
                .addOnSuccessListener {

                    if (it.exists()) {
                        val profileData = it.toObject(UserInterestsAndRolesDM::class.java)
                            ?: throw  IllegalStateException("unable to parse profile object")
                        profileData.id = it.id
                        mProfile = profileData
                        cont.resume(profileData)
                    } else {
                        cont.resume(UserInterestsAndRolesDM())
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    }


    private suspend fun getRoleBasedCoursesC(): List<Course> = suspendCoroutine { cont ->
        Log.d(TAG, "getRoleBasedCoursesC() : strated fetching all courses ...")
        FirebaseFirestore.getInstance().collection("Course_blocks")
            .whereEqualTo(
                TYPE,
                TYPE_COURSE
            )
            .get()
            .addOnSuccessListener { querySnap ->
                Log.d(TAG, "getRoleBasedCoursesC() : fetched ${querySnap.size()} courses")

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(Course::class.java)!!
                        course.id = it.id
                        course
                    }

                cont.resume(courses)
            }
            .addOnFailureListener {
                Log.d(TAG, "getRoleBasedCoursesC() : error fetching courses", it)
                cont.resumeWithException(it)
            }
    }

    private suspend fun doesCourseFullFillsCondition(it: Course): Boolean {
        Log.d(TAG, "doesCourseFullFillsCondition() : checking course ${it.id} with userMappings..")

        if (it.isOpened) {

            Log.d(
                TAG,
                "doesCourseFullFillsCondition() : course ${it.id} is opened for all returning true "
            )
            return true
        } else {

            Log.d(
                TAG,
                "doesCourseFullFillsCondition() : course ${it.id} getting course-company mappings ..."
            )
            val courseAndMappings = getCourseCompanyMappings(it.id)

            Log.d(
                TAG,
                "doesCourseFullFillsCondition() : course ${it.id} got ${courseAndMappings.size} mappings..."
            )
            if (courseAndMappings.isEmpty()) {
                return false
            }

            courseAndMappings.forEach {

                if (it.isopened) {

                    Log.d(
                        TAG,
                        "doesCourseFullFillsCondition() : course ${it.courseId} company mapping is opened for all returning true "
                    )
                    return true
                }

                if (it.userIdsRequired) {
                    Log.d(TAG, "doesCourseFullFillsCondition() : checking user id... ")

                    val userMatched = it.userUids.contains(
                        FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    )
                    Log.d(TAG, "doesCourseFullFillsCondition() : user matched $userMatched")

                    if (userMatched) return true
                }


                if (it.rolesRequired) {
                    Log.d(TAG, "doesCourseFullFillsCondition() : checking user roles... ")

                    if (mProfile?.role_interests != null) {
                        for (role in mProfile!!.role_interests!!) {
                            for (courseRoles in it.roles) {
                                if (courseRoles == role.interestID) {
                                    Log.d(
                                        TAG,
                                        "doesCourseFullFillsCondition() : roles matched true"
                                    )
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "doesCourseFullFillsCondition() : no cond in course company matched")
            return false
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
                val courseCompanyMappingsCollection =
                    FirebaseFirestore.getInstance().collection("Course_company_mapping")


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

    override fun getCollectionName(): String {
        return ""
    }

}
