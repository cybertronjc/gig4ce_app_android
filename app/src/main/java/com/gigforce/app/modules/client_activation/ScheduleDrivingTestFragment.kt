package com.gigforce.app.modules.client_activation

import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.*
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.timer_tv
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.txt_otp
import java.util.regex.Matcher
import java.util.regex.Pattern

class ScheduleDrivingTestFragment : BaseFragment(),
        DrivingCertSuccessDialog.DrivingCertSuccessDialogCallbacks,
        AdapterScheduleTestCb.AdapterScheduleTestCbCallbacks, RejectionDialog.RejectionDialogCallbacks {
    private var enableOtpEditText: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private lateinit var mJobProfileId: String
    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var mNumber: String
    private var mNumbers:ArrayList<String> = ArrayList<String>()
    private val adapter: AdapterScheduleTestCb by lazy {
        AdapterScheduleTestCb()
    }
    val viewModel: ScheduleDrivingTestViewModel by viewModels()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_schedule_driving_test, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
//        loginViewModel.activity = requireActivity()
        setupRecycler()
        initViews()
        initObservers()

//        pb_schedule_test.visible()
    }

    private fun setupRecycler() {
        rv_cb_schedule_test.adapter = adapter
        adapter.setCallbacks(this)
        rv_cb_schedule_test.layoutManager = LinearLayoutManager(requireContext())
        rv_cb_schedule_test.addItemDecoration(ItemOffsetDecoration(resources.getDimensionPixelSize(R.dimen.size_4)))
    }


    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableJPSettings.observe(viewLifecycleOwner, Observer { docReceiving ->
            adapter.addData(docReceiving.checkItems)
            tv_title_toolbar.text = docReceiving.title
            tv_driving_test_certification.text = docReceiving.subtitle
            otp_label.text = docReceiving.otpLabel
            note_msg.text = Html.fromHtml("<b><font color=\'#333333\'>Note</font></b> : "+docReceiving.noteMsg)
            viewModel.getApplication(mJobProfileId, mType, mTitle)


            viewModel.observableApplied.observe(viewLifecycleOwner, Observer {
                pb_schedule_test.gone()
                val otpVerifiedDialog =
                        OTPVerifiedDialog()
                otpVerifiedDialog.isCancelable = false
                otpVerifiedDialog.setCallbacks(this)
                otpVerifiedDialog.arguments = bundleOf(
                        StringConstants.TITLE.value to docReceiving.dialogTitle,
                        StringConstants.ACTION_MAIN.value to docReceiving.dialogActionMain,
                        StringConstants.ACTION_SEC.value to docReceiving.dialogActionSec,
                        StringConstants.SUBTITLE.value to docReceiving.dialogSubtitle,
                        StringConstants.ILLUSTRATION.value to docReceiving.dialogIllustration,
                        StringConstants.CONTENT.value to docReceiving.dialogContent
                )
                otpVerifiedDialog.show(
                        parentFragmentManager,
                        DrivingCertSuccessDialog::class.java.name
                )
            })
        })

        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {
            //            if (it == null) return@Observer
//            if (!it.partnerSchoolDetails?.contact.isNullOrEmpty()) {
//                var contactNumber = ""
//                if (!it.partnerSchoolDetails?.contact!![0].number.contains("+91")) {
//                    contactNumber = "+91"
//                }
//                contactNumber += it.partnerSchoolDetails?.contact!![0].number
//                val number = contactNumber
//                tv_number_otp.text = getString(R.string.we_have_send_otp) + " " + number
//                resend_otp.setOnClickListener {
//                    pb_schedule_test.visible()
//                    sendVerificationCode(number)
//                }
//
//            }

        })
        viewModel.liveState.observe(viewLifecycleOwner, Observer {

            when (it.stateResponse) {


                ScheduleDrivingTestViewModel.CODE_SENT -> {
                    pb_schedule_test.gone()
                    showToast(it.msg)
                    counterStart()
                }
                ScheduleDrivingTestViewModel.VERIFY_FAILED -> {
                    pb_schedule_test.gone()
                    showToast(it.msg)
                }
                ScheduleDrivingTestViewModel.VERIFY_SUCCESS -> {
                    pb_schedule_test.visible()
                    showToast(it.msg)
                    countDownTimer?.cancel()
                    showResendOTPMessage(false)
                }
            }
        })




        viewModel.getUIData(mJobProfileId)
        viewModel.sendOTP
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> {
                            UtilMethods.showLoading(requireContext())
                        }
                        is Lce.Content -> {
                            UtilMethods.hideLoading()
                            generate_otp.gone()
                            showToast("Otp sent")
                            note_msg.gone()
                            verify_otp_button_schedule.visible()
                            otp_screen.visible()
                            counterStart()
                            adapter.disableAllItems()
                            viewModel.otpVerificationToken = it.content.verificationToken.toString()
                        }
                        is Lce.Error -> {
                            UtilMethods.hideLoading()
                            showToast("" + it.error)
                        }
                    }
                })
        viewModel.verifyOTP.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            when (it) {
                Lce.Loading -> {
                    UtilMethods.showLoading(requireContext())
                }
                is Lce.Content -> {
                    countDownTimer?.cancel()

                    viewModel.apply(
                            mJobProfileId,
                            mType,
                            mTitle,
                            adapter.selectedItems,
                            it.content.mobile
                    )
                    pb_schedule_test.visible()

                    UtilMethods.hideLoading()
//                    showToast("Otp sent")
                }
                is Lce.Error -> {
                    UtilMethods.hideLoading()
                    showToast("" + it.error)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
            mNumbers = it.getStringArrayList(StringConstants.MOBILE_NUMBERS.value)?:return@let
        }

        arguments?.let {
            mNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
            mNumbers = it.getStringArrayList(StringConstants.MOBILE_NUMBERS.value)?:return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)
        outState.putString(StringConstants.MOBILE_NUMBER.value, mNumber)
        outState.putStringArrayList(StringConstants.MOBILE_NUMBERS.value,mNumbers)
    }

    private val OTP_NUMBER =
            Pattern.compile("[0-9]{6}\$")
    lateinit var match: Matcher;

    private fun initViews() {

        txt_otp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                verify_otp_button_schedule.isEnabled = enableOtpEditText && (s?.length ?: 0) >= 6
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        generate_otp.setOnClickListener {
            var finalMobileNumber = ""
            if(mNumber.contains("+91"))
                finalMobileNumber = mNumber.takeLast(10)
            else finalMobileNumber = mNumber
            viewModel.sendOTPToMobile(finalMobileNumber,mNumbers)
        }
        otpnotcorrect_schedule_test.setOnClickListener{
            var finalMobileNumber = ""
            if(mNumber.contains("+91"))
                finalMobileNumber = mNumber.takeLast(10)
            else finalMobileNumber = mNumber
            viewModel.sendOTPToMobile(finalMobileNumber,mNumbers)
            counterStart()
        }
//        resend_otp.paintFlags = resend_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
        otpnotcorrect_schedule_test.text =
                Html.fromHtml(getString(R.string.resend_message))


        verify_otp_button_schedule?.setOnClickListener {
            val otpIn = txt_otp?.text.toString()
            verify_otp_button_schedule.isEnabled = false
            viewModel.verifyOTP(otpIn)
        }

        iv_back_application_gig_activation.setOnClickListener { popBackState() }
    }

    private fun counterStart() {
        showResendOTPMessage(false)

        countDownTimer =
                object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        var time = (millisUntilFinished / 1000)
                        var timeStr: String = "00:"
                        if (time.toString().length < 2) {
                            timeStr = timeStr + "0" + time
                        } else {
                            timeStr += time
                        }
                        timer_tv?.text = timeStr

                    }

                    override fun onFinish() {
                        showResendOTPMessage(true)
//                        if (reenter_mobile != null)
//                            reenter_mobile.visibility = View.VISIBLE
                    }
                }.start()
    }

    fun showResendOTPMessage(isShow: Boolean) {
        if (isShow) {
            tv_number_otp?.visibility = View.VISIBLE
            otpnotcorrect_schedule_test?.visibility = View.VISIBLE
//            resend_otp.visibility = View.VISIBLE
            setTextViewColor(timer_tv, R.color.time_up_color)
            timerStarted = false
        } else {
            tv_number_otp?.visibility = View.GONE
            otpnotcorrect_schedule_test?.visibility = View.GONE
//            resend_otp.visibility = View.GONE
            setTextViewColor(timer_tv, R.color.timer_color)
            timerStarted = true
        }
    }

    var timerStarted = false
    override fun onBackPressed(): Boolean {
        countDownTimer?.cancel()
        return super.onBackPressed()

    }

    override fun onClickOkay() {
        findNavController().popBackStack(R.id.fragment_doc_sub, true)
    }

    override fun enableConfirmOtpButton(enable: Boolean) {
        this.enableOtpEditText = enable
        generate_otp.isEnabled = enable
        txt_otp.text = txt_otp.text
    }

    override fun onClickRefer() {
        popFragmentFromStack(R.id.fragment_doc_sub)
        navigate(R.id.referrals_fragment)
    }

    override fun onClickTakMeHome() {
        popFragmentFromStack(R.id.fragment_doc_sub)
    }

//    fun sendVerificationCode(phoneNumber: String) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            phoneNumber, // Phone number to verify
//            60, // Timeout duration
//            TimeUnit.SECONDS, // Unit of timeout
//            requireActivity(), // Activity (for callback binding)
//            viewModel.callbacks // OnVerificationStateChangedCallbacks
//        ) // ForceResendingToken from callbacks
//    }
}