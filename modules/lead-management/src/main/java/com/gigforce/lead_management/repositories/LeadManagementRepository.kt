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
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
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
        getProfilesUrl = buildConfig.getGigersUnderTlUrl(),
        tlUid = tlUid,
        userUid = null
    ).bodyOrThrow()

    suspend fun getJobProfilesWithStatus(
        tlUid: String,
        userUid: String
    ): List<JobProfileOverview> = joiningProfileRemoteService.getProfiles(
        getProfilesUrl = buildConfig.getGigersUnderTlUrl(),
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun getJobProfileDetails(
        jobProfileId: String,
        tlUid: String,
        userUid: String
    ): JobProfileDetails = joiningProfileRemoteService.getProfileDetails(
        getProfilesDetailsUrl = buildConfig.getGigersUnderTlUrl(),
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

    suspend fun verifyOtp(token: String, otp: String, url: String): VerifyOtpResponse {
        val verifyOtpResponse = createUserApi.verifyOtp(
           url,
            token,
            otp
        )

        if (!verifyOtpResponse.isSuccessful) {
            throw Exception(verifyOtpResponse.message())
        } else {
            return verifyOtpResponse.body()!!
        }
    }

    suspend fun getUserAuthStatus(mobileNo : String, url: String): UserAuthStatusModel {
        var userAuthStatus = createUserApi.getGigersAuthStatus(url,mobileNo)
        if(userAuthStatus.isSuccessful){
            return userAuthStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : checkIfSignInOrSignup Method ${userAuthStatus.message()}")
            throw Exception("Issue in Authentication result ${userAuthStatus.message()}")
        }
    }

    suspend fun createUser(
        createUserUrl: String,
        mobile: String,
        enrolledByName: String
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
//                    createProfileDataForUser(
//                        uid = it,
//                        mobile = mobile
//                    )
                }

            }

            return response
        }
    }
    suspend fun getJobProfiles1(gigerid: String): List<GigApplication>{
        val allClientActivations = ArrayList<GigApplication>()
        val items = firebaseFirestore.collection("Job_Profiles")
            .whereEqualTo("isActive", true).get()
            .await()

        if (items.documents.isNullOrEmpty()){
            return emptyList()
        }
        val toObjects = items.toObjects(JobProfile::class.java)
        for (i in 0..toObjects.size - 1 ){
            val obj = toObjects[i]
            var jobProfileId = items.documents.get(i).id
            obj.id = toObjects[i].profileId
            obj.id?.let {
                Log.d("profileId", it)
                val jpObject = getJPApplication(it, gigerid)
                Log.d("object", jpObject.toString())
                val jpExplore = GigApplication(jobProfileId,gigerid, jpId = jpObject.id, profileId = obj.profileId, obj.profileName,  obj.cardTitle, obj.cardImage, jpObject.status, obj.title, "Ongoing Applications")
                allClientActivations.add(jpExplore)
            }

        }
        return allClientActivations
    }

    suspend fun getJPApplication(jobProfileId: String, gigerid: String): JpApplication {
        var jpApplication = JpApplication()
        try {
            val items = firebaseFirestore.collection("JP_Applications").whereEqualTo("jpid", jobProfileId)
                .whereEqualTo("gigerId", gigerid).get()
                .await()

            if (items.documents.isNullOrEmpty()) {
                jpApplication = JpApplication(JPId = jobProfileId, gigerId = gigerid)
            } else {
                val toObject = items.toObjects(JpApplication::class.java).get(0)
                toObject.id = items.documents[0].id
                Log.d("status", toObject.toString())
                jpApplication = toObject
            }

        } catch (e: Exception) {

        }

        return jpApplication
    }

    suspend fun getOtherApplications(): List<GigApplication> {
        val allGigApplications = ArrayList<GigApplication>()
        val items = firebaseFirestore.collection("Job_Profiles")
            .whereEqualTo("isActive", true).get()
            .await()

        if (items.documents.isNullOrEmpty()){
            return emptyList()
        }
        val toObjects = items.toObjects(JobProfile::class.java)

        toObjects.forEachIndexed { index, jobProfile ->
            var jobProfileId = items.documents.get(index).id
            jobProfile.id = jobProfile.profileId
            val gigApplication = GigApplication(jobProfileId, "", jpId = jobProfile.id, profileId = jobProfile.profileId, jobProfile.profileName,  jobProfile.cardTitle, jobProfile.cardImage, "", jobProfile.title, "Other Applications")
            allGigApplications.add(gigApplication)
        }

        return allGigApplications

    }

}