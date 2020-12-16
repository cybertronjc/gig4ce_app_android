package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import kotlinx.android.synthetic.main.fragment_embassador_program_requirement_screen.*

class AmbassadorEnrollmentRequirementFragment : BaseFragment(),
    AmbassadorEnrolledSuccessfullyDialogFragmentListeners {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val gigVerificationViewModel: GigVerificationViewModel by viewModels()

    private var profileData : ProfileData? = null
    private var gigerVerificationStatus : GigerVerificationStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_embassador_program_requirement_screen, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()
    }

    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }

        apply_amb_btn.setOnClickListener {
            AmbassadorEnrolledDialogFragment.launch(childFragmentManager,this@AmbassadorEnrollmentRequirementFragment)
        }
    }

    private fun initViewModel() {
        profileViewModel.getProfileData()
            .observe(viewLifecycleOwner, Observer {
                this.profileData = it
                updateProgress()

                if (it.hasUserUploadedProfilePicture()) {
                    profile_pic_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_green)
                    profile_pic_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                } else {
                    profile_pic_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_yellow)
                    profile_pic_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                }

                if (it.address.current.isEmpty()) {
                    current_address_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_green)
                    current_address_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                } else {
                    current_address_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_yellow)
                    current_address_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                }
            })

        gigVerificationViewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                this.gigerVerificationStatus = it
                updateProgress()

                if (it.bankDetailsUploaded) {
                    bank_details_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_green)
                    bank_details_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                } else {
                    bank_details_check_iv.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_yellow)
                    bank_details_check_iv.setImageResource(R.drawable.ic_baseline_check_32)
                }
            })

        gigVerificationViewModel.startListeningForGigerVerificationStatusChanges()
    }

    private fun updateProgress() {
        if(gigerVerificationStatus == null || profileData == null)
            return

        val totalSteps = 3
        var completedSteps = 0

        if(gigerVerificationStatus!!.bankDetailsUploaded)
            completedSteps++

        if(profileData!!.hasUserUploadedProfilePicture())
            completedSteps++

        if(!profileData!!.address.current.isEmpty())
            completedSteps++

        steps_completed_tv.text = "$completedSteps/$totalSteps"
        val completedPercentage = (completedSteps * 100) / totalSteps
        pb_amb_req.progress = completedPercentage
    }

    override fun onStartingOnBoardingGigersClicked() {
        TODO()
    }

    override fun onViewGigDetailsClicked() {
        TODO("Not yet implemented")
    }

}