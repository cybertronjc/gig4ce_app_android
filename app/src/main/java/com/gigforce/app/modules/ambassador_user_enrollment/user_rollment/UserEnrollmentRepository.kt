package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment

import com.gigforce.app.BuildConfig
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.ambassador_user_enrollment.models.CreateUserRequest
import com.gigforce.app.modules.ambassador_user_enrollment.models.CreateUserResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrollmentStepsCompleted
import com.gigforce.app.modules.verification.service.CreateUserAccEnrollmentAPi
import com.gigforce.app.modules.verification.service.RetrofitFactory
import com.gigforce.app.utils.setOrThrow
import com.google.firebase.Timestamp

class UserEnrollmentRepository constructor(
    private val createUserApi: CreateUserAccEnrollmentAPi = RetrofitFactory.createUserAccEnrollmentAPi()
) : BaseFirestoreDBRepository() {

    suspend fun createUser(mobile: String): CreateUserResponse {
        val createUserResponse = createUserApi.createUser(
            BuildConfig.CREATE_USER_URL, listOf(
                CreateUserRequest(mobile)
            )
        )

        if (!createUserResponse.isSuccessful) {
            throw Exception(createUserResponse.message())
        } else {
            val response = createUserResponse.body()!!.first()

            db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_Enrolled_Users)
                .document(response.uid)
                .setOrThrow( EnrolledUser(
                    uid = response.uid,
                    enrolledOn = Timestamp.now(),
                    enrolledBy = getUID(),
                    enrollmentStepsCompleted = EnrollmentStepsCompleted(),
                    name = mobile
                ))

            return response
        }
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = "Ambassador_Enrolled_User"
        private const val COLLECTION_Enrolled_Users = "Enrolled_Users"
    }
}