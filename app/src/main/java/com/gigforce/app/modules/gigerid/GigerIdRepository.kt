package com.gigforce.app.modules.gigerid

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.preferences.PreferencesFragment

class GigerIdRepository : BaseFirestoreDBRepository(), GigerIDCallbacks {


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

    override fun getGigDetails(
        gigId: String,
        responseCallbacks: GigerIDCallbacks.ResponseCallbacks
    ) {

        db.collection("Gigs")
            .document(gigId)
            .addSnapshotListener { documentSnapshot, error ->

                responseCallbacks.getGigDetailsResponse(documentSnapshot, error)
            }
    }

    override fun getURls(responseCallbacks: GigerIDCallbacks.ResponseCallbacks) {
        db.collection("Configuration").document("Urls").addSnapshotListener { success, error ->
            run {
                responseCallbacks.getUrlResponse(success, error)
            }
        }
    }


    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }


    companion object {
        private const val COLLECTION_NAME = "Profiles"
    }

}