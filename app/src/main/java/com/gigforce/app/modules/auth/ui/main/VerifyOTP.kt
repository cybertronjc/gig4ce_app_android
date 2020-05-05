package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_SUCCESS
import kotlinx.android.synthetic.main.otp_verification.*
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
    var timerStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
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
        counterStart();
        var str = resources.getString(R.string.otp_reenter_mobile)
        val spannableString1 = SpannableString(str)
        spannableString1.setSpan(UnderlineSpan(),0,str.length,0)
        reenter_mobile.text = spannableString1
        textView29?.text = "We have send the OTP on Number\n" +
                "will apply auto to the fields";
    }

    private fun observer() {
        viewModel.liveState.observe(viewLifecycleOwner, Observer { it ->
            if(it.stateResponse == STATE_SIGNIN_FAILED){
                showWrongOTPLayout(true)
            }else if (it.stateResponse == STATE_SIGNIN_SUCCESS) {
                navigate(R.id.action_verifyOTP_to_onOTPSuccess)
            }
        })

    }



    private fun listeners() {
        cvotpwrong?.visibility = View.INVISIBLE;
        txt_otp.setOnClickListener{
            cvotpwrong.visibility = View.INVISIBLE
            textView26.visibility = View.VISIBLE
        }
        verify_otp_button?.setOnClickListener {
            val otpIn = txt_otp?.text
            match = OTP_NUMBER.matcher(otpIn)
            if(match.matches()){
                viewModel.verifyPhoneNumberWithCode(otpIn.toString())
            }
            else {
                showWrongOTPLayout(true)
            }
        }
        resend_otp?.setOnClickListener {
                otpresentcounter++;
                counterStart();
        }
        reenter_mobile.setOnClickListener {
            navigateToLoginScreen()
        }
        txt_otp.doAfterTextChanged { showWrongOTPLayout(false) }
    }

    private fun navigateToLoginScreen() {
        navigateWithAllPopupStack(R.id.Login)
//        navController.popBackStack(R.id.Login,true)
    }

    private fun counterStart(){
        showResendOTPMessage(false)

        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var time = (millisUntilFinished / 1000)
                var timeStr:String = "00:"
                if(time.toString().length<2){
                    timeStr = timeStr+"0"+time
                }
                else{
                    timeStr = timeStr+time
                }
                timer_tv?.text = timeStr

            }
            override fun onFinish() {
//                layout?.otptimertv?.text = "Resend"
                showResendOTPMessage(true)
            }
        }.start()
    }

    fun showResendOTPMessage(isShow:Boolean){
        if(otpnotcorrect==null)return
        if(isShow) {
            otpnotcorrect.visibility = View.VISIBLE
            resend_otp.visibility = View.VISIBLE
            setTextViewColor(timer_tv,R.color.time_up_color)
            timerStarted = false
        }else{
            otpnotcorrect.visibility = View.INVISIBLE
            resend_otp.visibility = View.INVISIBLE
            setTextViewColor(timer_tv,R.color.timer_color)
            timerStarted = true
        }
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

    override fun onBackPressed(): Boolean {

        if(!timerStarted){
            navigateToLoginScreen()
        }
        return true
    }
}