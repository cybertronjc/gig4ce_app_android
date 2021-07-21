package com.gigforce.lead_management


import android.util.Log
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigApplication
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.ambassador.*
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.EnrollmentInfo
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.extensions.setOrThrow
import com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await


class LeadManagementRepo (
    private val buildConfig: IBuildConfigVM
    ) : BaseFirestoreDBRepository() {


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
                    createProfileDataForUser(
                        uid = it,
                        mobile = mobile
                    )
                }

            }

            return response
        }
    }

    private suspend fun createProfileDataForUser(
        uid: String,
        mobile: String,


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
                enrolledOn = Timestamp.now()
            )
        )

        db.collection("Profiles")
            .document(uid)
            .setOrThrow(profileData)
    }

//    suspend fun fetchGigApplications(gigerid: String): List<GigApplication>{
//
//        try {
//            val otherApplications = getOtherApplications()
//            val ongoingApplications = getJobProfiles(gigerid)
//
//        }catch (e: Exception){
//
//        }
//
//        return emptyList()
//    }

    suspend fun getJobProfiles(gigerid: String): List<GigApplication>{
            val allClientActivations = ArrayList<GigApplication>()
            val items = db.collection("Job_Profiles")
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
                    val jpExplore = GigApplication(jobProfileId,gigerid, jpId = jpObject.id, profileId = obj.profileId, obj.profileName,  obj.cardTitle, obj.cardImage, jpObject.status, obj.title, "ongoing")
                    allClientActivations.add(jpExplore)
                }

            }
            return allClientActivations
        }

    suspend fun getJPApplication(jobProfileId: String, gigerid: String): JpApplication {
        var jpApplication = JpApplication()
        try {
            val items = db.collection("JP_Applications").whereEqualTo("jpid", jobProfileId)
                    .whereEqualTo("gigerId", gigerid).get()
                    .await()

            if (items.documents.isNullOrEmpty()) {
                jpApplication = JpApplication(JPId = jobProfileId, gigerId = getUID())
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
        val items = db.collection("Job_Profiles")
            .whereEqualTo("isActive", true).get()
            .await()

        if (items.documents.isNullOrEmpty()){
            return emptyList()
        }
        val toObjects = items.toObjects(JobProfile::class.java)

        toObjects.forEachIndexed { index, jobProfile ->
            var jobProfileId = items.documents.get(index).id
            jobProfile.id = jobProfile.profileId
            val gigApplication = GigApplication(jobProfileId, "", jpId = jobProfile.id, profileId = jobProfile.profileId, jobProfile.profileName,  jobProfile.cardTitle, jobProfile.cardImage, "", jobProfile.title, "other")
            allGigApplications.add(gigApplication)
        }

        return allGigApplications

    }



    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        private const val COLLECTION_NAME = ""

    }

}