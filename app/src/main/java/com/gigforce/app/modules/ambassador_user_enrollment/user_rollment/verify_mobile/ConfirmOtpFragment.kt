package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_confirm_otp.*

class ConfirmOtpFragment : BaseFragment() {

    private val viewModel: VerifyUserMobileViewModel by viewModels()

    private lateinit var verificationToken: String
    private lateinit var mobileNo: String

    private var userId: String? = null
    private var mode = EnrollmentConstants.MODE_ADD

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_confirm_otp, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initView()
        initListeners()
        initViewModel()
    }

    private fun initView() {
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
    }

    private fun initListeners() {
        we_will_send_otp_label.text = "We sent it to the number +91 - $mobileNo"

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

        ic_back_iv.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun validateDataAndSubmit() {
        if (txt_otp.text?.length != 6) {
            showAlertDialog("", "Enter a valid otp")
            return
        }

        viewModel.checkOtpAndCreateProfile(
                mode = mode,
                token = verificationToken,
                otp = txt_otp.text.toString(),
                mobile = mobileNo
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun initViewModel() {
        viewModel.createProfile
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> {
                            submitBtn.invisible()
                            confirming_otp_pb.visible()
                        }
                        is Lce.Content -> {
                            showToast("Otp Confirmed, Profile Created")

                            if (mode == EnrollmentConstants.MODE_EDIT) {
                                navigate(
                                        R.id.addUserDetailsFragment, bundleOf(
                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                        EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to mobileNo,
                                        EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                )
                                )
                            } else {
                                navigate(
                                        R.id.addUserDetailsFragment, bundleOf(
                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.uid,
                                        EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber,
                                        EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                )
                                )
                            }
                        }
                        is Lce.Error -> {
                            confirming_otp_pb.gone()
                            submitBtn.visible()

                            showAlertDialog("", it.error)
                        }
                    }
                })
    }

    companion object {
        const val INTENT_EXTRA_MOBILE_NO = "mobileNo"
        const val INTENT_EXTRA_OTP_TOKEN = "otp_token"
    }
}