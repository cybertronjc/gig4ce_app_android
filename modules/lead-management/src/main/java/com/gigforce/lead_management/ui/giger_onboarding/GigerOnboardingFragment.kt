package com.gigforce.lead_management.ui.giger_onboarding


import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.visible

import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.GigerOnboardingFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class GigerOnboardingFragment : BaseFragment2<GigerOnboardingFragmentBinding>(
    fragmentName = "GigerOnboardingFragment",
    layoutId = R.layout.giger_onboarding_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerOnboardingFragment()
        private const val TAG = "GigforceOnboardingFragment"
        private val INDIAN_MOBILE_NUMBER = Pattern.compile("^[6-9][0-9]{9}\$")
        const val INTENT_EXTRA_MOBILE_NO = "mobileNo"
        const val INTENT_EXTRA_OTP_TOKEN = "otp_token"
        private var isNumberRegistered = false
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: GigerOnboardingViewModel by viewModels()


    override fun viewCreated(
        viewBinding: GigerOnboardingFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initListeners()
        initViewModel()
    }

    val REFERRAL_TAG: String = "referral"
    private fun initListeners() {
        viewBinding.submitButton.setOnClickListener {
            if (viewBinding.submitButton.tag?.toString().equals(REFERRAL_TAG)) {
                //navigate to referral
                navigation.navigateTo(
                    dest = "LeadMgmt/pickProfileForReferralFragment",
                    args = null,
                    navOptions = getNavOptions()
                )

            } else {
                validateDataAndsubmit()
            }

        }

        viewBinding.appBar.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

        viewBinding.changeNumber.setOnClickListener {
            viewBinding.mobileNoEt.requestFocus()
        }

        viewBinding.mobileNoEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                if (viewBinding.mobileNoEt.text.toString().length == 10) {
                    hideKeyboard()
                    viewBinding.submitButton.isEnabled = true
                    viewBinding.submitButton.background =
                        resources.getDrawable(R.drawable.gradient_button)
                } else {
                    viewBinding.submitButton.isEnabled = false
                    viewBinding.submitButton.background =
                        resources.getDrawable(R.drawable.app_gradient_button_disabled)
                }
            }

        })

        viewBinding.createProfileBtn.setOnClickListener {
            logger.d(TAG, "When User not registered send OTP")
            viewModel.sendOtp(
                viewBinding.mobileNoEt.text.toString()
            )
        }

    }

    private fun validateDataAndsubmit() {

        if (viewBinding.mobileNoEt.text.length != 10) {
            showAlertDialog("", "Enter a valid Mobile No")
            return
        }
        val mobileNo = viewBinding.mobileNoEt.text.toString()
        if (!INDIAN_MOBILE_NUMBER.matcher(mobileNo).matches()) {
            showAlertDialog("", "Enter a valid Mobile No")
            return
        }

        viewModel.checkIfNumberAlreadyRegistered(
            viewBinding.mobileNoEt.text.toString()
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

        viewModel.numberRegistered
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }

                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        if (it.content.status) {
                            if (!it.content.isUserRegistered) {
                                isNumberRegistered = false
                                logger.d(
                                    TAG,
                                    "When User is not registered " + isNumberRegistered.toString()
                                )
                                showMobileAlreadyRegisterdDialog()
                            } else {
                                isNumberRegistered = true
                                logger.d(
                                    TAG,
                                    "When User is registered " + isNumberRegistered.toString()
                                )
                                logger.d(TAG, "When User is registered send OTP")
                                viewModel.sendOtp(
                                    viewBinding.mobileNoEt.text.toString()
                                )

                            }
                        } else {
                            logger.d(TAG, "While checking if number is  registered")
                        }
                    }

                    is Lce.Error -> {
                        UtilMethods.hideLoading()
                        showAlertDialog("", it.error)
                        logger.d(TAG, "While checking if number is  registered: " + it.error)
                    }
                }
            })

        viewModel.checkMobileNo
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        showToast("Otp sent")

                        if (isNumberRegistered) {
                            //show user already registered dialog
                            showToast("Number  registered")
                            if (viewBinding.mobileNoEt.text.toString().isNotEmpty()) {
                                navigation.navigateTo(
                                    "LeadMgmt/gigerOnboardingOtp", bundleOf(
                                        LeadManagementConstants.INTENT_EXTRA_MODE to LeadManagementConstants.MODE_REGISTERED,
                                        INTENT_EXTRA_MOBILE_NO to "${viewBinding.mobileNoEt.text}",
                                        INTENT_EXTRA_OTP_TOKEN to it.content.verificationToken
                                    )
                                )
                                viewBinding.mobileNoEt.setText("")
                            }
                        } else {
                            showMobileAlreadyRegisterdDialog()
                            showToast("Number not registered")

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
        //UserAlreadyExistDialogFragment.launch(childFragmentManager, this)
        viewBinding.notRegisteredLayout.visible()
        viewBinding.createProfileBtn.visible()
        viewBinding.changeNumber.visible()
        viewBinding.enterMobileLabel.setText(getString(R.string.giger_not_registered))
        viewBinding.tvPleaseEnter.setText(getString(R.string.joining_failed))
        viewBinding.makeSureText.setText(getString(R.string.not_signed_up))
        viewBinding.submitButton.setText("Share Referral Link")
        viewBinding.submitButton.tag = REFERRAL_TAG
    }


    fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity?.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        view ?: run {
            view = View(activity)
        }
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showKeyboard() {
        viewBinding.mobileNoEt?.let {
            it.isFocusableInTouchMode = true
            it.requestFocus()
            val inputMethodManager =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                it.applicationWindowToken,
                InputMethodManager.SHOW_FORCED, 0
            )
        }

    }

}