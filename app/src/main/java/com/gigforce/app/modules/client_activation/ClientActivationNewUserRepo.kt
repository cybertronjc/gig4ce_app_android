package com.gigforce.app.modules.client_activation

import android.location.Location
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
        lessons: List<String>,
        responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        firebaseDB.collection("Course_blocks").whereIn("lesson_id", lessons)
            .addSnapshotListener { success, error ->
                responseCallbacks.lessonResponse(success, error)


            }
    }

    override fun getApplication(
        jobProfileID: String,
        responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        var listener: ListenerRegistration? = null
        listener = firebaseDB.collection("JP_Applications")
            .whereEqualTo("jpid", jobProfileID)
            .whereEqualTo("gigerId", getUserID())
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
        return ""
    }


}