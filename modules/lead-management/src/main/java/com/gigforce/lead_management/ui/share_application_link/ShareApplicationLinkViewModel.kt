package com.gigforce.lead_management.ui.share_application_link

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningSignUpInitiatedMode
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.exceptions.TryingToDowngradeJoiningStatusException
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ShareApplicationLinkViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val profileFirebaseRepository: ProfileFirebaseRepository,
    private val buildConfig: IBuildConfig
) : ViewModel() {

    companion object {
        private const val TAG = "ShareApplicationLinkViewModel"
    }

    private val _viewState = MutableLiveData<Lce<List<JobProfileOverview>>>()
    val viewState: LiveData<Lce<List<JobProfileOverview>>> = _viewState

    private val _referralViewState = MutableLiveData<ShareReferralViewState>()
    val referralViewState: LiveData<ShareReferralViewState> = _referralViewState

    init {
        getJobProfileForSharing()
    }

    //Data
    private var jobProfiles: List<JobProfileOverview> = emptyList()
    private var jobProfilesShownOnView: List<JobProfileOverview> = emptyList()
    private var currentlySelectedGigIndex: Int = -1

    fun getSelectedJobProfile(): JobProfileOverview? {
        if (currentlySelectedGigIndex == -1) return null

        return if (jobProfilesShownOnView.size > currentlySelectedGigIndex)
            jobProfilesShownOnView[currentlySelectedGigIndex]
        else
            null
    }

    private fun getJobProfileForSharing() = viewModelScope.launch {
        _viewState.postValue(Lce.loading())

        try {
            logger.d(TAG, "fetching job profiles...")

            jobProfiles = leadManagementRepository.getJobProfiles(
                tlUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
            )
            jobProfilesShownOnView = jobProfiles
            _viewState.value = Lce.content(jobProfiles)

            logger.d(TAG, "received ${jobProfiles.size} job profiles from server")

        } catch (e: Exception) {
            _viewState.value = Lce.error("Unable to load Job Profiles")
            logger.e(
                TAG,
                " getJobProfileForSharing()",
                e
            )
        }
    }


    fun searchJobProfiles(
        searchString: String
    ) {
        logger.d(TAG, "search job profiles called , search string : '${searchString}'")

        if (searchString.isEmpty()) {
            jobProfilesShownOnView = jobProfiles
            _viewState.value = Lce.content(jobProfilesShownOnView)
            return
        }

        jobProfilesShownOnView = jobProfiles.filter {
            it.tradeName?.contains(searchString, true) ?: false
                    || it.profileName?.contains(searchString, true) ?: false
        }

        _viewState.value = Lce.content(jobProfilesShownOnView)
        logger.d(TAG, "Job profiles found after search : ${jobProfilesShownOnView.size}")
    }


    fun selectJobProfile(
        jobProfileId: String
    ) {
        logger.d(TAG, "selecting job profile $jobProfileId...")

        if (currentlySelectedGigIndex == -1) {
            currentlySelectedGigIndex = jobProfilesShownOnView.indexOfFirst {
                it.jobProfileId == jobProfileId
            }
            logger.d(
                TAG,
                "no job profile selected yet, selecting index no $currentlySelectedGigIndex"
            )

            if (currentlySelectedGigIndex != -1) {
                jobProfilesShownOnView[currentlySelectedGigIndex].isSelected = true
            }
        } else {

            val newSelectedItemIndex = jobProfilesShownOnView.indexOfFirst {
                it.jobProfileId == jobProfileId
            }

            if (newSelectedItemIndex == currentlySelectedGigIndex) {
                //Item Already selected
                return
            }

            jobProfilesShownOnView[currentlySelectedGigIndex].isSelected = false
            jobProfilesShownOnView[newSelectedItemIndex].isSelected = true

            logger.d(
                TAG,
                "already profile selected yet, selecting index no $newSelectedItemIndex, deselecting : $currentlySelectedGigIndex"
            )

            currentlySelectedGigIndex = newSelectedItemIndex
        }

        _viewState.value = Lce.content(jobProfilesShownOnView)
    }


    fun sendAppReferralLink(
        name: String,
        mobileNumber: String,
        jobProfileId: String,
        jobProfileName: String,
        tradeName: String,
        jobProfileIcon: String
    ) = viewModelScope.launch {

        _referralViewState.postValue(ShareReferralViewState.SharingAndUpdatingJoiningDocument)
        val result = createDocumentWithSignUpPending(
            mobileNumber,
            jobProfileId,
            jobProfileName,
            name,
            tradeName,
            jobProfileIcon
        )
        if (!result) return@launch

        val referralLink = try {
            createAppReferralLink()
        } catch (e: Exception) {
            logger.e(TAG,"unable to create referral link",e)
            _referralViewState.postValue(
                ShareReferralViewState.UnableToCreateShareLink(
                    "Unable to create referral link"
                )
            )
            return@launch
        }

        try {
            logger.d(
                TAG,
                "calling share request api...",
                mapOf(
                    "referralType" to ShareReferralType.SHARE_SIGNUP_LINK,
                    "mobile-number" to mobileNumber,
                    "name" to name,
                    "jobProfileName" to jobProfileName,
                )
            )

            leadManagementRepository.sendReferralLink(
                referralType = ShareReferralType.SHARE_SIGNUP_LINK,
                mobileNumber = mobileNumber,
                jobProfileName = jobProfileName,
                name = name,
                shareLink = referralLink,
                tradeName = tradeName
            )

            _referralViewState.postValue(
                ShareReferralViewState.DocumentUpdatedAndReferralShared(
                    shareType = ShareReferralType.SHARE_SIGNUP_LINK,
                    shareLink = referralLink
                )
            )
            logger.d(
                TAG,
                "[Success] Referral shared"
            )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "[Error] in sharing referral",
                e
            )

            _referralViewState.postValue(
                ShareReferralViewState.OpenWhatsAppToShareDocumentSharingDocument(
                    shareType = ShareReferralType.SHARE_SIGNUP_LINK,
                    shareLink = referralLink
                )
            )
        }
    }

    fun sendAppReferralLinkViaOtherApps(
        name: String,
        mobileNumber: String,
        jobProfileId: String,
        jobProfileName: String,
        tradeName: String,
        jobProfileIcon: String
    ) = viewModelScope.launch {

        _referralViewState.postValue(ShareReferralViewState.SharingAndUpdatingJoiningDocument)
        val result = createDocumentWithSignUpPending(
            mobileNumber,
            jobProfileId,
            jobProfileName,
            name,
            tradeName,
            jobProfileIcon
        )
        if (!result) return@launch

        val referralLink = try {
            createAppReferralLink()
        } catch (e: Exception) {
            logger.e(TAG,"unable to create referral link",e)
            _referralViewState.postValue(
                ShareReferralViewState.UnableToCreateShareLink(
                    "Unable to create referral link"
                )
            )
            return@launch
        }

        _referralViewState.postValue(
            ShareReferralViewState.OpenOtherAppsToShareDocumentSharingDocument(
                shareType = ShareReferralType.SHARE_SIGNUP_LINK,
                shareLink = referralLink
            )
        )
    }

    private suspend fun createDocumentWithSignUpPending(
        mobileNumber: String,
        jobProfileId: String,
        jobProfileName: String,
        name: String,
        tradeName: String,
        jobProfileIcon: String
    ): Boolean {
        try {
            logger.d(
                TAG,
                "creating joining document with sign_up_pending status",
                mapOf(
                    "mobile-number" to mobileNumber,
                    "jobProfileId" to jobProfileId,
                    "jobProfileName" to jobProfileName,
                )
            )

            leadManagementRepository.createOrUpdateJoiningDocumentWithStatusSignUpPending(
                userUid = "",
                name = name,
                phoneNumber = mobileNumber,
                jobProfileId = jobProfileId,
                jobProfileName = jobProfileName,
                signUpMode = JoiningSignUpInitiatedMode.BY_LINK,
                lastStatusChangeSource = TAG,
                tradeName = tradeName,
                jobProfileIcon = jobProfileIcon
            )

            logger.d(
                TAG,
                "joining document created"
            )

            return true
        } catch (e: Exception) {
            logger.e(
                TAG,
                "error in creating or updating document, stopping...",
                e
            )

            _referralViewState.postValue(
                ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                    "Unable to create joining document, please try again later"
                )
            )
            return false
        }
    }


    fun sendJobProfileReferralLink(
        userUid: String,
        jobProfileId: String,
        jobProfileName: String,
        tradeName: String,
        jobProfileIcon: String,
        joiningId : String?
    ) = viewModelScope.launch {

        _referralViewState.postValue(ShareReferralViewState.SharingAndUpdatingJoiningDocument)
        val profile = getProfileForUid(userUid) ?: return@launch
        val result = createOrUpdateJoiningPendingDocument(
            userUid,
            jobProfileId,
            jobProfileName,
            profile,
            tradeName,
            jobProfileIcon,
            joiningId
        )
        if (!result) return@launch

        val shareLink = try {
            createJobProfileReferralLink(jobProfileId)
        } catch (e: Exception) {
            logger.e(TAG,"Unable to create share link",e)
            return@launch
        }

        try {
            logger.d(
                TAG,
                "calling share request api...",
                mapOf(
                    "referralType" to ShareReferralType.SHARE_JOB_PROFILE_LINK,
                    "mobile-number" to profile.loginMobile,
                    "name" to profile.name,
                    "jobProfileName" to jobProfileName,
                )
            )

            leadManagementRepository.sendReferralLink(
                referralType = ShareReferralType.SHARE_JOB_PROFILE_LINK,
                mobileNumber = profile.loginMobile,
                jobProfileName = jobProfileName,
                name = profile.name,
                shareLink = shareLink,
                tradeName = tradeName
            )

            _referralViewState.postValue(
                ShareReferralViewState.DocumentUpdatedAndReferralShared(
                  shareType = ShareReferralType.SHARE_JOB_PROFILE_LINK,
                  shareLink = shareLink
                )
            )
            logger.d(
                TAG,
                "[Success] Referral shared"
            )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "[Error] in sharing referral",
                e
            )

            _referralViewState.postValue(
                ShareReferralViewState.OpenWhatsAppToShareDocumentSharingDocument(
                    shareType = ShareReferralType.SHARE_JOB_PROFILE_LINK,
                    shareLink = shareLink
                )
            )
        }
    }

    private suspend fun createOrUpdateJoiningPendingDocument(
        userUid: String,
        jobProfileId: String,
        jobProfileName: String,
        profile: ProfileData,
        tradeName: String,
        jobProfileIcon: String,
        joiningId: String?
    ): Boolean {
        try {
            logger.d(
                TAG,
                "creating joining document with application_pending status",
                mapOf(
                    "uid" to userUid,
                    "jobProfileId" to jobProfileId,
                    "jobProfileName" to jobProfileName,
                )
            )

            leadManagementRepository.createOrUpdateJoiningDocumentWithApplicationPending(
                userUid = userUid,
                name = profile.name,
                phoneNumber = profile.loginMobile,
                jobProfileId = jobProfileId,
                jobProfileName = jobProfileName,
                lastStatusChangeSource = TAG,
                tradeName = tradeName,
                joiningId = joiningId,
                jobProfileIcon = jobProfileIcon
            )

            logger.d(
                TAG,
                "joining document created"
            )

            return true
        } catch (e: TryingToDowngradeJoiningStatusException){

            _referralViewState.postValue(
                ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                    "You've already referred this user for this job profile"
                )
            )
            return false
        }catch (e: Exception) {
            logger.e(
                TAG,
                "error in creating or updating document, stoppping...",
                e
            )

            _referralViewState.postValue(
                ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                    "Unable to create joining document, please try again later"
                )
            )
            return false
        }
    }

    private suspend fun getProfileForUid(
        userUid: String
    ): ProfileData? = try {
        logger.d(
            TAG,
            "fetching profile data with uid : $userUid"
        )

        profileFirebaseRepository.getProfileOrThrow(userUid)
    } catch (e: Exception) {
        _referralViewState.postValue(
            ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                "Unable to create joining document, please try again later"
            )
        )

        logger.e(
            TAG,
            "unable to fetch profile data",
            e
        )

        null
    }

    fun sendJobProfileReferralLinkViaOtherApps(
        userUid: String,
        jobProfileId: String,
        jobProfileName: String,
        tradeName: String,
        jobProfileIcon : String,
        joiningId: String?,
    ) = viewModelScope.launch {

        _referralViewState.postValue(ShareReferralViewState.SharingAndUpdatingJoiningDocument)
        val profile = getProfileForUid(userUid) ?: return@launch
        val result = createOrUpdateJoiningDocWithStatusApplicationPending(
            userUid,
            jobProfileId,
            jobProfileName,
            profile,
            tradeName,
            jobProfileIcon,
            joiningId
        )
        if (!result) return@launch


        try {
            logger.d(
                TAG,
                "creating share link...",
                mapOf(
                    "referralType" to ShareReferralType.SHARE_JOB_PROFILE_LINK,
                    "mobile-number" to profile.loginMobile,
                    "name" to profile.name,
                    "jobProfileName" to jobProfileName,
                )
            )

            val referralLink = createJobProfileReferralLink(jobProfileId)
            _referralViewState.postValue(
                ShareReferralViewState.OpenOtherAppsToShareDocumentSharingDocument(
                    shareType = ShareReferralType.SHARE_JOB_PROFILE_LINK,
                    shareLink = referralLink
                )
            )
            logger.d(
                TAG,
                "[Success] Referral shared"
            )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "[Error] in sharing referral",
                e
            )

            _referralViewState.postValue(
                ShareReferralViewState.UnableToCreateShareLink(
                    "Unable to create share link"
                )
            )
        }
    }

    private suspend fun createOrUpdateJoiningDocWithStatusApplicationPending(
        userUid: String,
        jobProfileId: String,
        jobProfileName: String,
        profile: ProfileData,
        tradeName: String,
        jobProfileIcon : String,
        joiningId: String?
    ) = try {
        logger.d(
            TAG,
            "creating joining document with application_pending status",
            mapOf(
                "uid" to userUid,
                "jobProfileId" to jobProfileId,
                "jobProfileName" to jobProfileName,
            )
        )

        leadManagementRepository.createOrUpdateJoiningDocumentWithApplicationPending(
            userUid = userUid,
            name = profile.name,
            phoneNumber = profile.loginMobile,
            jobProfileId = jobProfileId,
            jobProfileName = jobProfileName,
            lastStatusChangeSource = TAG,
            tradeName = tradeName,
            joiningId = joiningId,
            jobProfileIcon = jobProfileIcon
        )

        logger.d(
            TAG,
            "joining document created"
        )
        true
    } catch (e: TryingToDowngradeJoiningStatusException){

        _referralViewState.postValue(
            ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                "You've already referred this user for this job profile"
            )
        )
        false
    } catch (e: Exception) {
        logger.e(
            TAG,
            "error in creating or updating document, stoppping...",
            e
        )

        _referralViewState.postValue(
            ShareReferralViewState.ErrorInCreatingOrUpdatingDocument(
                "Unable to create joining document, please try again later"
            )
        )
        false
    }


    private suspend fun createAppReferralLink() = suspendCoroutine<String> { cont ->
        Firebase.dynamicLinks.shortLinkAsync {
            longLink = Uri.parse(
                buildDeepLink(
                    Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=${firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid}&is_ambassador=true&latitude=0.0&longitude=0.0")).toString())
                ).toString()
            )
        }.addOnSuccessListener { result ->
            cont.resume(result.shortLink.toString())
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

    private suspend fun createJobProfileReferralLink(
        jobProfileId: String
    ) = suspendCoroutine<String> { cont ->
        Firebase.dynamicLinks.shortLinkAsync {
            longLink = Uri.parse(
                buildDeepLink(
                    Uri.parse("http://www.gig4ce.com/?job_profile_id=$jobProfileId&invite=${firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid}")
                ).toString()
            )
        }.addOnSuccessListener { result ->
            cont.resume(result.shortLink.toString())
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

    private fun buildDeepLink(
        deepLink: Uri
    ): Uri {
        return FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(deepLink.toString()))
            .setDomainUriPrefix(buildConfig.getReferralBaseUrl())
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .setIosParameters(DynamicLink.IosParameters.Builder("com.gigforce.ios").build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Gigforce")
                    .setDescription("Flexible work and learning platform")
                    .setImageUrl(Uri.parse("https://firebasestorage.googleapis.com/v0/b/gig4ce-app.appspot.com/o/app_assets%2Fgigforce.jpg?alt=media&token=f7d4463b-47e4-4b8e-9b55-207594656161"))
                    .build()
            ).buildDynamicLink()
            .uri
    }
}