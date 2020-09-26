package com.gigforce.app.modules.explore_by_role

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

interface RoleDetailsCallbacks {

    fun getRoleDetails(id: String?, responseCallbacks: ResponseCallbacks)

    interface ResponseCallbacks {
        fun getRoleDetailsResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )
    }
}