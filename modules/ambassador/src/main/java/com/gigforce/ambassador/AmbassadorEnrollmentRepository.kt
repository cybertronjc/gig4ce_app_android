package com.gigforce.ambassador

import com.gigforce.core.fb.BaseFirestoreDBRepository

class AmbassadorEnrollmentRepository  : BaseFirestoreDBRepository() {

    fun getEnrolledUsersQuery() = getDBCollection().collection(COLLECTION_ENROLLED_USERS)

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }



    companion object {
        private const val COLLECTION_NAME = "Ambassador_Enrolled_User"
        private const val COLLECTION_ENROLLED_USERS = "Enrolled_Users"
    }
}