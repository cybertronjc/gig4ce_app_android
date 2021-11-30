package com.gigforce.verification.joiningVerficationForms

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
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

        fun launch(
            userId : String,
            jobProfileId : String = "",
        ){
           val alreadyShownAadharFragment =  childFragmentManager.findFragmentByTag(LOG_TAG)

            if(alreadyShownAadharFragment != null){
                (alreadyShownAadharFragment as JoiningAadhaarVerificationFragment).setOnSubmissionListener(submissionListener)
            } else{
                JoiningAadhaarVerificationFragment().apply {

                    arguments = bundleOf(
                        INTENT_EXTRA_USER_ID to userId,
                        INTENT_EXTRA_JOB_PROFILE_ID to jobProfileId
                    )
                    setOnSubmissionListener(submissionListener)
                }
            }
        }

        fun checkForMissingAadhaarFragmentIfExistApply(
            childFragmentManager : FragmentManager,
            parentFragment : Fragment,
            submissionListener : JoiningAadhaarVerificationFragmentSubmissionListener
        ){

        }
    }

    private lateinit var submissionListener :JoiningAadhaarVerificationFragmentSubmissionListener

    fun setOnSubmissionListener(
        submissionListener: JoiningAadhaarVerificationFragmentSubmissionListener
    ){
        this.submissionListener = submissionListener
    }

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
}