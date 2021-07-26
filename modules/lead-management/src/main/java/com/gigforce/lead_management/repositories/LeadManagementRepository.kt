package com.gigforce.lead_management.repositories

import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.JoiningProfileService
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.extensions.addOrThrow
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.lead_management.exceptions.TryingToDowngradeJoiningStatusException
import com.gigforce.lead_management.exceptions.UserDoesNotExistInProfileException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadManagementRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val joiningProfileRemoteService: JoiningProfileService
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


    fun fetchJoiningsQuery(): Query = joiningsCollectionRef
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
        tlUid = tlUid,
        userUid = null
    ).bodyOrThrow()

    suspend fun getJobProfilesWithStatus(
        tlUid: String,
        userUid: String
    ): List<JobProfileOverview> = joiningProfileRemoteService.getProfiles(
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun getJobProfileDetails(
        jobProfileId: String,
        tlUid: String,
        userUid: String
    ): JobProfileDetails = joiningProfileRemoteService.getProfileDetails(
        jobProfileId = jobProfileId,
        tlUid = tlUid,
        userUid = userUid
    ).bodyOrThrow()

    suspend fun assignGigs(
        assignGigRequest: AssignGigRequest
    ) = joiningProfileRemoteService.createGigs(assignGigRequest)
        .bodyOrThrow()

    suspend fun createOrUpdateJoiningDocumentWithStatusSignUpPending(
        userUid: String,
        name: String,
        phoneNumber: String,
        jobProfileId: String,
        jobProfileName: String,
        signUpMode: String,
        lastStatusChangeSource: String
    ) {

        val getJobProfileLink = joiningsCollectionRef
            .whereEqualTo(
                "joiningTLUid",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
            .whereEqualTo("phoneNumber", phoneNumber)
            .whereEqualTo("jobProfileId", jobProfileId)
            .getOrThrow()

        if (!getJobProfileLink.isEmpty) {

            val firstJoiningId = getJobProfileLink.first().id
            val existingJoiningStatus = getJobProfileLink.first().getString("status")

            if (existingJoiningStatus != null &&
                isExistingStatusHigherThan(
                    existingStatusString = existingJoiningStatus,
                    newStatus = JoiningStatus.SIGN_UP_PENDING
                )
            ) {
                throw TryingToDowngradeJoiningStatusException(
                    documentId = firstJoiningId,
                    existingStatus = existingJoiningStatus,
                    newStatus = JoiningStatus.SIGN_UP_PENDING.getStatusString()
                )
            }

            joiningsCollectionRef
                .document(firstJoiningId)
                .updateOrThrow(
                    mapOf(
                        "updatedOn" to Timestamp.now(),
                        "status" to JoiningStatus.SIGN_UP_PENDING.getStatusString(),
                        "lastStatusChangeSource" to lastStatusChangeSource,
                        "jobProfileIdInvitedFor" to jobProfileId,
                        "jobProfileNameInvitedFor" to jobProfileName,
                        "signUpMode" to signUpMode,
                        "name" to name,
                        "uid" to userUid
                    )
                )

        } else {
            joiningsCollectionRef.addOrThrow(
                Joining(
                    uid = userUid,
                    joiningStartedOn = Timestamp.now(),
                    updatedOn = Timestamp.now(),
                    joiningTLUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                    status = JoiningStatus.SIGN_UP_PENDING.getStatusString(),
                    name = name,
                    phoneNumber = phoneNumber,
                    profilePicture = null,
                    profilePictureThumbnail = null,
                    jobProfileIdInvitedFor = jobProfileId,
                    jobProfileNameInvitedFor = jobProfileName,
                    signUpMode = signUpMode,
                    lastStatusChangeSource = lastStatusChangeSource
                )
            )
        }
    }

    @Throws(UserDoesNotExistInProfileException::class)
    suspend fun createOrUpdateJoiningDocumentWithApplicationPending(
        userUid: String,
        name: String,
        jobProfileId: String,
        jobProfileName: String,
        phoneNumber: String = "",
        lastStatusChangeSource: String
    ) {
        val getProfileForUid = profileCollectionRef
            .document(userUid)
            .getOrThrow()

        if (!getProfileForUid.exists()) {
            throw UserDoesNotExistInProfileException(userUid)
        }

        val userMobileNo: String = if (phoneNumber.isEmpty()) {
            getProfileForUid.get("loginMobile") as String
        } else {
            phoneNumber
        }

        val getJobProfileLink = joiningsCollectionRef
            .whereEqualTo(
                "joiningTLUid",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
            .whereEqualTo("phoneNumber", userMobileNo)
            .whereEqualTo("jobProfileId", jobProfileId)
            .getOrThrow()

        if (getJobProfileLink.isEmpty) {

            joiningsCollectionRef.addOrThrow(
                Joining(
                    uid = userUid,
                    joiningStartedOn = Timestamp.now(),
                    updatedOn = Timestamp.now(),
                    joiningTLUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                    status = JoiningStatus.APPLICATION_PENDING.getStatusString(),
                    name = name,
                    phoneNumber = userMobileNo,
                    profilePicture = null,
                    profilePictureThumbnail = null,
                    jobProfileIdInvitedFor = jobProfileId,
                    jobProfileNameInvitedFor = jobProfileName,
                    signUpMode = null,
                    lastStatusChangeSource = lastStatusChangeSource
                )
            )
        } else {
            val existingJoiningId = getJobProfileLink.first().id
            val existingJoiningStatus = getJobProfileLink.first().getString("status")

            if (existingJoiningStatus != null &&
                isExistingStatusHigherThan(
                    existingStatusString = existingJoiningStatus,
                    newStatus = JoiningStatus.APPLICATION_PENDING
                )
            ) {
                throw TryingToDowngradeJoiningStatusException(
                    documentId = existingJoiningId,
                    existingStatus = existingJoiningStatus,
                    newStatus = JoiningStatus.APPLICATION_PENDING.getStatusString()
                )
            }


            joiningsCollectionRef
                .document(existingJoiningId)
                .updateOrThrow(
                    mapOf(
                        "updatedOn" to Timestamp.now(),
                        "status" to JoiningStatus.APPLICATION_PENDING.getStatusString(),
                        "lastStatusChangeSource" to lastStatusChangeSource
                    )
                )
        }
    }


    @Throws(UserDoesNotExistInProfileException::class)
    suspend fun createOrUpdateJoiningDocumentWithJoiningPending(
        userUid: String,
        name: String,
        jobProfileId: String,
        jobProfileName: String,
        phoneNumber: String = "",
        lastStatusChangeSource: String
    ) {
        val getProfileForUid = profileCollectionRef
            .document(userUid)
            .getOrThrow()

        if (!getProfileForUid.exists()) {
            throw UserDoesNotExistInProfileException(userUid)
        }

        val userMobileNo: String = if (phoneNumber.isEmpty()) {
            getProfileForUid.get("loginMobile") as String
        } else {
            phoneNumber
        }

        val getJobProfileLink = joiningsCollectionRef
            .whereEqualTo(
                "joiningTLUid",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
            .whereEqualTo("phoneNumber", userMobileNo)
            .whereEqualTo("jobProfileId", jobProfileId)
            .getOrThrow()

        if (getJobProfileLink.isEmpty) {

            joiningsCollectionRef.addOrThrow(
                Joining(
                    uid = userUid,
                    joiningStartedOn = Timestamp.now(),
                    updatedOn = Timestamp.now(),
                    joiningTLUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                    status = JoiningStatus.JOINING_PENDING.getStatusString(),
                    name = name,
                    phoneNumber = userMobileNo,
                    profilePicture = null,
                    profilePictureThumbnail = null,
                    jobProfileIdInvitedFor = jobProfileId,
                    jobProfileNameInvitedFor = jobProfileName,
                    signUpMode = null,
                    lastStatusChangeSource = lastStatusChangeSource
                )
            )
        } else {
            val existingJoiningId = getJobProfileLink.first().id
            val existingJoiningStatus = getJobProfileLink.first().getString("status")

            if (existingJoiningStatus != null &&
                isExistingStatusHigherThan(
                    existingStatusString = existingJoiningStatus,
                    newStatus = JoiningStatus.JOINING_PENDING
                )
            ) {
                throw TryingToDowngradeJoiningStatusException(
                    documentId = existingJoiningId,
                    existingStatus = existingJoiningStatus,
                    newStatus = JoiningStatus.JOINING_PENDING.getStatusString()
                )
            }

            joiningsCollectionRef
                .document(existingJoiningId)
                .updateOrThrow(
                    mapOf(
                        "updatedOn" to Timestamp.now(),
                        "status" to JoiningStatus.JOINING_PENDING.getStatusString(),
                        "lastStatusChangeSource" to lastStatusChangeSource
                    )
                )
        }
    }

    private fun isExistingStatusHigherThan(
        existingStatusString: String,
        newStatus: JoiningStatus
    ): Boolean {

        if (existingStatusString.isEmpty())
            return false

        val existingStatus = JoiningStatus.fromValue(existingStatusString)
        if (existingStatus == JoiningStatus.JOINED) {

            return newStatus != JoiningStatus.JOINED
        } else if (existingStatus == JoiningStatus.JOINING_PENDING) {

            return newStatus == JoiningStatus.APPLICATION_PENDING ||
                    newStatus == JoiningStatus.SIGN_UP_PENDING

        } else if (existingStatus == JoiningStatus.APPLICATION_PENDING) {

            return newStatus == JoiningStatus.SIGN_UP_PENDING
        } else {
            return false
        }
    }

     suspend fun sendReferralLink(
        referralType: String,
        mobileNumber: String,
        jobProfileName: String,
        name: String,
        shareLink :String
    ) {



    }

}