package com.gigforce.client_activation.client_activation

import android.location.Location
import com.gigforce.common_ui.viewdatamodels.client_activation.Media
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ClientActivationNewUserRepo : ClientActivationNavCallbacks {
    private var firebaseDB = FirebaseFirestore.getInstance()


    override fun getJobProfile(
            docID: String,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        firebaseDB.collection("Job_Profiles").document(docID)
                .addSnapshotListener { success, error ->
                    run {
                        responseCallbacks.jobProfileResponse(success, error)


                    }
                }
    }

    override fun getCoursesList(
        lessons: List<Media>,
        responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        firebaseDB.collection("Course_blocks").whereIn("id",lessons.map { it.lessonId })//.whereIn("course_id", lessons.map { it.courseId }).whereIn("lesson_id", lessons.map { it.lessonId })
                .addSnapshotListener { success, error ->
                    responseCallbacks.lessonResponse(success, error, lessons.map { it.lessonId })
                }
    }

    override fun getApplication(
            jobProfileID: String,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        var listener: ListenerRegistration? = null
        listener = firebaseDB.collection("JP_Applications")
                .whereEqualTo("jpid", jobProfileID)
                .whereEqualTo("gigerId", FirebaseAuth.getInstance().currentUser?.uid)
                .addSnapshotListener { success, err ->
                    listener?.remove()
                    run {
                        responseCallbacks.applicationResponse(success, err)
                    }
                }
    }

    override fun addInviteUserID(
            jobProfileID: String,
            mInviteUserId: String,
            location: Location,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {

    }

    override fun getUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid!!
    }


}