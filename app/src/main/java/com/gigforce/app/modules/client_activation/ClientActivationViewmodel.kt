package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.client_activation.models.JobProfile
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.StringConstants
import com.google.firebase.firestore.ListenerRegistration

class ClientActivationViewmodel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var initialized: Boolean = false
    val clientActivationRepository = ClientActivationRepository()

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

    fun getWorkOrder(docID: String) {
        clientActivationRepository.getCollectionReference().document(docID)
            .addSnapshotListener { success, error ->
                run {

                    if (error != null) {
                        _observableError.value = error.message
                    } else {
                        val toObject = success?.toObject(JobProfile::class.java)
                        savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
                        _observableWorkOrder.value = toObject
                    }
                    initialized = true
                }
            }

    }

    fun getCoursesList(lessons: List<String>) {
        if (!lessons.isNullOrEmpty()) {
            _observableCourses.value = Lce.loading()
            clientActivationRepository.db.collection("Course_blocks").whereIn("course_id", lessons).whereEqualTo("lesson_type","video")
                .addSnapshotListener { success, error ->
                    if (error != null) {
                        _observableCourses.value = Lce.error(error.message.toString())
                    } else {
                        val toObjects = success?.toObjects(LessonModel::class.java)
                        _observableCourses.value = Lce.content(toObjects!!);
                        savedStateHandle.set(
                            StringConstants.SAVED_STATE_VIDEOS_CLIENT_ACT.value,
                            toObjects
                        )
                    }

                }
        }

    }

    fun getApplication(workOrderId: String) {
        var listener: ListenerRegistration? = null
        listener = clientActivationRepository.db.collection("JP_Applications")
            .whereEqualTo("jpid", workOrderId)
            .whereEqualTo("gigerId", clientActivationRepository.getUID())
            .addSnapshotListener { success, err ->
                listener?.remove()
                run {
                    if (err == null) {
                        if (!success?.documents.isNullOrEmpty()) {
                            observableJpApplication.value =
                                success?.toObjects(JpApplication::class.java)?.get(0)
                        } else {
                            observableJpApplication.value = null

                        }
                    } else {
                        observableJpApplication.value = null
                    }
                }
            }
    }


}