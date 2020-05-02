package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.login_frament.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class Login: BaseFragment() {
    companion object {
        fun newInstance() = Login()
        val PERMISSION_REQ_CODE = 100
        val permissionsRequired = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        )
        var MOBILENO_INPUT_CHANGED = false
    }
    lateinit var viewModel: LoginViewModel
    private val INDIAN_MOBILE_NUMBER =
        Pattern.compile("^[+][0-9]{12}\$")

    lateinit var match: Matcher;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setDarkStatusBarTheme(false);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        return inflateView(com.gigforce.app.R.layout.login_frament, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activity = this.activity!!
        listeners()
        observer()
        requestForDeviceMobileNumber()
    }

    private fun observer() {
        viewModel.liveState.observeForever {
            when(it){
                LoginViewModel.STATE_CODE_SENT -> findNavController().navigate(LoginDirections.actionLogin2ToVerifyOTP(viewModel.verificationId!!))
                else -> {
                    // Doing Nothing
                }
            }
        }
    }

    private fun listeners() {
        otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                doActionOnClick()
            }
            false
        })

        login_button.setOnClickListener{
            login_button.setEnabled(false)

            Handler().postDelayed(Runnable {
                // This method will be executed once the timer is over
                login_button.setEnabled(true)
            }, 3000) // se

            doActionOnClick()
        }

    }

    private fun doActionOnClick(){
        var phoneNumber: String = "+91" + otp_mobile_number.text.toString();
        if(validatePhoneNumber(phoneNumber)) {
            viewModel.sendVerificationCode(phoneNumber)
        }
    }
    private fun validatePhoneNumber(phoneNumber:String): Boolean {
        match = INDIAN_MOBILE_NUMBER.matcher(phoneNumber)
        if (phoneNumber.isEmpty()) {
            showToast("Please enter valid phone number")
            return false
        }
        if(!match.matches()) {
            showToast("Please enter valid phone number")
            return false
        }
        return true
    }
    private fun requestForDeviceMobileNumber() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.READ_PHONE_NUMBERS) ==
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            var mSubscriptionManager: SubscriptionManager = SubscriptionManager.from(context);
            val subInfoList: List<SubscriptionInfo> = mSubscriptionManager.activeSubscriptionInfoList
            var deviceMobileNos  = ArrayList<String>()

            for (subscriptionInfo in subInfoList) {
                if(subscriptionInfo.number!=null) {
                    var numbStr = subscriptionInfo.number
                    if(subscriptionInfo.number.contains("+91"))
                        numbStr = subscriptionInfo.number.substringAfter("+91")
                    deviceMobileNos.add(numbStr)
                }
            }
            otp_mobile_number.threshold = 0
            otp_mobile_number.setAdapter(ArrayAdapter(activity!!, com.gigforce.app.R.layout.support_simple_spinner_dropdown_item, deviceMobileNos))

        }
        else{
            checkForAllPermissions()
        }
    }

    private fun checkForAllPermissions() {
        requestPermissions(permissionsRequired, PERMISSION_REQ_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_CODE && isAllPermissionGranted(grantResults)) {
            requestForDeviceMobileNumber()
            showToast("Permissions Granted")
        } else {
            checkForAllPermissions()
        }
    }
    fun isAllPermissionGranted(grantResults: IntArray):Boolean{
        for(result in grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

}