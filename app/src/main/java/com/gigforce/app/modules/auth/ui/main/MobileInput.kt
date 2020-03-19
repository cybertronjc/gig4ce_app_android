package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.mobile_number_input.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MobileInput: Fragment() {
    companion object {
        fun newInstance() = MobileInput()
    }

    lateinit var layout: View
    lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        layout = inflater.inflate(R.layout.mobile_number_input, container, false)
        return layout
    }

    private val INDIAN_MOBILE_NUMBER =
        Pattern.compile("^[+][0-9]{12}\$")

    lateinit var match: Matcher;

    private fun validatePhoneNumber(phoneNumber:String): Boolean {
        match = INDIAN_MOBILE_NUMBER.matcher(phoneNumber)
        if (phoneNumber.isEmpty()) {
            //PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)
            // fieldPhoneNumber.error = "Invalid phone number."
            Toast.makeText(this.context, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
            Log.d("LoginDebug>>>1>", phoneNumber)
            return false
        }
        if(!match.matches()) {
            Toast.makeText(this.context, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
            Log.d("LoginDebug>>2>>", phoneNumber)
            return false
        }
        return true
    }

    private fun doActionOnClick(){
        var phoneNumber: String = "+91" + layout.otp_mobile_number.text.toString();
        Log.d("LoginDebug", phoneNumber)
        validatePhoneNumber(phoneNumber)
        viewModel.phoneNo = phoneNumber;
        viewModel.sendVerificationCode(phoneNumber)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.activity = this.activity!!

        layout.otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                //Perform Code
                doActionOnClick()
            }
            false
        })

        layout.login_button.setOnClickListener{
            doActionOnClick()
        }

//        layout.otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
//                //Perform Code
//                var phoneNumber: String = "+91" + layout.otp_mobile_number.text.toString();
//                Log.d("LoginDebug", phoneNumber)
//                validatePhoneNumber(phoneNumber)
//                viewModel.phoneNo = phoneNumber;
//                viewModel.sendVerificationCode(phoneNumber)
//            }
//        }
//        )

        viewModel.liveState.observeForever {
            when(it){
                LoginViewModel.STATE_CODE_SENT -> findNavController().navigate(MobileInputDirections.actionMobileInputToVerifyOTP(viewModel.verificationId!!))
                LoginViewModel.STATE_SIGNIN_SUCCESS -> findNavController().popBackStack()
                else -> {

                }
            }
        }
    }
}