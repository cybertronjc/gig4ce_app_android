package com.gigforce.app.modules.auth.ui.main

import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_SUCCESS
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.otp_verification.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class VerifyOTP : BaseFragment(), SmsRetrieverBroadcastReceiver.OTPReceiveListener {

    companion object {
        fun newInstance() = VerifyOTP()
    }


    private var countDownTimer: CountDownTimer? = null
    private var verificationId: String = ""
    private var mobile_number: String = ""
    var layout: View? = null;
    lateinit var viewModel: LoginViewModel
    var otpresentcounter = 0;
    private val OTP_NUMBER =
            Pattern.compile("[0-9]{6}\$")
    lateinit var match: Matcher;
    var timerStarted = false
    private  var client: SmsRetrieverClient? = null
    private var otpReceiver: SmsRetrieverBroadcastReceiver.OTPReceiveListener = this
    private  var smsBroadcast = SmsRetrieverBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
            mobile_number = it.getString("mobile_number")!!
        }

        smsBroadcast.initOTPListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)


        context?.registerReceiver(smsBroadcast, intentFilter)
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
        startSmsRetriver()
        listeners()
        observer()
        saveNewUsedMobileNumber()

//        if(otpresentcounter>=2){
//            layout.otptimertv.text = "try later!"
//            Toast.makeText(layout.context, "Too many invalid attempts, Try again later!", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun startSmsRetriver() {
        client = context?.let { SmsRetriever.getClient(it) }
        val task = client?.startSmsRetriever()

       task?.addOnSuccessListener { showToast("SMS Retriever Started") }

        task?.addOnFailureListener { showToast("SMS Retriever Failed") }
    }

    override fun onOTPReceived(otp: String) {
        if (smsBroadcast != null) {
            context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(smsBroadcast) }
        }
        showToast(otp)
        txt_otp.setText(otp)
        Log.e("OTP Received", otp)
    }

    override fun onOTPTimeOut() {
        // do nothing
    }


    private fun saveNewUsedMobileNumber() {
        var oldData = getAllMobileNumber()
        if (oldData == null || oldData.equals("")) {
            saveAllMobileNumber(mobile_number)
        } else if (!oldData.contains(mobile_number)) {
            oldData += "," + mobile_number
            saveAllMobileNumber(oldData)

        }
    }

    private fun initializeViews() {
        counterStart();
        txt_otp.setUnderLineColor(R.color.otp_underline_color)
        var str = resources.getString(R.string.otp_reenter_mobile)
        val spannableString1 = SpannableString(str)
        spannableString1.setSpan(UnderlineSpan(), 0, str.length, 0)
        reenter_mobile.text = spannableString1
        otp_label?.text =
                getString(R.string.we_have_sent_otp) + " " + mobile_number + ".\n" + getString(R.string.please_enter_it_below)
    }

    private fun observer() {
        viewModel.liveState.observe(viewLifecycleOwner, Observer { it ->
            if (it.stateResponse == STATE_SIGNIN_FAILED) {
                showWrongOTPLayout(true)
            } else if (it.stateResponse == STATE_SIGNIN_SUCCESS) {

                countDownTimer?.cancel()

//                navigate(R.id.action_verifyOTP_to_onOTPSuccess)
            }
        })

    }

    private fun listeners() {
        cvotpwrong?.visibility = View.INVISIBLE;
        txt_otp.setOnClickListener {
            cvotpwrong.visibility = View.INVISIBLE
//            textView26.visibility = View.VISIBLE
        }
        verify_otp_button?.setOnClickListener {
            val otpIn = txt_otp?.text
            match = OTP_NUMBER.matcher(otpIn)
            if (match.matches()) {
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
            } else {
                showWrongOTPLayout(true)
            }
        }
        resend_otp?.setOnClickListener {


            if (otpresentcounter < 2) {
                otpresentcounter++;
                counterStart();
                viewModel.sendVerificationCode("+91" + mobile_number)
            } else {
                navigateToLoginScreen()
            }
        }
        reenter_mobile.setOnClickListener {
//            if (!timerStarted) {
                navigateToLoginScreen()
//            }
        }
        txt_otp.doAfterTextChanged { showWrongOTPLayout(false) }
        iv_back_otp_fragment.setOnClickListener {
            onBackPressed()
        }
    }

    private fun navigateToLoginScreen() {
        countDownTimer?.cancel()
        val bundle = bundleOf(
            "mobileno" to mobile_number

        )
        popAllBackStates()
        navigate(R.id.Login, bundle)
//        navigateWithAllPopupStack(R.id.Login)
    }

    private fun counterStart() {
        showResendOTPMessage(false)

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var time = (millisUntilFinished / 1000)
                var timeStr: String = "00:"
                if (time.toString().length < 2) {
                    timeStr = timeStr + "0" + time
                } else {
                    timeStr = timeStr + time
                }
                timer_tv?.text = timeStr
//                if (reenter_mobile != null)
//                    reenter_mobile.visibility = View.INVISIBLE
            }

            override fun onFinish() {
                showResendOTPMessage(true)
//                if (reenter_mobile != null)
//                    reenter_mobile.visibility = View.VISIBLE
            }
        }.start()
    }

    fun showResendOTPMessage(isShow: Boolean) {

        if (isShow) {
            resend_otp.visibility = View.VISIBLE
            timer_tv.gone()
//            setTextViewColor(timer_tv, R.color.time_up_color)
            timerStarted = false
        } else {
            resend_otp.visibility = View.GONE
            timer_tv.visible()
//            setTextViewColor(timer_tv, R.color.timer_color)
            timerStarted = true
        }
    }

    private fun showWrongOTPLayout(show: Boolean) {
        if (show) {
            cvotpwrong.visibility = View.VISIBLE

        } else {
            cvotpwrong.visibility = View.INVISIBLE

        }
    }

    override fun onBackPressed(): Boolean {

        if (!timerStarted) {
            navigateToLoginScreen()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }
}