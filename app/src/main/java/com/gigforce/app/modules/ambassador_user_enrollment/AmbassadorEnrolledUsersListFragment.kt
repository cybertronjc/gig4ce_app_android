package com.gigforce.app.modules.ambassador_user_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile.ConfirmOtpFragment
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.VerticalItemDecorator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*

class AmbassadorEnrolledUsersListFragment : BaseFragment(),
        EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener {

    private val viewModel: AmbassadorEnrollViewModel by viewModels()

    private val enrolledUserAdapter: EnrolledUsersRecyclerAdapter by lazy {
        EnrolledUsersRecyclerAdapter(requireContext()).apply {
            this.setListener(this@AmbassadorEnrolledUsersListFragment)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_embassador_enrolled_users_list, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()
        getEnrolledUsers()
    }

    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }

        create_profile_btn.setOnClickListener {
            navigate(R.id.checkMobileFragment)
        }

        createProfileBtn.setOnClickListener {
            navigate(R.id.checkMobileFragment)
        }

        enrolled_users_rv.layoutManager = LinearLayoutManager(activity?.applicationContext)
        enrolled_users_rv.addItemDecoration(VerticalItemDecorator(30))
        enrolled_users_rv.adapter = enrolledUserAdapter
    }

    private fun initViewModel() {
        viewModel.enrolledUsers
                .observe(viewLifecycleOwner, Observer {

                    if (it.isEmpty()) {
                        enrolledUserAdapter.setData(emptyList())
                        no_users_enrolled_layout.visible()
                        createProfileBtn.gone()
                        total_complete_profile_tv.gone()
                        total_incomplete_profile_tv.gone()
                    } else {
                        no_users_enrolled_layout.gone()
                        createProfileBtn.visible()
                        enrolledUserAdapter.setData(it)
                        total_complete_profile_tv.visible()

                        val totalCompleteProfiles = it.count { it.enrollmentStepsCompleted.allStepsCompleted() }
                        val totalInCompleteProfiles = it.count { it.enrollmentStepsCompleted.allStepsCompleted().not() }

                        total_complete_profile_tv.text = buildSpannedString {
                            append("Total Completed Profile : ")
                            bold {
                                color(ResourcesCompat.getColor(resources, R.color.activated_color, null)) {
                                    append(totalCompleteProfiles.toString())
                                }
                            }
                        }

                        total_incomplete_profile_tv.visible()
                        total_incomplete_profile_tv.text = buildSpannedString {
                            append("Total Incomplete Profile : ")
                            bold {
                                color(ResourcesCompat.getColor(resources, R.color.text_orange, null)) {
                                    append(totalInCompleteProfiles.toString())
                                }
                            }
                        }
                    }
                })

        viewModel
                .sendOtpToPhoneNumber
                .observe(viewLifecycleOwner, Observer {
                    it ?: return@Observer

                    when (it) {
                        Lce.Loading -> {
                            UtilMethods.showLoading(requireContext())
                        }
                        is Lce.Content -> {
                            UtilMethods.hideLoading()

                            showToast("Otp Sent")
                            navigate(
                                    R.id.confirmOtpFragment, bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.enrolledUser.uid,
                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.enrolledUser.mobileNumber,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_EDIT,
                                    ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to it.content.enrolledUser.mobileNumber,
                                    ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to it.content.checkMobileResponse.verificationToken
                            )
                            )
                        }
                        is Lce.Error -> {

                            UtilMethods.hideLoading()
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Error")
                                    .setMessage(it.error)
                                    .setPositiveButton("Okay") { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun getEnrolledUsers(){
        viewModel.enrolledUsers
    }

    override fun onBackPressed(): Boolean {

        try {
            findNavController().getBackStackEntry(R.id.mainHomeScreen)
            findNavController().popBackStack(R.id.mainHomeScreen, false)
        } catch (e: Exception) {
            findNavController().popBackStack(R.id.landinghomefragment, false)
        }
        return true
    }

    override fun onUserClicked(enrolledUser: EnrolledUser) {

    }

    override fun onUserEditButtonclicked(enrolledUser: EnrolledUser) {
        viewModel.getMobileNumberAndSendOtpInfo(enrolledUser)
    }
}