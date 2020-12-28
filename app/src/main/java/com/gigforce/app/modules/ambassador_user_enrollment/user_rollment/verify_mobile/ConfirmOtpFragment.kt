package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorEnrollmentProfile
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_confirm_otp.*

class ConfirmOtpFragment : BaseFragment() {

    private val viewModel: VerifyUserMobileViewModel by viewModels()
    private lateinit var mAmbObj: AmbassadorEnrollmentProfile

    private lateinit var verificationToken: String
    private lateinit var mobileNo: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_confirm_otp, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initListeners()
        initViewModel()
    }

    private fun initUi() {
        enter_mobile_label.text = mAmbObj.enterCodeText
        submitBtn.text = mAmbObj.confirmOtpActionText
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mAmbObj =
                it.getParcelable(StringConstants.AMB_APPLICATION_OBJ.value)
                    ?: AmbassadorEnrollmentProfile()
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {
            mAmbObj =
                it.getParcelable(StringConstants.AMB_APPLICATION_OBJ.value)
                    ?: AmbassadorEnrollmentProfile()
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
        outState.putParcelable(StringConstants.AMB_APPLICATION_OBJ.value, mAmbObj)
    }

    private fun initListeners() {
        we_will_send_otp_label.text = "${mAmbObj.sentOtpText} $mobileNo"

        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }

        ic_back_iv.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun validateDataAndsubmit() {
        if (txt_otp.text?.length != 6) {
            showAlertDialog("", mAmbObj.validOtpText)
            return
        }

        viewModel.checkOtpAndCreateProfile(
            verificationToken,
            txt_otp.text.toString(),
            mobileNo
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.createProfile
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        showToast(mAmbObj.otpConfirmedText)
                        navigate(
                            R.id.addUserDetailsFragment, bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.uid,
                                EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber
                            )
                        )
                    }
                    is Lce.Error -> {
                        UtilMethods.hideLoading()
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