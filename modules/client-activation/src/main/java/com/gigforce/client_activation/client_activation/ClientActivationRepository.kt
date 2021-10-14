package com.gigforce.client_activation.client_activation

import android.location.Location
import com.gigforce.common_ui.viewdatamodels.client_activation.Media
import com.gigforce.core.StringConstants
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.ClientActs
import com.google.firebase.Timestamp
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

        db.collection("Course_blocks").whereIn(
            "id",
            lessons.map { it.lessonId })//.whereIn("course_id", lessons.map { it.courseId }).whereEqualTo("type","lesson")
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
        val map = mapOf("updatedAt" to Timestamp.now(),"updatedBy" to StringConstants.APP.value, "invited_client_activations" to
            FieldValue.arrayUnion(
                ClientActs(
                    jobProfileId = jobProfileID,
                    lat = location.latitude.toString(),
                    lon = location.longitude.toString(),
                    invitedBy = mInviteUserId
                )
            ))
        db.collection("Profiles").document(getUID())
            .update(
                map
            )
            .addOnCompleteListener {
                responseCallbacks.addMarkInterestStatus(it)
            }
    }

    override fun getUserID(): String {
        return getUID()
    }


}