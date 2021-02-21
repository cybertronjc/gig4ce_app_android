package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.common_ui.StringConstants
import kotlinx.android.synthetic.main.fragment_embassador_program_requirement_screen.*

@ExperimentalStdlibApi
class AmbassadorEnrollmentRequirementFragment : BaseFragment(),
        AmbassadorEnrolledSuccessfullyDialogFragmentListeners {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val gigVerificationViewModel: GigVerificationViewModel by viewModels()

    private var profileData: ProfileData? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var redirectToNextStep = false


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_embassador_program_requirement_screen, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForBackPress()
        initUi()
        initViewModel()
    }

    private fun checkForBackPress() {
        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.BACK_PRESSED.value, false) == true
            ) {
                redirectToNextStep = false
                navFragmentsData?.setData(bundleOf())
            }
        }
    }


    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }

        apply_amb_btn.setOnClickListener {
            AmbassadorEnrolledDialogFragment.launch(childFragmentManager, this@AmbassadorEnrollmentRequirementFragment)
        }

        bank_details_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(R.id.addBankDetailsInfoFragment, bundleOf(
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            ))
        }

        current_address_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(R.id.addCurrentAddressFragment, bundleOf(
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            ))
        }

        profile_photo_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(
                    R.id.addProfilePictureFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT,
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            )
            )
        }
    }

    private fun initViewModel() {
        val completedItems = LinkedHashMap<String, Boolean>()
        profileViewModel.getProfileData()
                .observe(viewLifecycleOwner, Observer {
                    this.profileData = it
                    updateProgress()
                    completedItems["profile"] = it.hasUserUploadedProfilePicture()
                    if (it.hasUserUploadedProfilePicture()) {
                        profile_pic_check_iv.setImageResource(R.drawable.ic_done)
                    } else {
                        profile_pic_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                    }
                    completedItems["address"] = !it.address.current.isEmpty()
                    if (it.address.current.isEmpty()) {
                        current_address_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                    } else {
                        current_address_check_iv.setImageResource(R.drawable.ic_done)
                    }
                    gigVerificationViewModel.gigerVerificationStatus
                            .observe(viewLifecycleOwner, Observer {
                                this.gigerVerificationStatus = it
                                updateProgress()
                                completedItems["bank_details"] = it.bankDetailsUploaded
                                if (it.bankDetailsUploaded) {
                                    bank_details_check_iv.setImageResource(R.drawable.ic_done)
                                } else {
                                    bank_details_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                                }
                                checkForRedirection(completedItems)
                            })

                    gigVerificationViewModel.startListeningForGigerVerificationStatusChanges()
                })


    }

    private fun updateProgress() {
        if (gigerVerificationStatus == null || profileData == null)
            return

        val totalSteps = 3
        var completedSteps = 0

        if (gigerVerificationStatus!!.bankDetailsUploaded)
            completedSteps++

        if (profileData!!.hasUserUploadedProfilePicture())
            completedSteps++

        if (!profileData!!.address.current.isEmpty())
            completedSteps++

        steps_completed_tv.text = "$completedSteps/$totalSteps"
        val completedPercentage = (completedSteps * 100) / totalSteps
        pb_amb_req.progress = completedPercentage
    }

    override fun onStartingOnBoardingGigersClicked() {
        navigate(R.id.ambassadorEnrolledUsersListFragment)
    }

    override fun onViewGigDetailsClicked() {

    }

    private fun checkForRedirection(map: LinkedHashMap<String, Boolean>) {
        if (!redirectToNextStep) return
        for (i in map.keys) {
            if (map[i] == false) {
                when (i) {
                    "profile" -> {
                        navigate(
                                R.id.addProfilePictureFragment, bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT,
                                AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                        ))
                    }
                    "address" -> {
                        navigate(R.id.addCurrentAddressFragment, bundleOf(
                                AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                        ))
                    }
                    "bank_details" ->
                        navigate(R.id.addBankDetailsInfoFragment, bundleOf(
                                AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                        )
                        )

                }
                break
            }

        }


    }

}