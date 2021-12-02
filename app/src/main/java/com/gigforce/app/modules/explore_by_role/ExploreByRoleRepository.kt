package com.gigforce.app.modules.explore_by_role

import android.location.Location
import com.gigforce.core.StringConstants
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.RoleInterests
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class ExploreByRoleRepository : BaseFirestoreDBRepository(), ExploreByRoleCallbacks {
    override fun getCollectionName(): String {
        return "Roles"
    }

    override fun getRoles(responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks) {
        getCollectionReference().addSnapshotListener { success, error ->
            run {
                responseCallbacks.getRolesResponse(success, error)
            }
        }

    }

    override fun checkIfDocsAreVerified(responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks) {
        db.collection("Verification").document(getUID()).addSnapshotListener { element, err ->
            run {
                responseCallbacks.docsVerifiedResponse(element, err)
            }
        }
    }

    override fun markAsInterest(
        roleID: String?,
        inviteID: String?,
        location: Location?,

        responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks
    ) {
        val map = mapOf("role_interests" to
            FieldValue.arrayUnion(
                RoleInterests(
                    interestID = roleID,
                    lat = location?.latitude.toString(),
                    lon = location?.longitude.toString(),
                    invitedBy = inviteID ?: ""
                )
            ), "updatedAt" to Timestamp.now(), "updatedBy" to getUID()
        )
        db.collection("Profiles").document(getUID())
            .update(
                map
            )
            .addOnCompleteListener {
                responseCallbacks.markedAsInterestSuccess(it)
            }
    }
}