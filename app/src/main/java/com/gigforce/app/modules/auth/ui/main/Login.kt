package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.login_frament.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class Login : BaseFragment() {
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
        Pattern.compile("^[+][9][1][6-9][0-9]{9}\$")

    //        private val INDIAN_MOBILE_NUMBER =
//        Pattern.compile("^[+][0-9]{12}\$")
    lateinit var match: Matcher;
    private var mobile_number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mobile_number = it.getString("mobileno") ?: ""
        }
    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //this.setDarkStatusBarTheme(false);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        return inflateView(com.gigforce.app.R.layout.login_frament, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getIntroCompleted() == null || getIntroCompleted().equals("")) {
            navigateWithAllPopupStack(R.id.authFlowFragment)
        } else {
            viewModel.activity = this.requireActivity()
            otp_mobile_number.setText(mobile_number)
            getAllEarlierMobileNumbers()
            listeners()
            observer()
            if (mobile_number.equals(""))
                showComfortDialog()
        }
    }

    private fun showComfortDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.comfort_message_login)
        val okay = dialog?.findViewById(R.id.okay) as TextView
        okay.setOnClickListener {
            dialog?.dismiss()
        }
        val cancel = dialog?.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener() {
            removeIntroComplete()
            popFragmentFromStack(R.id.Login)
            navigate(
                R.id.authFlowFragment
            )
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun observer() {
        viewModel.liveState.observeForever {
            when (it.stateResponse) {
                LoginViewModel.STATE_CODE_SENT -> navigateToOTPVarificationScreen()
                LoginViewModel.STATE_VERIFY_FAILED -> showToast(it.msg)
                LoginViewModel.STATE_VERIFY_SUCCESS -> navigateToOTPVarificationScreen()
            }
        }
    }

    fun navigateToOTPVarificationScreen() {
        // fixed by PD - during a hotfix for apk release - doubleclick issue resolved
        if (getNavigationController().currentDestination?.id == R.id.Login) {
            try {
                findNavController().navigate(
                    LoginDirections.actionLogin2ToVerifyOTP(
                        viewModel.verificationId!!,
                        otp_mobile_number.text.toString()
                    )
                )
            } catch (e: Exception) {
            }
        }
    }

    private fun listeners() {
        cvloginwrong.visibility = INVISIBLE
        otp_mobile_number.doAfterTextChanged { showWrongMobileNoLayout(false) }
        otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            cvloginwrong.visibility = INVISIBLE
            textView23.visibility = VISIBLE
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                login_button.isEnabled = false;
                doActionOnClick()
            }
            false
        })

        login_button.setOnClickListener {
            login_button.setEnabled(false)
            progressBar.visibility = View.VISIBLE
            Handler().postDelayed(Runnable {
                // This method will be executed once the timer is over
                if (login_button != null) {
                    login_button.setEnabled(true)
                    progressBar.visibility = View.GONE
                }
            }, 3000)
            doActionOnClick()
        }

    }


    private fun showWrongMobileNoLayout(show: Boolean) {
        if (show) {
            cvloginwrong.visibility = VISIBLE
            textView23.visibility = INVISIBLE
        } else {
            cvloginwrong.visibility = INVISIBLE
            textView23.visibility = VISIBLE

        }
    }

    private fun doActionOnClick() {
        var phoneNumber: String = "+91" + otp_mobile_number.text.toString();
        if (!validatePhoneNumber(phoneNumber)) {
            // TODO make the error bar visible
            cvloginwrong.visibility = VISIBLE
            textView23.visibility = INVISIBLE
            login_button.isEnabled = true;
        } else {
            viewModel.sendVerificationCode(phoneNumber)
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        match = INDIAN_MOBILE_NUMBER.matcher(phoneNumber)
        if (phoneNumber.isEmpty()) {
            return false
        }
        if (!match.matches()) {
            return false
        }
        return true
    }


    private fun getAllEarlierMobileNumbers() {
        var deviceMobileNos = ArrayList<String>()
        var oldMobileNumbers = getAllMobileNumber()
        if (!oldMobileNumbers.equals("")) {
            var oldDeviceMobileNosList = oldMobileNumbers?.split(",")
            for (i in 0..(oldDeviceMobileNosList?.size!!) - 1!!) {
                deviceMobileNos.add(oldDeviceMobileNosList.get(i))
            }
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.select_dialog_item,
                deviceMobileNos
            )
            otp_mobile_number.threshold = 1
            otp_mobile_number.setAdapter(
                adapter
            )
        }
    }
}

//private fun checkForAllPermissions() {
//    requestPermissions(Login.permissionsRequired, Login.PERMISSION_REQ_CODE)
//}
//override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    if (requestCode == Login.PERMISSION_REQ_CODE && isAllPermissionGranted(grantResults)) {
//        requestForDeviceMobileNumber()
//        showToast("Permissions Granted")
//    } else {
//        checkForAllPermissions()
//    }
//}
//fun isAllPermissionGranted(grantResults: IntArray):Boolean{
//    for(result in grantResults){
//        if(result != PackageManager.PERMISSION_GRANTED){
//            return false
//        }
//    }
//    return true
//}

//private fun requestForDeviceMobileNumber() {
//    if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.READ_PHONE_NUMBERS) ==
//            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!,
//                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//        var mSubscriptionManager: SubscriptionManager = SubscriptionManager.from(context);
//        val subInfoList: List<SubscriptionInfo> = mSubscriptionManager.activeSubscriptionInfoList
//        var deviceMobileNos  = ArrayList<String>()
//
//        for (subscriptionInfo in subInfoList) {
//            if(subscriptionInfo.number!=null) {
//                var numbStr = subscriptionInfo.number
//                if(subscriptionInfo.number.contains("+91"))
//                    numbStr = subscriptionInfo.number.substringAfter("+91")
//                deviceMobileNos.add(numbStr)
//            }
//        }
//        otp_mobile_number.threshold = 0
//        otp_mobile_number.setAdapter(ArrayAdapter(activity!!, com.gigforce.app.R.layout.support_simple_spinner_dropdown_item, deviceMobileNos))
//
//    }
//    else{
//        checkForAllPermissions()
//    }
//}