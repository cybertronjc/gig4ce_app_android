package com.gigforce.app.core.base.basefirestore.example

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class TestFirebaseRepository : BaseFirestoreDBRepository() {

    var PROFILE_COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return PROFILE_COLLECTION_NAME
    }



}