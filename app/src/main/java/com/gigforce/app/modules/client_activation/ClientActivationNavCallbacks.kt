package com.gigforce.app.modules.client_activation

import android.location.Location
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.Lce
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

interface ClientActivationNavCallbacks {
    fun getWorkOrder(docID: String, responseCallbacks: ClientActivationResponseCallbacks)
    fun getCoursesList(lessons: List<String>, responseCallbacks: ClientActivationResponseCallbacks)
    fun getApplication(workOrderId: String, responseCallbacks: ClientActivationResponseCallbacks)
    fun addInviteUserID(
        mWorkOrderId: String,
        mInviteUserId: String,
        location: Location,
        responseCallbacks: ClientActivationResponseCallbacks
    )

    fun getUserID(): String
    public interface ClientActivationResponseCallbacks {
        fun lessonResponse(snapShot: QuerySnapshot?, exception: Exception?)
        fun workOrderResponse(snapShot: DocumentSnapshot?, exception: Exception?)
        fun applicationResponse(snapShot: QuerySnapshot?, exception: Exception?)
        fun addMarkInterestStatus(it: Task<Void>)

    }
}