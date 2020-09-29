package com.gigforce.app.modules.explore_by_role

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ExploreByRoleCallbacks {
    fun getRoles(responseCallbacks: ResponseCallbacks)

    interface ResponseCallbacks {
        fun getRolesResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
    }

}