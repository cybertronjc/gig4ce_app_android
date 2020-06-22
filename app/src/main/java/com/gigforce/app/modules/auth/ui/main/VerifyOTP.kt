package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_SUCCESS
import kotlinx.android.synthetic.main.otp_verification.*
import kotlinx.android.synthetic.main.otp_verification.progressBar
import java.util.regex.Matcher
import java.util.regex.Pattern

class VerifyOTP: BaseFragment() {

    companion object {
        fun newInstance() = VerifyOTP()
    }

    private var verificationId: String = ""
    private var mobile_number:String = ""
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
            mobile_number = it.getString("mobile_number")!!
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

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activity = this.requireActivity()
        initializeViews()
        listeners()
        observer()
        saveNewUsedMobileNumber()
//        if(otpresentcounter>=2){
//            layout.otptimertv.text = "try later!"
//            Toast.makeText(layout.context, "Too many invalid attempts, Try again later!", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun saveNewUsedMobileNumber() {
        var oldData = getAllMobileNumber()
        if(oldData==null||oldData.equals("")) {
            saveAllMobileNumber(mobile_number)
        }
        else if(!oldData.contains(mobile_number)){
            oldData += ","+mobile_number
            saveAllMobileNumber(oldData)

        }
    }
    private fun initializeViews() {
        counterStart();
        var str = resources.getString(R.string.otp_reenter_mobile)
        val spannableString1 = SpannableString(str)
        spannableString1.setSpan(UnderlineSpan(),0,str.length,0)
        reenter_mobile.text = spannableString1
        textView29?.text = "One Time Password (OTP) has been sent to your mobile "+mobile_number+". Please enter the same here to login."
    }

    private fun observer() {
        viewModel.liveState.observe(viewLifecycleOwner, Observer { it ->
            if(it.stateResponse == STATE_SIGNIN_FAILED){
                showWrongOTPLayout(true)
            }else if (it.stateResponse == STATE_SIGNIN_SUCCESS) {
//                navigate(R.id.action_verifyOTP_to_onOTPSuccess)
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
                progressBar.visibility = View.VISIBLE
                verify_otp_button.setEnabled(false)
                Handler().postDelayed(Runnable {
                    // This method will be executed once the timer is over
                    if (verify_otp_button != null) {
                        verify_otp_button.setEnabled(true)
                        progressBar.visibility = View.GONE
                    }
                }, 3000)
                viewModel.verifyPhoneNumberWithCode(otpIn.toString())
            }
            else {
                showWrongOTPLayout(true)
            }
        }
        resend_otp?.setOnClickListener {


            if(otpresentcounter<2) {
                    otpresentcounter++;
                    counterStart();
                    viewModel.sendVerificationCode("+91" + mobile_number)
                }else{
                    navigateToLoginScreen()
                }
        }
        reenter_mobile.setOnClickListener {
            if(!timerStarted) {
                navigateToLoginScreen()
            }
        }
        txt_otp.doAfterTextChanged { showWrongOTPLayout(false) }
    }

    private fun navigateToLoginScreen() {
        var bundle = bundleOf("mobileno" to mobile_number)
        popAllBackStates()
        navigate(R.id.Login,bundle)
//        navigateWithAllPopupStack(R.id.Login)
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
                if(reenter_mobile!=null)
                reenter_mobile.visibility = View.INVISIBLE
            }
            override fun onFinish() {
                showResendOTPMessage(true)
                if(reenter_mobile!=null)
                reenter_mobile.visibility = View.VISIBLE
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