package com.gigforce.app.modules.explore_by_role

import com.gigforce.core.StringConstants
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class AddBioRepository : BaseFirestoreDBRepository(), AddBioViewModelCallbacks {
    override fun saveBio(
            bio: String,
            responseCallbacks: AddBioViewModelCallbacks.ResponseCallbacks
    ) {
        val map = mapOf("aboutMe" to bio, "updatedAt" to Timestamp.now(), "updatedBy" to StringConstants.APP.value)
        getCollectionReference().document(getUID()).update(map).addOnCompleteListener {
            responseCallbacks.saveBioResponse(it)
        }
    }

    override suspend fun getProfileData(responseCallbacks: AddBioViewModelCallbacks.ResponseCallbacks) {
        try {
            val await = getCollectionReference().document(getUID()).get().await()
            responseCallbacks.profileDate(await, null)

        } catch (e: Exception) {
            responseCallbacks.profileDate(null, e)
        }


    }

    override fun getCollectionName(): String {
        return "Profiles"
    }


}