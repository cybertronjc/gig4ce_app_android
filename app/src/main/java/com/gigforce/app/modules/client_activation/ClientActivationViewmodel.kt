package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.WorkOrder
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent

class ClientActivationViewmodel : ViewModel() {

    val clientActivationRepository = ClientActivationRepository()

    private val _observableWorkOrder: SingleLiveEvent<WorkOrder> by lazy {
        SingleLiveEvent<WorkOrder>();
    }
    val observableWorkOrder: SingleLiveEvent<WorkOrder> get() = _observableWorkOrder

    private val _observableCourses: SingleLiveEvent<Lce<List<Course>>> by lazy {
        SingleLiveEvent<Lce<List<Course>>>();
    }
    val observableCourses: SingleLiveEvent<Lce<List<Course>>> get() = _observableCourses
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
                        _observableWorkOrder.value = success?.toObject(WorkOrder::class.java)
                    }

                }
            }

    }

    fun getCoursesList(courses: List<String>) {
        _observableCourses.value = Lce.loading()

        clientActivationRepository.db.collection("Course_blocks").whereIn("lesson_id", courses)
            .addSnapshotListener { success, error ->
                if (error != null) {
                    _observableCourses.value = Lce.error(error.message.toString())
                } else {
                    _observableCourses.value = Lce.content(success?.toObjects(Course::class.java)!!);
                }

            }

    }


}