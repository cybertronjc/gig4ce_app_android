package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.validation.Regexes
import com.gigforce.app.utils.setDarkStatusBarTheme
import kotlinx.android.synthetic.main.mobile_number_input.*
import kotlinx.android.synthetic.main.mobile_number_input.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Login: BaseFragment() {
    companion object {
        fun newInstance() = Login()
    }

    lateinit var viewModel: LoginViewModel
    lateinit var match: Matcher;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setDarkStatusBarTheme(false);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        return inflateView(R.layout.mobile_number_input, inflater, container)
    }

    private fun validatePhoneNumber(phoneNumber:String): Boolean {
        match = Regexes.INDIAN_MOBILE_NUMBER.matcher(phoneNumber)
        if (phoneNumber.isEmpty() || !match.matches() || !(android.util.Patterns.PHONE.matcher(phoneNumber).matches())) {
            showToast("Please enter valid phone number")
            Log.d("Login Mobile No: ", phoneNumber)
            return false
        }
        return true
    }

    private fun doActionOnClick(){
        var phoneNumber: String = "+91" + otp_mobile_number.text.toString();
        Log.d("LoginDebug", phoneNumber)
        if(!validatePhoneNumber(phoneNumber)){
            // TODO make the error bar visible
            cvloginwrong.visibility = VISIBLE
            textView23.visibility = INVISIBLE
            login_button.isEnabled = true;
        }
        else{
            viewModel.phoneNo = phoneNumber;
            viewModel.sendVerificationCode(phoneNumber)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.activity = this.activity!!

        otp_mobile_number.setOnClickListener {
            cvloginwrong.visibility = INVISIBLE
            textView23.visibility = VISIBLE
        }

        otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                cvloginwrong.visibility = INVISIBLE
                textView23.visibility = VISIBLE
            if (keyCode == KeyEvent.KEYCODE_ENTER){// && event.action == KeyEvent.ACTION_UP) {
                login_button.isEnabled = false;
                doActionOnClick()
            }
            false
        })

        login_button.setOnClickListener {
            doActionOnClick()
        }

        viewModel.liveState.observeForever {
            when(it){
                LoginViewModel.STATE_CODE_SENT -> findNavController().navigate(LoginDirections.actionLogin2ToVerifyOTP(viewModel.verificationId!!))
                else -> {
                    // Doing Nothing
                }
            }
        }
    }
}