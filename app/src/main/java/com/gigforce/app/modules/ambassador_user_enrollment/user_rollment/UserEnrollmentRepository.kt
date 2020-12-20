package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment

import com.gigforce.app.BuildConfig
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.ambassador_user_enrollment.models.*
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
            if(response.error != null){
                throw Exception(response.error)
            } else {
                db.collection(COLLECTION_NAME)
                    .document(getUID())
                    .collection(COLLECTION_Enrolled_Users)
                    .document(response.uid!!)
                    .setOrThrow(
                        EnrolledUser(
                            uid = response.uid,
                            enrolledOn = Timestamp.now(),
                            enrolledBy = getUID(),
                            enrollmentStepsCompleted = EnrollmentStepsCompleted(),
                            name = mobile
                        )
                    )
            }

            return response
        }
    }

    suspend fun registerUser(mobile: String): RegisterMobileNoResponse {
        val registerUserRequest = createUserApi.registerMobile(
            BuildConfig.CHECK_USER_OR_SEND_OTP_URL,
            RegisterMobileNoRequest(mobile)
        )

        if (!registerUserRequest.isSuccessful) {
            throw Exception(registerUserRequest.message())
        } else {
            return registerUserRequest.body()!!
        }
    }

    suspend fun verifyOtp(token: String,otp : String): VerifyOtpResponse {
        val verifyOtpResponse = createUserApi.verifyOtp(
            BuildConfig.VERIFY_OTP_URL,
            token,
            otp
        )

        if (!verifyOtpResponse.isSuccessful) {
            throw Exception(verifyOtpResponse.message())
        } else {
            return verifyOtpResponse.body()!!
        }
    }

    suspend fun updateUserProfileName(
        userId: String,
        name: String
    ) {

        db.collection(COLLECTION_NAME)
            .document(getUID())
            .collection(COLLECTION_Enrolled_Users)
            .document(userId)
            .update("name", name)

        //tofo update or throw
    }

    suspend fun updateUserProfilePicture(
        userId: String,
        profilePic: String
    ) {

        db.collection(COLLECTION_NAME)
            .document(getUID())
            .collection(COLLECTION_Enrolled_Users)
            .document(userId)
            .update("profilePic", profilePic)

        //tofo update or throw
    }


    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = "Ambassador_Enrolled_User"
        private const val COLLECTION_Enrolled_Users = "Enrolled_Users"
    }
}