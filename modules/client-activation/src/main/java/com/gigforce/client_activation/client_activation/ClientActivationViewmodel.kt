package com.gigforce.client_activation.client_activation

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.common_ui.viewdatamodels.client_activation.Media
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.datamodels.learning.LessonModel
import com.gigforce.core.utils.Lce
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class ClientActivationViewmodel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), ClientActivationNavCallbacks.ClientActivationResponseCallbacks {

    private lateinit var clientActivationNavCallbacks: ClientActivationNavCallbacks
    fun setRepository(callbacks: ClientActivationNavCallbacks) {
        this.clientActivationNavCallbacks = callbacks
    }

    var initialized: Boolean = false
    private val _observableJobProfile: MutableLiveData<JobProfile> by lazy {
        MutableLiveData<JobProfile>()
    }
    val observableJobProfile: MutableLiveData<JobProfile> = _observableJobProfile


    private val _observableCourses: MutableLiveData<List<LessonModel>> by lazy {
        MutableLiveData<List<LessonModel>>()
    }
    val observableCourses: MutableLiveData<List<LessonModel>>
        get() = _observableCourses
    private val _observableCoursesLce: MutableLiveData<Lce<List<LessonModel>>> by lazy {
        MutableLiveData<Lce<List<LessonModel>>>()
    }
    val observableCoursesLce: MutableLiveData<Lce<List<LessonModel>>> = _observableCoursesLce


    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>()
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>()
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableAddInterest: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>()
    }
    val observableAddInterest: SingleLiveEvent<Boolean> get() = _observableAddInterest

    fun getJobProfile(docID: String) {
        clientActivationNavCallbacks.getJobProfile(docID, this)


    }

    fun getCoursesList(lessons: List<Media>) {
        if (!lessons.isNullOrEmpty()) {
            _observableCoursesLce.value = Lce.loading()
            clientActivationNavCallbacks.getCoursesList(lessons, this)
        }

    }

    fun getApplication(jobProfileID: String) {
        clientActivationNavCallbacks.getApplication(jobProfileID, this)

    }

    fun getUID(): String {
        return clientActivationNavCallbacks.getUserID()
    }

    override fun lessonResponse(
        snapShot: QuerySnapshot?,
        exception: Exception?,
        lessonsToFilter: List<String>
    ) {
        if (exception != null) {
            _observableCoursesLce.value = Lce.error(exception.message.toString())
        } else {
            if (!snapShot?.documentChanges.isNullOrEmpty()) {
                val toObjects = snapShot?.toObjects(LessonModel::class.java)
//                savedStateHandle.set(
//                        StringConstants.SAVED_STATE_VIDEOS_CLIENT_ACT.value,
//                        toObjects)
                _observableCourses.value = toObjects!!
                _observableCoursesLce.value = Lce.content(_observableCourses.value!!)

            } else {
                _observableCoursesLce.value = Lce.error("No Videos Found!!!")

            }

        }
    }

    override fun jobProfileResponse(snapShot: DocumentSnapshot?, exception: Exception?) {
        if (exception != null) {
            _observableError.value = exception.message
        } else {
            val toObject = snapShot?.toObject(JobProfile::class.java)
//            savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
            _observableJobProfile.value = toObject
        }
        initialized = true
    }

    override fun applicationResponse(snapShot: QuerySnapshot?, exception: Exception?) {

        if (exception == null) {
            if (!snapShot?.documents.isNullOrEmpty()) {
                observableJpApplication.value =
                    snapShot?.toObjects(JpApplication::class.java)?.get(0)
            } else {
                observableJpApplication.value = null

            }
        } else {
            observableJpApplication.value = null
        }

    }

    override fun addMarkInterestStatus(it: Task<Void>) {
        if (it.isSuccessful) {
            _observableAddInterest.value = true
        } else {
            _observableError.value = it.exception?.message
        }
    }

    fun addInviteUserId(mInviteUserID: String, mJobProfileId: String, location: Location) {
        clientActivationNavCallbacks.addInviteUserID(mJobProfileId, mInviteUserID, location, this)
    }


}