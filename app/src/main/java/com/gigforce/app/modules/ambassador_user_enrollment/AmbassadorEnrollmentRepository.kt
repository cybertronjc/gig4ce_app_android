package com.gigforce.app.modules.ambassador_user_enrollment

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class AmbassadorEnrollmentRepository  : BaseFirestoreDBRepository() {


    fun getEnrolledUsersQuery() = getDBCollection().collection(COLLECTION_Enrolled_Users)

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = "Ambassador_Enrolled_User"
        private const val COLLECTION_Enrolled_Users = "Enrolled_Users"
    }
}