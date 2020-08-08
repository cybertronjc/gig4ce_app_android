package com.gigforce.app.modules.gigerid

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.preferences.PreferencesFragment

class GigerIdRepository : BaseFirestoreDBRepository(), GigerIDCallbacks {
    var COLLECTION_NAME = "Profiles"

    override fun getProfileData(responseCallbacks: GigerIDCallbacks.ResponseCallbacks) {
        getDBCollection()
            .addSnapshotListener { value, e ->
                responseCallbacks.getProfileSuccess(value, e)
            }
    }

    override fun getProfilePicture(
        avatarName: String,
        responseCallbacks: GigerIDCallbacks.ResponseCallbacks
    ) {
        PreferencesFragment.storage.reference.child("profile_pics").child(avatarName)

        responseCallbacks.getProfilePic(
            PreferencesFragment.storage.reference.child("profile_pics").child(avatarName)
        )

    }


    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}