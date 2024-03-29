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
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

import com.gigforce.core.navigation.INavigation
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.navigation.LeadManagementConstants
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
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
        const val INTENT_CAME_FROM_JOINING = "came_from_joing"
        private var isNumberRegistered = false
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: GigerOnboardingViewModel by viewModels()

    //Data
    private var cameFromJoinings: Boolean = false
    private val firebaseAuthStateListener : FirebaseAuthStateListener = FirebaseAuthStateListener.getInstance()

    override fun viewCreated(
        viewBinding: GigerOnboardingFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getArgsFrom(
            arguments,
            savedInstanceState
        )
        initToolbar(viewBinding)
        initListeners()
        initViewModel()
    }

    private fun getArgsFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            cameFromJoinings = it.getBoolean(INTENT_CAME_FROM_JOINING)
        }

        savedInstanceState?.let {
            cameFromJoinings = it.getBoolean(INTENT_CAME_FROM_JOINING)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            INTENT_CAME_FROM_JOINING,
            cameFromJoinings
        )
    }

    val REFERRAL_TAG: String = "referral"
    private fun initListeners() {
        viewBinding.submitButton.setOnClickListener {
            if (viewBinding.submitButton.tag?.toString().equals(REFERRAL_TAG)) {

                val mobileNo = viewBinding.mobileNoEt.text.toString()
                if (!INDIAN_MOBILE_NUMBER.matcher(mobileNo).matches()) {
                    showAlertDialog("", getString(R.string.enter_valid_mobile_lead))
                    return@setOnClickListener
                }

                //navigate to referral
                navigation.navigateTo(
                    dest = LeadManagementNavDestinations.FRAGMENT_PICK_PROFILE_FOR_REFERRAL,
                    args = bundleOf(
                        LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER to "+91$mobileNo"
                    ),
                    navOptions = getNavOptions()
                )

            } else {
                validateDataAndsubmit()
            }

        }

        viewBinding.changeNumber.setOnClickListener {
            viewBinding.mobileNoEt.isEnabled = true
            viewBinding.mobileNoEt.setText("")
            viewBinding.mobileNoEt.requestFocus()
            viewBinding.createProfileBtn.gone()
            viewBinding.submitButton.tag = "next"
            viewBinding.submitButton.setText(getString(R.string.next_lead))
            viewBinding.notRegisteredLayout.gone()
            viewBinding.tvPleaseEnter.setText(resources.getString(R.string.please_enter_lead))
            viewBinding.makeSureText.setText(resources.getString(R.string.registered_lead))
            viewBinding.enterMobileLabel.setText(resources.getString(R.string.gigers_phone_lead))
            viewBinding.changeNumber.gone()
            viewBinding.changeNumberIv.gone()

            showKeyboard()
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
            viewModel.sendOtp(
                viewBinding.mobileNoEt.text.toString()
            )
        }

    }

    private fun initToolbar(
        viewBinding: GigerOnboardingFragmentBinding
    ) = viewBinding.toolbarOnboarding.apply {
        this.hideActionMenu()
        this.showTitle(context.getString(R.string.mobile_number_lead))
        this.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun validateDataAndsubmit() {

        if (viewBinding.mobileNoEt.text.length != 10) {
            showAlertDialog("", getString(R.string.enter_valid_mobile_lead))
            return
        }
        val mobileNo = viewBinding.mobileNoEt.text.toString()
        if (!INDIAN_MOBILE_NUMBER.matcher(mobileNo).matches()) {
            showAlertDialog("", getString(R.string.enter_valid_mobile_lead))
            return
        }

        if("+91${viewBinding.mobileNoEt.text}" == firebaseAuthStateListener.getCurrentSignInInfo()?.phoneNumber){
            showAlertDialog("Wrong mobile number","You cannot user your own mobile number")
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
            .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
            .show()
    }

    private fun initViewModel() {

        viewModel.numberRegistered
            .observe(viewLifecycleOwner, {
                val status  = it?: return@observe

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }

                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        if (it.content.status) {

                            val isUserRegistered = it.content.uId != null
                            if (!isUserRegistered) {
                                isNumberRegistered = false
                                logger.d(
                                    TAG,
                                    "When User is not registered " + isNumberRegistered.toString()
                                )
                                showMobileNotRegisterdDialog()
                            } else {
                                isNumberRegistered = true
                                logger.d(
                                    TAG,
                                    "When User is registered " + isNumberRegistered.toString()
                                )
//                                viewModel.sendOtp(
//                                    viewBinding.mobileNoEt.text.toString()
//                                )
                                if(cameFromJoinings) {

                                    navigation.navigateTo(
                                        LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_TO_ACTIVATE,
                                        bundleOf(
                                            LeadManagementConstants.INTENT_EXTRA_USER_ID to it.content.uId,
                                        )
                                    )
                                } else{
                                    PhoneNumberAlreadyRegisteredDialogFragment.launch(childFragmentManager)
                                }
                            }
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.unable_to_check_user_lead))
                                .setMessage(getString(R.string.unable_to_check_already_registered_lead))
                                .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                                .show()
                            logger.d(
                                TAG,
                                "While checking if number is  registered API status failure"
                            )
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
                it ?: return@Observer

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        showToast(getString(R.string.otp_sent_lead))

                        if (viewBinding.mobileNoEt.text.toString().isNotEmpty()) {
                            navigation.navigateTo(
                                LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING_OTP, bundleOf(
                                    LeadManagementConstants.INTENT_EXTRA_MODE to LeadManagementConstants.MODE_REGISTERED,
                                    INTENT_EXTRA_MOBILE_NO to viewBinding.mobileNoEt.text.toString(),
                                    INTENT_EXTRA_OTP_TOKEN to it.content.verificationToken,
                                    INTENT_CAME_FROM_JOINING to cameFromJoinings
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

    private fun showMobileNotRegisterdDialog() {
        //UserAlreadyExistDialogFragment.launch(childFragmentManager, this)
        viewBinding.notRegisteredLayout.visible()
        viewBinding.createProfileBtn.visible()
        viewBinding.changeNumber.visible()
        viewBinding.changeNumberIv.visible()
        viewBinding.mobileNoEt.isEnabled = false
        viewBinding.enterMobileLabel.setText(getString(R.string.giger_not_registered_lead))
        viewBinding.tvPleaseEnter.setText(getString(R.string.joining_failed_lead_lead))
        viewBinding.makeSureText.setText(getString(R.string.not_signed_up_lead))
        viewBinding.submitButton.setText(getString(R.string.share_referral_link_lead))
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