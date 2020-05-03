package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_SUCCESS
import kotlinx.android.synthetic.main.otp_verification.*
import kotlinx.android.synthetic.main.otp_verification.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class VerifyOTP: BaseFragment() {

    companion object {
        fun newInstance() = VerifyOTP()
    }

    private var verificationId: String = ""
    var layout: View? = null;
    lateinit var viewModel: LoginViewModel
    var otpresentcounter=0;
    private val OTP_NUMBER =
        Pattern.compile("[0-9]{6}\$")
    lateinit var match: Matcher;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
            counterStart();
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.verificationId = verificationId.toString()
        layout = inflateView(R.layout.otp_verification, inflater, container)
        //TODO
//        layout?.textView29?.text = "We have sent the OTP to your " +". Please enter the OTP";
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        listeners()
        observer()
//        if(otpresentcounter>=2){
//            layout.otptimertv.text = "try later!"
//            Toast.makeText(layout.context, "Too many invalid attempts, Try again later!", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun initializeViews() {
        var str = resources.getString(R.string.otp_reenter_mobile)
        val spannableString1 = SpannableString(str)
        spannableString1.setSpan(UnderlineSpan(),0,str.length,0)
        reenter_mobile.text = spannableString1
        textView29?.text = "We have sent the OTP to your " +". Please enter the OTP";
    }

    private fun observer() {
        viewModel.liveState.observe(viewLifecycleOwner, Observer { it ->
            if(it.stateResponse == STATE_SIGNIN_FAILED){
                showWrongOTPLayout(true)
            }else if (it.stateResponse == STATE_SIGNIN_SUCCESS) {
                navigate(R.id.onOTPSuccess)
            }
        })

    }

    private fun showWrongOTPLayout(show: Boolean) {
        if(show){
            cvotpwrong.visibility = View.VISIBLE
            textView26.visibility = View.INVISIBLE
        }
        else{
            cvotpwrong.visibility = View.INVISIBLE
            textView26.visibility = View.VISIBLE
        }
    }

    private fun listeners() {

        layout?.cvotpwrong?.visibility = View.INVISIBLE;
        txt_otp.setOnClickListener{
            cvotpwrong.visibility = View.INVISIBLE
            textView26.visibility = View.VISIBLE
        }
        verify_otp_button?.setOnClickListener {
            val otpIn = layout?.txt_otp?.text
            match = OTP_NUMBER.matcher(otpIn)
            if(match.matches()){
                viewModel.verifyPhoneNumberWithCode(otpIn.toString())
            }
            else {
                showWrongOTPLayout(true)
            }
        }
        otptimertv?.setOnClickListener {
            if(layout?.otptimertv?.text == "Resend") {
                otpresentcounter++;
                Toast.makeText(layout?.context!!, "OTP resent", Toast.LENGTH_SHORT).show()
                counterStart();
            }
            else{
                Toast.makeText(layout?.context!!, "Click on Reenter mobile number as it could be wrong!", Toast.LENGTH_SHORT).show()
            }
        }
        reenter_mobile.setOnClickListener {
            findNavController().navigate(R.id.Login)
        }
        txt_otp.doAfterTextChanged { showWrongOTPLayout(false) }
    }

    private fun counterStart(){
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                layout?.otptimertv?.text = (millisUntilFinished / 1000).toString() + " s"
            }
            override fun onFinish() {
                layout?.otptimertv?.text = "Resend"
            }
        }.start()
    }
}