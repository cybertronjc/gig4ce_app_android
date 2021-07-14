package com.gigforce.lead_management.gigeronboarding

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentGigerOtpVerificationBinding
import com.gigforce.lead_management.databinding.GigerOnboardingFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class GigerOtpVerification : BaseFragment2<FragmentGigerOtpVerificationBinding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_giger_otp_verification,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerOtpVerification()
        const val INTENT_EXTRA_MOBILE_NO = "mobileNo"
        const val INTENT_EXTRA_OTP_TOKEN = "otp_token"
        const val TAG = "GigerOnboardingOTP"
    }

    private val viewModel: GigerOnboardingViewModel by viewModels()
    private lateinit var verificationToken: String
    private lateinit var mobileNo: String

    private var userId: String? = null
    private var mode = 0

    @Inject
    lateinit var navigation: INavigation

    override fun viewCreated(viewBinding: FragmentGigerOtpVerificationBinding, savedInstanceState: Bundle?) {
        getDataFromIntents(arguments, savedInstanceState)
        initView()
        initListeners()
        initViewModel()
    }

    private fun initView() {
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mode = it.getInt(LeadManagementConstants.INTENT_EXTRA_MODE)
            userId = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {
            mode = it.getInt(LeadManagementConstants.INTENT_EXTRA_MODE)
            userId = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userId)
    }

    private fun initListeners() {
        viewBinding.weWillSendOtpLabel.text = "Code is sent to +91 - $mobileNo"

        viewBinding.submitButton.setOnClickListener {
            validateDataAndSubmit()
        }

        viewBinding.appBar.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

        viewBinding.txtOtp.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                if (viewBinding.txtOtp.text.toString().length == 6) {
                    hideKeyboard()
                    viewBinding.submitButton.isEnabled = true
                    viewBinding.submitButton.background = resources.getDrawable(R.drawable.gradient_button)
                } else {
                    viewBinding.submitButton.isEnabled = false
                    viewBinding.submitButton.background =
                        resources.getDrawable(R.drawable.app_gradient_button_disabled)
                }
            }

        })
    }

    private fun initViewModel() {
            viewModel.verifyOtp.observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> {
                        viewBinding.submitButton.invisible()
                        viewBinding.confirmingOtpPb.visible()
                    }
                    is Lce.Content -> {
                        showToast("Otp Confirmed")

                        if (mode == LeadManagementConstants.MODE_REGISTERED) {
                            logger.d(TAG, "User is Registered with Gigforce")
                            navigation.navigateTo(
                                "LeadMgmt/selectGigApplicationToActivate", bundleOf(
                                    LeadManagementConstants.INTENT_EXTRA_USER_ID to userId,
                                    LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER to mobileNo,
                                    LeadManagementConstants.INTENT_EXTRA_MODE to mode
                                )
                            )
                        } else {
                            logger.d(TAG, "User was not Registered with Gigforce")
//                            navigation.navigateTo(
//                                "LeadMgmt/selectGigApplicationToActivate", bundleOf(
//                                    LeadManagementConstants.INTENT_EXTRA_USER_ID to it.content.uid,
//                                    LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber,
//                                    LeadManagementConstants.INTENT_EXTRA_MODE to mode
//                                )
//                            )
                        }
                    }
                    is Lce.Error -> {
                        viewBinding.confirmingOtpPb.gone()
                        viewBinding.submitButton.visible()

                        showAlertDialog("", it.error)
                    }
                }
            })
    }


    private fun validateDataAndSubmit() {
        if (viewBinding.txtOtp.text?.length != 6) {
            showAlertDialog("", "Enter a valid otp")
            return
        }

        viewModel.checkOtp(
            userId = userId,
            mode,
            token = verificationToken,
            otp = viewBinding.txtOtp.text.toString(),
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
        viewBinding.txtOtp?.let {
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