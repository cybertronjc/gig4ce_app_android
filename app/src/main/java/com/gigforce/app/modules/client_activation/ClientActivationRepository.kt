package com.gigforce.app.modules.client_activation

import android.location.Location
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.client_activation.models.Media
import com.gigforce.app.modules.profile.models.ClientActs
import com.google.firebase.firestore.FieldValue

import com.google.firebase.firestore.ListenerRegistration

class ClientActivationRepository : BaseFirestoreDBRepository(), ClientActivationNavCallbacks {
    override fun getCollectionName(): String {
        return "Job_Profiles"
    }

    override fun getJobProfile(
            docID: String,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        getCollectionReference().document(docID)
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

        db.collection("Course_blocks").whereIn("id",lessons.map { it.lessonId })//.whereIn("course_id", lessons.map { it.courseId }).whereEqualTo("type","lesson")
                .addSnapshotListener { success, error ->
                    responseCallbacks.lessonResponse(success, error, lessons.map { it.lessonId })
                }
    }

    override fun getApplication(
            jobProfileId: String,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        var listener: ListenerRegistration? = null
        listener = db.collection("JP_Applications")
                .whereEqualTo("jpid", jobProfileId)
                .whereEqualTo("gigerId", getUID())
                .addSnapshotListener { success, err ->
                    listener?.remove()
                    run {
                        responseCallbacks.applicationResponse(success, err)
                    }
                }
    }

    override fun addInviteUserID(
            jobProfileID: String,
            mInviteUserId: String, location: Location,
            responseCallbacks: ClientActivationNavCallbacks.ClientActivationResponseCallbacks
    ) {
        db.collection("Profiles").document(getUID())
                .update(
                        "invited_client_activations",
                        FieldValue.arrayUnion(
                                ClientActs(
                                        jobProfileId = jobProfileID,
                                        lat = location.latitude.toString(),
                                        lon = location.longitude.toString(),
                                        invitedBy = mInviteUserId ?: ""
                                )
                        )
                )
                .addOnCompleteListener {
                    responseCallbacks.addMarkInterestStatus(it)
                }
    }

    override fun getUserID(): String {
        return getUID()
    }


}