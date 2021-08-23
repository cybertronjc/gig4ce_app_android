package com.gigforce.ambassador.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.ambassador.R
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_check_mobile.*
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class CheckMobileFragment : Fragment(), UserAlreadyExistDialogFragmentActionListener {

    private val viewModel: VerifyUserMobileViewModel by viewModels()

    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_check_mobile, container, false)

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
        toolbar_layout.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })
    }

    private fun validateDataAndsubmit() {

        if (mobile_no_et.text.length != 10) {
            showAlertDialog("", getString(R.string.enter_valid_mobile))
            return
        }
        val mobileNo = mobile_no_et.text.toString()
        if (!INDIAN_MOBILE_NUMBER.matcher(mobileNo).matches()) {
            showAlertDialog("", getString(R.string.enter_valid_mobile))
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
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
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
                        showToast(getString(R.string.otp_sent))

                        if (it.content.isUserAlreadyRegistered) {
                            //show user already registered dialog
                            showMobileAlreadyRegisterdDialog()
                        } else {
                            //navigation.navigateTo("")
                            if (mobile_no_et.text.toString().length > 0) {
                                navigation.navigateTo(
                                    "userinfo/confirmOtpFragment", bundleOf(
                                        ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to "${mobile_no_et.text}",
                                        ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to it.content.verificationToken
                                    )
                                )
                                mobile_no_et.setText("")
                            }
//                            navigate(
//                                R.id.confirmOtpFragment, bundleOf(
//                                    ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to "${mobile_no_et.text}",
//                                    ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to it.content.verificationToken
//                                )
//                            )
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