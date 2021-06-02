package com.gigforce.common_ui.repository.gig

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.utils.EventLogs.getOrThrow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GigerProfileFirebaseRepository @Inject constructor() : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun getFirstProfileWithPhoneNumber(
        phoneNumber: String? = null
    ): ProfileData? {

        val querySnap = getCollectionReference()
            .whereEqualTo("loginMobile", phoneNumber)
            .getOrThrow()

        if (querySnap.isEmpty)
            return null

        val docSnap = querySnap.documents.first()
        val profileData = docSnap.toObject(ProfileData::class.java)
            ?: throw  IllegalStateException("unable to parse profile object")
        profileData.id = docSnap.id
        return profileData
    }

    suspend fun getProfileDataIfExist(userId: String? = null): ProfileData? =
        suspendCoroutine { cont ->

            getCollectionReference()
                .document(userId ?: getUID())
                .get()
                .addOnSuccessListener {

                    if (it.exists()) {
                        val profileData = it.toObject(ProfileData::class.java)
                            ?: throw  IllegalStateException("unable to parse profile object")
                        profileData.id = it.id
                        cont.resume(profileData)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

}