package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class RepositoryAmbassadorProgram : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Ambassador_Profiles"
    }

}