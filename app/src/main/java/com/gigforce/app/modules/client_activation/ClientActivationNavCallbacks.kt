package com.gigforce.app.modules.client_activation

import android.location.Location
import com.gigforce.app.modules.client_activation.models.Media
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

interface ClientActivationNavCallbacks {
    fun getJobProfile(docID: String, responseCallbacks: ClientActivationResponseCallbacks)
    fun getCoursesList(lessons: List<Media>, responseCallbacks: ClientActivationResponseCallbacks)
    fun getApplication(jobProfileId: String, responseCallbacks: ClientActivationResponseCallbacks)
    fun addInviteUserID(
            jobProfileID: String,
            mInviteUserId: String,
            location: Location,
            responseCallbacks: ClientActivationResponseCallbacks
    )

    fun getUserID(): String
    public interface ClientActivationResponseCallbacks {
        fun lessonResponse(snapShot: QuerySnapshot?, exception: Exception?, lessonsToFilter: List<String>)
        fun jobProfileResponse(snapShot: DocumentSnapshot?, exception: Exception?)
        fun applicationResponse(snapShot: QuerySnapshot?, exception: Exception?)
        fun addMarkInterestStatus(it: Task<Void>)

    }
}