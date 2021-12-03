package com.gigforce.verification.joiningVerficationForms

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModel
import com.gigforce.verification.verificationCore.aadhar.BaseAadharDetailInfoFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoiningAadhaarVerificationFragment : BaseAadharDetailInfoFragment(
    fragmentName = LOG_TAG
) {

    companion object{
        const val LOG_TAG = "JoiningAadhaarVerificationFragment"

        const val INTENT_EXTRA_USER_ID = "user_id"
        const val INTENT_EXTRA_JOB_PROFILE_ID = "job_profile_id"
    }

    private val sharedViewModel : SharedVerificationViewModel by activityViewModels()

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
        data: AadhaarDetailsDataModel
    ) {
        sharedViewModel.aadhaarInfoSubmitted()
        findNavController().navigateUp()
    }
}