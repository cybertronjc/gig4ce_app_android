package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment

import com.gigforce.app.BuildConfig
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.ambassador_user_enrollment.models.*
import com.gigforce.app.modules.verification.service.CreateUserAccEnrollmentAPi
import com.gigforce.app.modules.verification.service.RetrofitFactory
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.setOrThrow
import com.gigforce.app.utils.updateOrThrow
import com.google.firebase.Timestamp

class UserEnrollmentRepository constructor(
        private val createUserApi: CreateUserAccEnrollmentAPi = RetrofitFactory.createUserAccEnrollmentAPi()
) : BaseFirestoreDBRepository() {

    suspend fun createUser(mobile: String, enrolledByName: String): CreateUserResponse {
        val createUserResponse = createUserApi.createUser(
                BuildConfig.CREATE_USER_URL, listOf(
                CreateUserRequest(mobile)
        )
        )

        if (!createUserResponse.isSuccessful) {
            throw Exception(createUserResponse.message())
        } else {
            val response = createUserResponse.body()!!.first()
            if (response.error != null) {
                throw Exception(response.error)
            } else {
                db.collection(COLLECTION_NAME)
                        .document(getUID())
                        .collection(COLLECTION_ENROLLED_USERS)
                        .document(response.uid!!)
                        .setOrThrow(
                                EnrolledUser(
                                        uid = response.uid,
                                        enrolledOn = Timestamp.now(),
                                        enrolledBy = getUID(),
                                        enrollmentStepsCompleted = EnrollmentStepsCompleted(),
                                        name = mobile,
                                        enrolledByName = enrolledByName,
                                        mobileNumber = mobile

                                )
                        )
            }

            return response
        }
    }

    suspend fun checkMobileForExistingRegistrationElseSendOtp(mobile: String): RegisterMobileNoResponse {
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

    suspend fun verifyOtp(token: String, otp: String): VerifyOtpResponse {
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
            name: String,
            latitude: Double,
            longitude: Double,
            address: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow(
                        mapOf(
                                "name" to name,
                                "enrollmentStepsCompleted.userDetailsUploaded" to true,
                                "enrolledFromLocation.latitude" to latitude,
                                "enrolledFromLocation.longitude" to longitude,
                                "enrolledFromLocation.completeAddress" to address
                        )
                )
    }

    suspend fun updateUserProfilePicture(
            userId: String,
            profilePic: String,
            thumbnailPic: String?
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow(mapOf(
                        "profilePic" to profilePic,
                        "profilePic_thumbnail" to thumbnailPic
                ))
    }

    suspend fun setUserDetailsAsFilled(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.userDetailsUploaded", true)

    }

    suspend fun setProfilePictureAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.profilePicUploaded", true)
    }

    suspend fun setInterestAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.interestUploaded", true)
    }

    suspend fun setExperienceAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.experienceUploaded", true)
    }

    suspend fun setCurrentAddressAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.currentAddressUploaded", true)
    }

    suspend fun setAadharAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.aadharDetailsUploaded", true)
    }

    suspend fun setBankDetailsAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.bankDetailsUploaded", true)
    }

    suspend fun setDrivingDetailsAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.drivingLicenseDetailsUploaded", true)
    }

    suspend fun setPANDetailsAsUploaded(
            userId: String
    ) {

        db.collection(COLLECTION_NAME)
                .document(getUID())
                .collection(COLLECTION_ENROLLED_USERS)
                .document(userId)
                .updateOrThrow("enrollmentStepsCompleted.panDetailsUploaded", true)
    }

    suspend fun loadCityAndStateUsingPincode(pinCode:String):PincodeResponse{
        val pincodeResponse = createUserApi.loadCityAndStateUsingPincode(AppConstants.PINCODE_URL+pinCode)

        if (!pincodeResponse.isSuccessful) {
            throw Exception(pincodeResponse.message())
        } else {
            val response = pincodeResponse.body()!!.first()
            return response
        }
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = "Ambassador_Enrolled_User"
        private const val COLLECTION_ENROLLED_USERS = "Enrolled_Users"
    }
}