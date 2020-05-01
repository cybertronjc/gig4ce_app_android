package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.permission.PermissionUtils
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
                deviceMobileNos.add(subscriptionInfo.number)
            }
            val adapter = ArrayAdapter<String>(
                this.context!!, // Context
                android.R.layout.simple_dropdown_item_1line, // Layout
                deviceMobileNos // Array
            )
            otp_mobile_number.setAdapter(adapter)
        }
        else{
            checkForAllPermissions()
        }
    }

    private fun checkForAllPermissions() {
        PermissionUtils.checkForPermission(activity, PERMISSION_REQ_CODE, *permissionsRequired)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_CODE && PermissionUtils.permissionsGrantedCheck(grantResults)) {
            showToast("Permissions Granted")
        } else {
            checkForAllPermissions()
        }
    }

}