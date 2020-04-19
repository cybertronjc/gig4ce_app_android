package com.gigforce.app.utils.dbrepository.test

import com.gigforce.app.modules.profile.models.Achievement
import com.gigforce.app.utils.dbrepository.BaseFirestoreDBRepository
import com.google.firebase.firestore.FieldValue

class TestFirebaseRepository : BaseFirestoreDBRepository() {

    var PROFILE_COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return PROFILE_COLLECTION_NAME
    }



}