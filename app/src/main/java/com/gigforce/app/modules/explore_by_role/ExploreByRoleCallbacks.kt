package com.gigforce.app.modules.explore_by_role

import android.location.Location
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ExploreByRoleCallbacks {
    fun getRoles(responseCallbacks: ResponseCallbacks)
    fun checkIfDocsAreVerified(responseCallbacks: ResponseCallbacks)
    fun markAsInterest(
        roleID: String?,
        inviteID: String?,
        location: Location?,
        responseCallbacks: ResponseCallbacks
    )

    interface ResponseCallbacks {
        fun getRolesResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun docsVerifiedResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )

        fun markedAsInterestSuccess(it: Task<Void>)


    }

}