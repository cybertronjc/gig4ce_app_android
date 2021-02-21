package com.gigforce.app.core.base.example

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class TestFirebaseRepository : BaseFirestoreDBRepository() {

    var PROFILE_COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return PROFILE_COLLECTION_NAME
    }
    
}