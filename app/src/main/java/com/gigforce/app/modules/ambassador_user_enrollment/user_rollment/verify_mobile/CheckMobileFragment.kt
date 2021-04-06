package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_check_mobile.*
import java.util.regex.Pattern

class CheckMobileFragment : BaseFragment(), UserAlreadyExistDialogFragmentActionListener {

    private val viewModel: VerifyUserMobileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_check_mobile, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initViewModel()
    }

    private fun initListeners() {
        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }

        toolbar_layout.hideActionMenu()
        toolbar_layout.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun validateDataAndsubmit() {

        if (mobile_no_et.text.length != 10) {
            showAlertDialog("", "Enter a valid Mobile No")
            return
        }
        val mobileNo = mobile_no_et.text.toString()
        if (!INDIAN_MOBILE_NUMBER.matcher(mobileNo).matches()) {
            showAlertDialog("", "Enter a valid Mobile No")
            return
        }

        viewModel.checkMobileNo(
            mobile_no_et.text.toString()
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
        viewModel.checkMobileNo
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        showToast("Otp sent")

                        if (it.content.isUserAlreadyRegistered) {
                            //show user already registered dialog
                            showMobileAlreadyRegisterdDialog()
                        } else {
                            navigate(
                                R.id.confirmOtpFragment, bundleOf(
                                    ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to "${mobile_no_et.text}",
                                    ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to it.content.verificationToken
                                )
                            )
                        }
                    }
                    is Lce.Error -> {
                        UtilMethods.hideLoading()
                        showAlertDialog("", it.error)
                    }
                }
            })
    }

    private fun showMobileAlreadyRegisterdDialog() {
        UserAlreadyExistDialogFragment.launch(childFragmentManager, this)

    }

    companion object {
        private val INDIAN_MOBILE_NUMBER = Pattern.compile("^[6-9][0-9]{9}\$")
    }

    override fun onOkayClicked() {

    }
}