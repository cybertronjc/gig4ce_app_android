package com.gigforce.lead_management.ui.giger_onboarding

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentGigerOtpVerificationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GigerOtpVerification : BaseFragment2<FragmentGigerOtpVerificationBinding>(
    fragmentName = "GigerOtpVerification",
    layoutId = R.layout.fragment_giger_otp_verification,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerOtpVerification()
        const val INTENT_EXTRA_MOBILE_NO = "mobileNo"
        const val INTENT_EXTRA_OTP_TOKEN = "otp_token"
        const val TAG = "GigerOnboardingOTP"

        const val INTENT_EXTRA_USER_ID = "uid"
        const val INTENT_EXTRA_PHONE_NUMBER = "phone_number"
        const val INTENT_EXTRA_USER_NAME = "user_name"
        const val INTENT_EXTRA_PIN_CODE = "pincode"
        const val INTENT_EXTRA_MODE = "mode"

        const val MODE_EDIT = 1
        const val MODE_ADD = 2
    }

    private val viewModel: GigerOnboardingViewModel by viewModels()
    private lateinit var verificationToken: String
    private lateinit var mobileNo: String
    private var cameFromJoinings : Boolean = false

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
            cameFromJoinings = it.getBoolean(GigerOnboardingFragment.INTENT_CAME_FROM_JOINING)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {
            cameFromJoinings = it.getBoolean(GigerOnboardingFragment.INTENT_CAME_FROM_JOINING)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
        outState.putBoolean(GigerOnboardingFragment.INTENT_CAME_FROM_JOINING, cameFromJoinings)
    }

    private fun initListeners() {
        viewBinding.weWillSendOtpLabel.text = getString(R.string.code_is_sent_to_lead) + mobileNo

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
            viewModel.verifyOtp.observe(viewLifecycleOwner, {

                when (it) {
                    Lce.Loading -> {
                        viewBinding.submitButton.invisible()
                        viewBinding.confirmingOtpPb.visible()
                    }
                    is Lce.Content -> {
                        showToast(getString(R.string.otp_confirmed_lead))

                        navigation.navigateTo(
                            "userinfo/addUserDetailsFragment", bundleOf(
                                INTENT_EXTRA_USER_ID to it.content.uid,
                                INTENT_EXTRA_PHONE_NUMBER to mobileNo,
                                INTENT_EXTRA_MODE to MODE_ADD
                            )
                        )

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
            showAlertDialog("", getString(R.string.enter_valid_otp_lead))
            return
        }

        viewModel.checkOtp(
            token = verificationToken,
            otp = viewBinding.txtOtp.text.toString(),
            mobile = mobileNo,
            cameFromJoining = cameFromJoinings
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
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