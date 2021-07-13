package com.gigforce.lead_management

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.ambassador.*
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.EnrollmentInfo
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.EventLogs.setOrThrow
import com.google.firebase.Timestamp
import javax.inject.Inject


class LeadManagementRepo @Inject constructor(
    private val buildConfig: IBuildConfigVM
    ) : BaseFirestoreDBRepository() {

    //todo shift to di
    private val createUserApi: CreateUserAccEnrollmentAPi = RetrofitFactory.createUserAccEnrollmentAPi()

    suspend fun checkMobileForExistingRegistrationElseSendOtp(mobile: String): RegisterMobileNoResponse {
        val registerUserRequest = createUserApi.registerMobile(
            buildConfig.getCreateOrSendOTPUrl(),
            RegisterMobileNoRequest(
                mobile
            )
        )

        if (!registerUserRequest.isSuccessful) {
            throw Exception(registerUserRequest.message())
        } else {
            return registerUserRequest.body()!!
        }
    }

    suspend fun verifyOtp(token: String, otp: String): VerifyOtpResponse {
        val verifyOtpResponse = createUserApi.verifyOtp(
            buildConfig.getVerifyOTPURL(),
            token,
            otp
        )

        if (!verifyOtpResponse.isSuccessful) {
            throw Exception(verifyOtpResponse.message())
        } else {
            return verifyOtpResponse.body()!!
        }
    }

    suspend fun createUser(
        createUserUrl: String,
        mobile: String,
        enrolledByName: String,
        latitude: Double,
        longitude: Double,
        fullAddress: String
    ): CreateUserResponse {
        val createUserResponse = createUserApi.createUser(
            createUserUrl, listOf(
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
                response.uid?.let {
                    createProfileDataForUser(
                        uid = it,
                        mobile = mobile,
                        latitude = latitude,
                        longitude = longitude,
                        fullAddress = fullAddress
                    )
                }

            }

            return response
        }
    }

    private suspend fun createProfileDataForUser(
        uid: String,
        mobile: String,
        latitude: Double,
        longitude: Double,
        fullAddress: String

    ) {

        val profileData = ProfileData(
            loginMobile = "+91${mobile}",
            contact = ArrayList(
                listOf(
                    Contact(
                        phone = "+91${mobile}",
                        email = ""
                    )
                )
            ),
            createdOn = Timestamp.now(),
            enrolledBy = EnrollmentInfo(
                id = getUID(),
                enrolledOn = Timestamp.now(),
                enrolledLocationLatitude = latitude,
                enrolledLocationLongitude = longitude,
                enrolledLocationAddress = fullAddress
            )
        )

        db.collection("Profiles")
            .document(uid)
            .setOrThrow(profileData)
    }


    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = ""

    }

}