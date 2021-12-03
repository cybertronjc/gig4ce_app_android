package com.gigforce.verification.joiningVerficationForms

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModel
import com.gigforce.verification.verificationCore.driving_license.BaseDrivingLicenseVerificationFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoiningPanVerificationFragment : BaseDrivingLicenseVerificationFragment(
    fragmentName = LOG_TAG
) {
    companion object {
        const val LOG_TAG = "JoiningDLVerificationFragment"

        const val INTENT_EXTRA_USER_ID = "user_id"
        const val INTENT_EXTRA_JOB_PROFILE_ID = "job_profile_id"
    }

    private val sharedViewModel: SharedVerificationViewModel by activityViewModels()

    override fun shouldEnableOcr(): Boolean {
        return true
    }

    override fun shouldUploadEventsToAnalytics(): Boolean {
        return false
    }

    override fun getUserId(): String {
        TODO("Not yet implemented")
    }

    override fun shouldUploadDocumentsToFirebase(): Boolean {
        return false
    }

    override fun dataValidatedAndSubmitted(
        name: String?,
        drivingLicenseNo: String,
        issueDate: String,
        expiryDate: String,
        dateOfBirth: String
    ) {

        sharedViewModel.drivingLicenseInfoSubmitted()
        findNavController().navigateUp()
    }
}