package com.gigforce.app.modules.explore_by_role

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

interface RoleDetailsCallbacks {

    fun getRoleDetails(id: String?, responseCallbacks: ResponseCallbacks)
    fun markAsInterest(roleID: String?, responseCallbacks: ResponseCallbacks)

    interface ResponseCallbacks {
        fun getRoleDetailsResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )

        fun markedAsInterestSuccess(it: Task<Void>)
    }
}