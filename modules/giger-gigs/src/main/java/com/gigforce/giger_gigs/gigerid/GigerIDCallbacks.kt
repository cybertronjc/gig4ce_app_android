package com.gigforce.giger_gigs.gigerid

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageReference

interface GigerIDCallbacks {
    fun getProfileData(responseCallbacks: ResponseCallbacks)
    fun getProfilePicture(avatarName: String, responseCallbacks: ResponseCallbacks)
    fun getGigDetails(gigId: String, responseCallbacks: ResponseCallbacks)
    suspend fun getGigAndGigOrderDetails(gigId : String) : GigAndGigOrder
    fun getURls(responseCallbacks: ResponseCallbacks)
    interface ResponseCallbacks {
        fun getProfileSuccess(querySnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?)
        fun getProfilePic(reference: StorageReference)
        fun getGigDetailsResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )

        fun getUrlResponse(
            querySnapshot: DocumentSnapshot?,
            error: FirebaseFirestoreException?
        )

    }
}