package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.analytics.AuthEvents
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.google.android.gms.auth.api.credentials.Credential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_frament.*
import kotlinx.android.synthetic.main.mobile_number_digit_layout.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
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

    @Inject
    lateinit var eventTracker: IEventTracker
    lateinit var viewModel: LoginViewModel
    private val INDIAN_MOBILE_NUMBER =
            Pattern.compile("^[+][9][1][6-9][0-9]{9}\$")

    //        private val INDIAN_MOBILE_NUMBER =
//        Pattern.compile("^[+][0-9]{12}\$")
    lateinit var match: Matcher
    private var mobile_number: String = ""
    private var mobile_number_sb = StringBuilder()
    private var arrayEditTexts1 = ArrayList<EditText>()
    private var win: Window? = null

    //    private var mixpanel: MixpanelAPI? = null
    private val CREDENTIAL_PICKER_REQUEST = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mobile_number = it.getString("mobileno") ?: ""
        }
//        showKeyboard()
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
//        if (getIntroCompleted() == null || getIntroCompleted().equals("")) {
//            navigateWithAllPopupStack(R.id.authFlowFragment)
//        } else {

        eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_LOADED, null))
        viewModel.activity = this.requireActivity()
        invisible_edit_mobile.setText(mobile_number)
        populateMobileInEditTexts(mobile_number)
        Log.d("mobile_number", mobile_number)
        changeStatusBarColor()
        getAllEarlierMobileNumbers()
        prepareEditTextList()
        listeners()
        observer()

        //setClickListnerOnEditTexts()

        //back button
        back_button_login.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }
//            showKeyboard()
        //registerTextWatcher()
//            if (mobile_number.equals(""))
//                showComfortDialog()
//        }
    }


    private fun populateMobileInEditTexts(mobile: String) {
        if (mobile.length == 10) {
            prepareEditTextList()
            Log.d("mobile sixe", mobile)
            for (i in 0..mobile.length - 1) {
                arrayEditTexts1.get(i).setText(mobile.toCharArray().get(i).toString())
                Log.d("array size", "" + arrayEditTexts1.size)
            }
//            login_button.isEnabled = true
//            login_button.background = resources.getDrawable(R.drawable.gradient_button)
        }

    }

    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        win?.statusBarColor = resources.getColor(R.color.colorStatusBar)
    }

    private fun prepareEditTextList() {
        arrayEditTexts1 = arrayListOf(
                mobile_digit_1,
                mobile_digit_2,
                mobile_digit_3,
                mobile_digit_4,
                mobile_digit_5,
                mobile_digit_6,
                mobile_digit_7,
                mobile_digit_8,
                mobile_digit_9,
                mobile_digit_10
        )
    }

    fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity?.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        view ?: run {
            view = View(activity)
        }
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showKeyboard() {
        invisible_edit_mobile?.let {
            it.isFocusableInTouchMode = true
            it.requestFocus()
            val inputMethodManager =
                    activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                    it.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED, 0
            )
        }

    }

    private fun showComfortDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.comfort_message_login)
        val okay = dialog?.findViewById(R.id.okay) as TextView
        okay.setOnClickListener {
            dialog.dismiss()
        }
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener {
            removeIntroComplete()
            popFragmentFromStack(R.id.Login)
            navigate(
                    R.id.authFlowFragment
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun observer() {
        viewModel.liveState.observeForever {
            when (it.stateResponse) {
                LoginViewModel.STATE_CODE_SENT -> {
                    navigateToOTPVarificationScreen()
//                    eventTracker.pushEvent(TrackingEventArgs("Navigate to OTP verification screen", null))
                }
                LoginViewModel.STATE_VERIFY_FAILED -> {
                    showToast(it.msg)
                    var map = mapOf("Error" to it.msg)
                    eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_ERROR, map))
                }
                LoginViewModel.STATE_VERIFY_SUCCESS -> {
                    navigateToOTPVarificationScreen()
//                    eventTracker.pushEvent(TrackingEventArgs("Navigate to OTP verification screen", null))
                }
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
                                makeMobileNumberString()
                        )
                )
            } catch (e: Exception) {
            }
        }
    }

    private fun listeners() {
        cvloginwrong.visibility = GONE

        some_id_if_needed.setOnClickListener {
            invisible_edit_mobile.requestFocus()
        }

        invisible_edit_mobile.doAfterTextChanged {
            showWrongMobileNoLayout(false)
            if (invisible_edit_mobile.text.toString().length == 10) {
                hideKeyboard()
                login_button.isEnabled = true
                login_button.background = resources.getDrawable(R.drawable.gradient_button)
            } else {
                login_button.isEnabled = false
                login_button.background = resources.getDrawable(R.drawable.app_gradient_button_disabled)
            }
            if (it.toString().length > 0) {
                //fill the boxes
                fillMobileDigitBoxes(invisible_edit_mobile.text.toString().length, it.toString()[it.toString().length - 1].toString(), true)
            }
            if (it.toString().length < makeMobileNumberString().length) {
                fillMobileDigitBoxes(invisible_edit_mobile.text.toString().length, "", false)
            }
        }


        invisible_edit_mobile.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            cvloginwrong.visibility = GONE
//            textView23.visibility = VISIBLE

//            if( keyCode == KeyEvent.KEYCODE_DEL ) {
//                //this is for backspace
//                    Log.d("backspace", "here")
//                fillMobileDigitBoxes(invisible_edit_mobile.text.toString().length, "", false)
//            }

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                login_button.isEnabled = false
                login_button.background = resources.getDrawable(R.drawable.app_gradient_button_disabled)
                doActionOnClick()
            }
            false
        })

        login_button.setOnClickListener {
            login_button.isEnabled = false
            progressBar.visibility = View.VISIBLE
            Handler().postDelayed(Runnable {
                // This method will be executed once the timer is over
                if (login_button != null) {
                    login_button.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            }, 3000)
            doActionOnClick()
        }

    }

    private fun fillMobileDigitBoxes(ind: Int, md: String, remove: Boolean) {

        if (!remove) {
            Log.d("fill", md + "ind " + ind + " remove")
            arrayEditTexts1.get(ind).setText("")
        } else {
            Log.d("fill", md + "ind " + ind + " add")
            arrayEditTexts1.get(ind - 1).setText(md)
        }

    }


    private fun showWrongMobileNoLayout(show: Boolean) {
        if (show) {
            cvloginwrong.visibility = VISIBLE
//            textView23.visibility = INVISIBLE
        } else {
            cvloginwrong.visibility = GONE
//            textView23.visibility = VISIBLE

        }
    }

    private fun doActionOnClick() {


        var phoneNumber: String = "+91" + invisible_edit_mobile.text.toString()
        if (!validatePhoneNumber(phoneNumber)) {
            // TODO make the error bar visible
            cvloginwrong.visibility = VISIBLE
//            textView23.visibility = INVISIBLE
            login_button.isEnabled = true
            login_button.background = resources.getDrawable(R.drawable.gradient_button)
        } else {

            eventTracker.pushEvent(TrackingEventArgs(
                    eventName = AuthEvents.SIGN_UP_STARTED,
                    props = mapOf(
                            "phone_no" to phoneNumber
                    )
            ))


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

    private fun makeMobileNumberString(): String {
        val sb = StringBuilder()
        for (i in 0..arrayEditTexts1.size - 1) {
            sb.append(arrayEditTexts1.get(i).text.toString())
        }

        return sb.toString()
    }


    private fun getAllEarlierMobileNumbers() {
        var deviceMobileNos = ArrayList<String>()
        var oldMobileNumbers = getAllMobileNumber()
        if (!oldMobileNumbers.equals("")) {
            var oldDeviceMobileNosList = oldMobileNumbers?.split(",")
            for (i in 0..(oldDeviceMobileNosList?.size!!) - 1) {
                deviceMobileNos.add(oldDeviceMobileNosList.get(i))
            }
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    requireActivity(),
                    android.R.layout.select_dialog_item,
                    deviceMobileNos
            )
//            makeMobileNumberString().threshold = 1
//            invisible_edit_mobile.setAdapter(
//                    adapter
//            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREDENTIAL_PICKER_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    // credential.getId();  <-- will need to process phone number string
                }
            // ...
        }
    }

    //    override fun onPause() {
//        super.onPause()
//        hideSoftKeyboard()
//    }
    override fun onResume() {
        super.onResume()
//        showKeyboard()
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
//        invisible_edit_mobile.threshold = 0
//        invisible_edit_mobile.setAdapter(ArrayAdapter(activity!!, com.gigforce.app.R.layout.support_simple_spinner_dropdown_item, deviceMobileNos))
//
//    }
//    else{
//        checkForAllPermissions()
//    }
//}
}