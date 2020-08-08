package com.gigforce.app.modules.gigerid

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageReference

interface GigerIDCallbacks {
    fun getProfileData(responseCallbacks: ResponseCallbacks)
    fun getProfilePicture(avatarName: String, responseCallbacks: ResponseCallbacks)
    fun getGigDetails(gigId: String, responseCallbacks: ResponseCallbacks)
    interface ResponseCallbacks {
        fun getProfileSuccess(querySnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?)
        fun getProfilePic(reference: StorageReference)
        fun getGigDetailsResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )

    }
}