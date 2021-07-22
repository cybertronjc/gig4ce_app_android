package com.gigforce.lead_management.repositories

import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.JoiningProfileService
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import android.util.Log
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigApplication
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.datamodels.ambassador.*
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.EnrollmentInfo
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.repo.UserEnrollmentRepository
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.EventLogs.setOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeadManagementRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val joiningProfileRemoteService: JoiningProfileService,
    private val buildConfig: IBuildConfigVM
) {

    companion object {
        private const val COLLECTION_JOININGS = "Joinings"
        private const val COLLECTION_PROFILE = "Profiles"
    }

    /**
     * Collection references
     */
    private val createUserApi: CreateUserAccEnrollmentAPi = RetrofitFactory.createUserAccEnrollmentAPi()
    //Collections Refs
    private val joiningsCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_JOININGS)
    }

    private val profileCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_PROFILE)
    }


    suspend fun fetchJoinings(): List<Joining> =
        joiningsCollectionRef
            .whereEqualTo(
                "joiningTLUid",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
            .getOrThrow()
            .toObjects(Joining::class.java)

    suspend fun saveReference(
        userUid: String,
        name: String,
        relation: String,
        contactNo: String
    ) = profileCollectionRef
        .document(userUid)
        .updateOrThrow(
            mapOf(
                "reference.name" to name,
                "reference.relation" to relation,
                "reference.contactNo" to contactNo
            )
        )

    /**
     * For Job Profiles for referral screen provide userUid : null,
     * For Selecting Job Profile
     */

    suspend fun getJobProfiles(
        tlUid: String
    ): List<JobProfileOverview> = joiningProfileRemoteService.getProfiles(
        getProfilesUrl = buildConfig.getJobProfiles(),
        tlUid = tlUid,
        userUid = null
    ).bodyOrThrow()

    suspend fun getJobProfilesWithStatus(
        tlUid: String,
        userUid: String
    ): List<JobProfileOverview> = joiningProfileRemoteService.getProfiles(
        getProfilesUrl = buildConfig.getJobProfiles(),
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun getJobProfileDetails(
        jobProfileId: String,
        tlUid: String,
        userUid: String
    ): JobProfileDetails = joiningProfileRemoteService.getProfileDetails(
        getProfilesDetailsUrl = buildConfig.getJobProfiles(),
        jobProfileId = jobProfileId,
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun checkMobileForExistingRegistrationElseSendOtp(mobile: String, url: String): RegisterMobileNoResponse {
        val registerUserRequest = createUserApi.registerMobile(
            url,
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

    suspend fun getUserAuthStatus(mobileNo : String): UserAuthStatusModel {
        var userAuthStatus = createUserApi.getGigersAuthStatus(buildConfig.getUserRegisterInfoUrl(),mobileNo)
        if(userAuthStatus.isSuccessful){
            return userAuthStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : checkIfSignInOrSignup Method ${userAuthStatus.message()}")
            throw Exception("Issue in Authentication result ${userAuthStatus.message()}")
        }
    }

    suspend fun createUser(
        mobile: String,
        enrolledByName: String
    ): CreateUserResponse {
        val createUserResponse = createUserApi.createUser(
            buildConfig.getCreateUserUrl(), listOf(
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
                addUserToAmbassadorEnrolledUserList(
                    response.uid!!,
                    mobile,
                    enrolledByName
                )
                response.uid?.let {
                    createProfileDataForUser(
                        uid = it,
                        mobile = mobile,
                        enrolledByName = enrolledByName
                    )
                }

            }

            return response
        }
    }
    private suspend fun createProfileDataForUser(
        uid: String,
        mobile: String,
        enrolledByName: String

    ) {

        val profileData = ProfileData(
            loginMobile = "+91${mobile}",
            contact = ArrayList(
                listOf(
                    Contact(
                        phone = "+91${mobile}",
                        email = "",
                    )
                )
            ),
            createdOn = Timestamp.now(),
            enrolledBy = EnrollmentInfo(
                id = uid,
                enrolledOn = Timestamp.now()
            )
        )

        profileCollectionRef
            .document(uid)
            .setOrThrow(profileData)
    }

    private suspend fun addUserToAmbassadorEnrolledUserList(
        uid: String,
        mobile: String,
        enrolledByName: String
    ) {
        firebaseFirestore.collection("Ambassador_Enrolled_User")
            .document(firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid)
            .collection("Enrolled_Users")
            .document(uid)
            .setOrThrow(
                EnrolledUser(
                    uid = uid,
                    enrolledOn = Timestamp.now(),
                    enrolledBy = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                    enrollmentStepsCompleted = EnrollmentStepsCompleted(),
                    name = mobile,
                    enrolledByName = enrolledByName,
                    mobileNumber = mobile,
                    locationLogs = listOf(
                        LocationLog(
                            entryType = "created_by_ambassador"
                        )
                    )
                )
            )
    }


}