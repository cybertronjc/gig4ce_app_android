package com.gigforce.app.modules.client_activation

import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.auth.ui.main.LoginViewModel
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.*
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.resend_otp
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.timer_tv
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.txt_otp
import kotlinx.android.synthetic.main.otp_verification.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ScheduleDrivingTestFragment : BaseFragment() {
    private lateinit var mWordOrderID: String
    val viewModel: ScheduleDrivingTestViewModel by viewModels()
    val loginViewModel: LoginViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.layout_fragment_schedule_driving_test, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        loginViewModel.activity = requireActivity()

        initViews()
        initObservers()
        pb_schedule_test.visible()
    }

    private fun initObservers() {
        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (!it.drivingCert?.partnerSchoolDetails?.contact.isNullOrEmpty()) {
                val number = "+91" + it.drivingCert?.partnerSchoolDetails?.contact!![0].number
                tv_number_otp.text = getString(R.string.we_have_send_otp) + " " + number
                loginViewModel.sendVerificationCode(number)
                resend_otp.setOnClickListener {
                    pb_schedule_test.visible()
                    loginViewModel.sendVerificationCode(number)
                }

            }

        })
        loginViewModel.liveState.observeForever {
            when (it.stateResponse) {
                LoginViewModel.STATE_CODE_SENT -> {
                    pb_schedule_test.gone()
                    counterStart()
                }
                LoginViewModel.STATE_VERIFY_FAILED -> showToast(it.msg)
                LoginViewModel.STATE_VERIFY_SUCCESS -> {
                    showToast(it.msg)
                }
            }
        }

        viewModel.getApplication(mWordOrderID)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }

    private val OTP_NUMBER =
            Pattern.compile("[0-9]{6}\$")
    lateinit var match: Matcher;

    private fun initViews() {

        resend_otp.paintFlags = resend_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
        otpnotcorrect_schedule_test.text = Html.fromHtml("If you didn’t receive the OTP, <font color=\'#d72467\'>RESEND</font>")
        verify_otp_button_schedule?.setOnClickListener {
            val otpIn = txt_otp?.text
            match = OTP_NUMBER.matcher(otpIn)
            if (match.matches()) {
                pb_schedule_test.visibility = View.VISIBLE
                verify_otp_button_schedule.setEnabled(false)
                Handler().postDelayed(Runnable {
                    // This method will be executed once the timer is over
                    if (verify_otp_button_schedule != null) {
                        verify_otp_button_schedule.setEnabled(true)
                        pb_schedule_test.visibility = View.GONE
                    }
                }, 3000)
                loginViewModel.verifyPhoneNumberWithCodeScheduleDrivingTest(otpIn.toString())
            } else {
                showToast("Wrong OTP")
            }
        }
    }

    private fun counterStart() {
        showResendOTPMessage(false)

        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var time = (millisUntilFinished / 1000)
                var timeStr: String = "00:"
                if (time.toString().length < 2) {
                    timeStr = timeStr + "0" + time
                } else {
                    timeStr = timeStr + time
                }
                timer_tv?.text = timeStr

            }

            override fun onFinish() {
                showResendOTPMessage(true)
                if (reenter_mobile != null)
                    reenter_mobile.visibility = View.VISIBLE
            }
        }.start()
    }

    fun showResendOTPMessage(isShow: Boolean) {
        if (isShow) {
            tv_number_otp.visibility = View.VISIBLE
            otpnotcorrect_schedule_test.visibility = View.VISIBLE
            resend_otp.visibility = View.VISIBLE
            setTextViewColor(timer_tv, R.color.time_up_color)
            timerStarted = false
        } else {
            tv_number_otp.visibility = View.GONE
            otpnotcorrect_schedule_test.visibility = View.GONE
            resend_otp.visibility = View.GONE
            setTextViewColor(timer_tv, R.color.timer_color)
            timerStarted = true
        }
    }

    var timerStarted = false


}