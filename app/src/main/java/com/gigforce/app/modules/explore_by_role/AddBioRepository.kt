package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class AddBioRepository : BaseFirestoreDBRepository(), AddBioViewModelCallbacks {
    override fun saveBio(
        bio: String,
        responseCallbacks: AddBioViewModelCallbacks.ResponseCallbacks
    ) {
        getCollectionReference().document(getUID()).update("aboutMe", bio).addOnCompleteListener {
            responseCallbacks.saveBioResponse(it)
        }
    }

    override fun getCollectionName(): String {
        return "Profiles"
    }

}