package com.gigforce.app.modules.auth.ui.main

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.analytics.AuthEvents
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_FAILED
import com.gigforce.app.modules.auth.ui.main.LoginViewModel.Companion.STATE_SIGNIN_SUCCESS
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.otp_verification.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class VerifyOTP : BaseFragment() {

    companion object {
        fun newInstance() = VerifyOTP()
    }

    @Inject
    lateinit var eventTracker: IEventTracker
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
    private var client: SmsRetrieverClient? = null

    //    private var otpReceiver: SmsRetrieverBroadcastReceiver.OTPReceiveListener = this
//    private  var smsBroadcast = SmsRetrieverBroadcastReceiver()
    //var appSignature = AppSignatureHelper(context)
    private var win: Window? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
            mobile_number = it.getString("mobile_number")!!
        }

        //Log.d("app signature", appSignature.appSignatures.get(0))


    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        changeStatusBarColor()
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.verificationId = verificationId.toString()
        layout = inflateView(R.layout.otp_verification, inflater, container)
        //TODO
        eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_OTP_SCREEN_LOADED, null))
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
//        startSmsRetriver()
        listeners()
        observer()
        saveNewUsedMobileNumber()
//        showKeyboard()
//        if(otpresentcounter>=2){
//            layout.otptimertv.text = "try later!"
//            Toast.makeText(layout.context, "Too many invalid attempts, Try again later!", Toast.LENGTH_SHORT).show()
//        }
    }

    fun showKeyboard() {
        txt_otp?.let {
            it.setFocusableInTouchMode(true)
            it.requestFocus()
            val inputMethodManager =
                    activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                    it.getApplicationWindowToken(),
                    InputMethodManager.SHOW_FORCED, 0
            )
        }


    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }

    //    private fun setupSmsRetriver() {
//        client = context?.let { SmsRetriever.getClient(it) }
//        var task: Task<Void>? = client?.startSmsRetriever()
//
//        // Listen for success/failure of the start Task. If in a background thread, this
//// can be made blocking using Tasks.await(task, [timeout]);
//        // Listen for success/failure of the start Task. If in a background thread, this
//// can be made blocking using Tasks.await(task, [timeout]);
//       task?.addOnSuccessListener {
//            Log.d("sms retrive", it.toString())
//       }
//
//        task?.addOnFailureListener {
//            Log.d("sms failure", it.toString())
//        }
//    }
    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_gray))
    }


//    private fun startSmsRetriver() {
//        client = activity?.let { SmsRetriever.getClient(it) }
//        val task = client?.startSmsRetriever()
//
//       task?.addOnSuccessListener { //showToast("SMS Retriever Started")
////           smsBroadcast.initOTPListener(this)
//           val intentFilter = IntentFilter()
//           intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
//
//
////           context?.registerReceiver(smsBroadcast, intentFilter)
//
//           //context?.let { it1 -> LocalBroadcastManager.getInstance(it1).registerReceiver(smsBroadcast, intentFilter) }
//
//       }
//
//        task?.addOnFailureListener { showToast("SMS Retriever Failed") }
//    }

//    override fun onOTPReceived(otp: String) {
//        if (smsBroadcast != null) {
//            context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(smsBroadcast) }
//        }
//        showToast(otp)
//        txt_otp.setText(otp)
//        Log.d("OTP Received", otp)
//    }
//
//    override fun onOTPTimeOut() {
//        // do nothing
//        Log.d("Otp", "timeout")
//    }


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

                var map = mapOf("Error" to it.msg)
                eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_ERROR, map))
//                mixpanel?.track("Entered wrong OTP")
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
            hideSoftKeyboard()
        }
        resend_otp?.setOnClickListener {


            if (otpresentcounter < 2) {
                otpresentcounter++;
                counterStart();
                viewModel.sendVerificationCode("+91" + mobile_number)

                eventTracker.pushEvent(TrackingEventArgs(
                        eventName = AuthEvents.SIGN_RESEND_OTP,
                        props = null
                ))
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
//        eventTracker.pushEvent(TrackingEventArgs("Navigate back to Login screen", null))
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

//        if (!timerStarted) {
//        }
        navigateToLoginScreen()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }

//    override fun onPause() {
//        super.onPause()
//        hideSoftKeyboard()
//    }
}