package com.gigforce.learning.repo

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.StringConstants
import com.gigforce.learning.datamodels.CourseDM
import com.gigforce.learning.datamodels.CourseMappingDM
import com.gigforce.learning.datamodels.UserInterestsAndRolesDM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface ILearningDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

class LearningDataRepository @Inject constructor() :
    ILearningDataRepository {
    private var allCourses: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()
    private var allAssessments: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()

    init {
        loadData()
    }

    override fun loadData() {
        FirebaseFirestore.getInstance().collection("Course_blocks").orderBy("priority")
            .whereEqualTo("type", "course").whereEqualTo("isopened", true)
            .addSnapshotListener { value, error ->

                val doc = value?.documents
                doc?.let {
                    val _data = ArrayList<FeatureItemCardDVM>()
                    for (item in it) {
                        val title = item?.get("Name") as? String ?: "-"
                        val subtitle = item?.get("Level") as? String ?: "-"
//                    val title = name + level
//                        val priority = (item?.get("priority") as? Int) ?: 500
                        val coverPic = item?.get("cover_pic") as? String
                        val nav_path = "learning/main"//item?.get("nav_path") as? String

                        _data.add(
                            FeatureItemCardDVM(
                                id = item.id,
                                title = title,
                                subtitle = subtitle,
                                image = coverPic,
                                navPath = nav_path,
                                args = bundleOf(
                                    StringConstants.COURSE_ID.value to item?.id
                                )
                            )
                        )
                    }
                    allCourses.value = _data
                }

            }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return allCourses
    }

    suspend fun getAllCourses(): List<CourseDM> {
        getProfileData()
        return getAllCoursesData().filter {
            it.isActive && doesCourseFullFillsCondition(it)
        }.sortedBy {
            it.priority
        }
    }

    suspend fun getAllCoursesData(): List<CourseDM> = suspendCoroutine {
        FirebaseFirestore.getInstance().collection("Course_blocks")
            .whereEqualTo("type", "course")
            .get()
            .addOnSuccessListener { querySnap ->

                val courses = querySnap.documents
                    .map {
                        val course = it.toObject(CourseDM::class.java)!!
                        course.id = it.id
                        course
                    }

            }
            .addOnFailureListener {
            }

    }

    var mProfile: UserInterestsAndRolesDM? = null
    suspend fun getProfileData(): UserInterestsAndRolesDM = suspendCoroutine { cont ->

        FirebaseFirestore.getInstance().collection("Profiles")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).get()
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


    private suspend fun doesCourseFullFillsCondition(it: CourseDM): Boolean {
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
                    val userMatched =
                        it.userUids.contains(FirebaseAuth.getInstance().currentUser?.uid!!)
                    if (userMatched) return true
                }


                if (it.rolesRequired) {

                    mProfile?.role_interests?.let { it1 ->
                        for (role in it1) {
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


    private suspend fun getCourseCompanyMappings(courseId: String): List<CourseMappingDM> =
        suspendCoroutine { cont ->

            mProfile?.let {
                it.companies?.let {
                    val companies: List<String> = it.map {
                        it.companyId
                    }
                    if (companies.isEmpty()) {
                        cont.resume(emptyList())
                    } else {
                        FirebaseFirestore.getInstance().collection("Course_company_mapping")
                            .whereIn("companyId", companies)
                            .whereEqualTo("courseId", courseId)
                            .get()
                            .addOnSuccessListener {
                                val courseMappings = it.documents.map {
                                    it.toObject(CourseMappingDM::class.java)!!
                                }
                                cont.resume(courseMappings)
                            }
                            .addOnFailureListener {
                                cont.resumeWithException(it)
                            }
                    }
                } ?: cont.resume(emptyList())
            } ?: cont.resume(emptyList())
        }
}
