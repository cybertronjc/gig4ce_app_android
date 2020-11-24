package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.WorkOrder
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.StringConstants

class ClientActivationViewmodel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var initialized: Boolean = false
    val clientActivationRepository = ClientActivationRepository()

    private val _observableWorkOrder: MutableLiveData<WorkOrder>
        get() = savedStateHandle.getLiveData(
            StringConstants.SAVED_STATE.value,
            WorkOrder()
        )
    val observableWorkOrder: MutableLiveData<WorkOrder> = _observableWorkOrder


    private val _observableCourses: SingleLiveEvent<Lce<List<LessonModel>>> by lazy {
        SingleLiveEvent<Lce<List<LessonModel>>>();
    }
    val observableCourses: SingleLiveEvent<Lce<List<LessonModel>>> get() = _observableCourses
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
                        val toObject = success?.toObject(WorkOrder::class.java)
                        savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
                        _observableWorkOrder.value = toObject
                    }
                    initialized = true
                }
            }

    }

    fun getCoursesList(lessons: List<String>) {

        _observableCourses.value = Lce.loading()
        clientActivationRepository.db.collection("Course_blocks").whereIn("docId", lessons)
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