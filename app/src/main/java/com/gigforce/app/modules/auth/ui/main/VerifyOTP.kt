package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.otp_verification.view.*

class VerifyOTP: Fragment() {

    companion object {
        fun newInstance() = VerifyOTP()
    }

    private var verificationId: String = ""
    lateinit var layout: View
    lateinit var viewModel: LoginViewModel
    //private lateinit var mTimerTextView: View
    var otpresentcounter=0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
      //      mTimerTextView = TextView(view?.context);
            //object : CountDownTimer(30000, 1000) {
            counterStart();
        }
    }

    private fun counterStart(){
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                layout.otptimertv.text = (millisUntilFinished / 1000).toString() + " s"
            }
            override fun onFinish() {
                layout.otptimertv.text = "Resend"
            }
        }.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        //                                                                  viewModel = this.parentFragment?.activity?.let { ViewModelProviders.of(it).get(LoginViewModel::class.java) }!!
        //viewModel = ViewModelProviders.of(this.activity!!).get(LoginViewModel::class.java)
        viewModel.verificationId = verificationId.toString()
        layout = inflater.inflate(R.layout.otp_verification, container, false)
        //TODO
        layout.textView29.text = "We have sent the OTP to your "+viewModel?.phoneNo?.toString()+"\nPlease enter the OTP";
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.verify_otp_button.setOnClickListener {
            viewModel.verifyPhoneNumberWithCode(layout.otp_string.text.toString())
            if(viewModel.liveState.value?.equals(STATE_SIGNIN_FAILED)!!){
                layout.otpnotcorrect.visibility=View.VISIBLE
                layout.otpnotcorrect.text = "Wrong Password !!";
            }
            //findNavController().navigate(R.id.homeFragment)
        }
        layout.otptimertv.setOnClickListener {
            if(layout.otptimertv.text == "Resend") {
                otpresentcounter++;
                Toast.makeText(layout.context, "OTP resent", Toast.LENGTH_SHORT).show()
                viewModel.phoneNo?.let { it1 -> viewModel.sendVerificationCode(it1) }
                counterStart();
            }
            else{
                Toast.makeText(layout.context, "Click on Reenter mobile number as it could be wrong!", Toast.LENGTH_SHORT).show()
            }
        }

        if(otpresentcounter>=2){
            layout.otptimertv.text = "try later!"
            Toast.makeText(layout.context, "Too many invalid attempts, Try again later!", Toast.LENGTH_SHORT).show()
        }
        
        layout.reenter_mobile.setOnClickListener { findNavController().navigate(R.id.mobileInput)}
    }
}