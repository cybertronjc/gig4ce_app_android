package com.gigforce.app.modules.explore_by_role

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import java.lang.Exception


interface AddBioViewModelCallbacks {
    fun saveBio(bio: String, responseCallbacks: ResponseCallbacks)

    suspend fun getProfileData(responseCallbacks: ResponseCallbacks)


    interface ResponseCallbacks {
        fun saveBioResponse(
                task: Task<Void>
        )

        fun profileDate(docReference: DocumentSnapshot?, exception: Exception?)

    }
}