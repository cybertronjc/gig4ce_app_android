package com.gigforce.lead_management.repositories

import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.JoiningProfileService
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadManagementRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val joiningProfileRemoteService: JoiningProfileService,
    private val buildConfig: IBuildConfig
) {


    companion object {

        private const val COLLECTION_JOININGS = "Joinings"
        private const val COLLECTION_PROFILE = "Profiles"
    }

    /**
     * Collection references
     */
    private val joiningsCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_JOININGS)
    }

    private val profileCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_PROFILE)
    }


    suspend fun fetchJoiningsQuery(): Query = joiningsCollectionRef
        .whereEqualTo(
            "joiningTLUid",
            firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
        )


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
        getProfilesUrl = buildConfig.getCreateOrSendOTPUrl(),
        tlUid = tlUid,
        userUid = null
    ).bodyOrThrow()

    suspend fun getJobProfilesWithStatus(
        tlUid: String,
        userUid: String
    ): List<JobProfileOverview> = joiningProfileRemoteService.getProfiles(
        getProfilesUrl = buildConfig.getCreateOrSendOTPUrl(),
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun getJobProfileDetails(
        jobProfileId: String,
        tlUid: String,
        userUid: String
    ): JobProfileDetails = joiningProfileRemoteService.getProfileDetails(
        getProfilesDetailsUrl = buildConfig.getCreateOrSendOTPUrl(),
        jobProfileId = jobProfileId,
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()
}