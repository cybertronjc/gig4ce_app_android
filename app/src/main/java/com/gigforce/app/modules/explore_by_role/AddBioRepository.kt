package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import kotlinx.coroutines.tasks.await

class AddBioRepository : BaseFirestoreDBRepository(), AddBioViewModelCallbacks {
    override fun saveBio(
            bio: String,
            responseCallbacks: AddBioViewModelCallbacks.ResponseCallbacks
    ) {
        getCollectionReference().document(getUID()).update("aboutMe", bio).addOnCompleteListener {
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