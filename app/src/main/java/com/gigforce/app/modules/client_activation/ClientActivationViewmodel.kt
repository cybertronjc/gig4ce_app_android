package com.gigforce.app.modules.client_activation

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.client_activation.models.JobProfile
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.StringConstants
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

class ClientActivationViewmodel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), ClientActivationNavCallbacks.ClientActivationResponseCallbacks {

    private lateinit var clientActivationNavCallbacks: ClientActivationNavCallbacks
    fun setRepository(callbacks: ClientActivationNavCallbacks) {
        this.clientActivationNavCallbacks = callbacks;
    }

    var initialized: Boolean = false
    private val _observableWorkOrder: MutableLiveData<JobProfile>
        get() = savedStateHandle.getLiveData(
            StringConstants.SAVED_STATE.value,
            JobProfile()
        )
    val observableWorkOrder: MutableLiveData<JobProfile> = _observableWorkOrder


    private val _observableCourses: SingleLiveEvent<Lce<List<LessonModel>>> by lazy {
        SingleLiveEvent<Lce<List<LessonModel>>>();
    }
    val observableCourses: SingleLiveEvent<Lce<List<LessonModel>>> get() = _observableCourses

    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>();
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableAddInterest: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableAddInterest: SingleLiveEvent<Boolean> get() = _observableAddInterest

    fun getWorkOrder(docID: String) {
        clientActivationNavCallbacks.getWorkOrder(docID, this)


    }

    fun getCoursesList(lessons: List<String>) {
        if (!lessons.isNullOrEmpty()) {
            _observableCourses.value = Lce.loading()
            clientActivationNavCallbacks.getCoursesList(lessons, this)
        }

    }

    fun getApplication(workOrderId: String) {
        clientActivationNavCallbacks.getApplication(workOrderId, this)

    }

    fun getUID(): String {
        return clientActivationNavCallbacks.getUserID()
    }

    override fun lessonResponse(snapShot: QuerySnapshot?, exception: Exception?) {
        if (exception != null) {
            _observableCourses.value = Lce.error(exception.message.toString())
        } else {
            if (!snapShot?.documentChanges.isNullOrEmpty()) {
                val toObjects = snapShot?.toObjects(LessonModel::class.java)
                _observableCourses.value = Lce.content(toObjects!!);
                savedStateHandle.set(
                    StringConstants.SAVED_STATE_VIDEOS_CLIENT_ACT.value,
                    toObjects
                )
            } else {
                _observableCourses.value = Lce.error("No Videos Found!!!")

            }

        }
    }

    override fun workOrderResponse(snapShot: DocumentSnapshot?, exception: Exception?) {
        if (exception != null) {
            _observableError.value = exception.message
        } else {
            val toObject = snapShot?.toObject(JobProfile::class.java)
            savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
            _observableWorkOrder.value = toObject
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