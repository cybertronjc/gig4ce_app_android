package com.gigforce.app.modules.gigPage2.repositories

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.utils.EventLogs.getOrThrow
import javax.inject.Inject

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
}