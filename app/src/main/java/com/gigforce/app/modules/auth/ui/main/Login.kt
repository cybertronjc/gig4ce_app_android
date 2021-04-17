package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.login_frament.*
import kotlinx.android.synthetic.main.mobile_number_digit_layout.*
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
    private var mobile_number_sb = StringBuilder()
    private var arrayEditTexts1 = ArrayList<EditText>()
    private var win: Window? = null


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
//        if (getIntroCompleted() == null || getIntroCompleted().equals("")) {
//            navigateWithAllPopupStack(R.id.authFlowFragment)
//        } else {
            viewModel.activity = this.requireActivity()
            //otp_mobile_number.setText(mobile_number)
            populateMobileInEditTexts(mobile_number)
            Log.d("mobile_number", mobile_number)
            changeStatusBarColor()
            getAllEarlierMobileNumbers()
            prepareEditTextList()
            listeners()
            observer()
            setClickListnerOnEditTexts()

            //back button
            back_button_login.setOnClickListener {
                activity?.onBackPressed()
            }

            //registerTextWatcher()
//            if (mobile_number.equals(""))
//                showComfortDialog()
//        }
    }


    private fun populateMobileInEditTexts(mobile: String){
        if (mobile.length == 10){
            prepareEditTextList()
            Log.d("mobile sixe", mobile)
            for (i in 0..mobile.length - 1){
                arrayEditTexts1.get(i).setText(mobile.toCharArray().get(i).toString())
                Log.d("array size", ""+arrayEditTexts1.size )
            }
        }

    }

    private fun changeStatusBarColor(){
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.colorStatusBar))
    }


    private fun setClickListnerOnEditTexts(){
        for (i in 0..arrayEditTexts1.size - 1){
            arrayEditTexts1.get(i).setOnClickListener {
                val lastFilledBox = mobile_number_sb.toString().length
                Log.d("lastFilledBox", "" + lastFilledBox)
                arrayEditTexts1.get(lastFilledBox).requestFocus()
            }

//            arrayEditTexts1.get(i).setOnTouchListener(object : View.OnTouchListener {
//                override fun onTouch(v: View?, event: MotionEvent): Boolean {
//                    if (MotionEvent.ACTION_UP === event.action) {
//                        if (mobile_number_sb.length == 0){
//                            mobile_digit_1.requestFocus()
//                        }
////                        arrayEditTexts1.get(i).clearFocus()
////                        val lastFilledBox = mobile_number_sb.toString().length
////                        Log.d("lastFilledBox", "" + lastFilledBox)
////                        arrayEditTexts1.get(lastFilledBox).requestFocus()
//                    }
//                    return false // return is important...
//                }
//            })

//            arrayEditTexts1.get(i).setOnFocusChangeListener { view, b ->
//                if (b){
//                    if (mobile_number_sb.length == 0){
//                        arrayEditTexts1.get(0).requestFocus()
//                    }
//                   // arrayEditTexts1.get(i).background = resources.getDrawable(R.drawable.mobile_number_digit_focus_bg)
//                }
//                else{
//                   // arrayEditTexts1.get(i).background = resources.getDrawable(R.drawable.mobile_number_digit_background)
//                }
//            }


        }
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
        var view = activity?.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        view ?: run {
            view = View(activity)
        }
        view?.let {
            imm?.hideSoftInputFromWindow(it.getWindowToken(), 0)
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
                LoginViewModel.STATE_VERIFY_SUCCESS ->

                    navigateToOTPVarificationScreen()
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
        otp_mobile_number.doAfterTextChanged {
            showWrongMobileNoLayout(false)
            if (otp_mobile_number.text.toString().length == 10) {
                hideKeyboard()
            }
        }

        for (i in 0..arrayEditTexts1.size - 1){
            arrayEditTexts1.get(i).setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && i != 0 && arrayEditTexts1.get(
                        i
                    ).text.isEmpty()
                ) {

                    arrayEditTexts1.get(i - 1)!!.text = null
                    arrayEditTexts1.get(i - 1).requestFocus()
                    //arrayEditTexts1.get(i-1).background = resources.getDrawable(R.drawable.mobile_number_digit_focus_bg)
                    if (mobile_number_sb.length > 0){
                        mobile_number_sb.deleteAt(mobile_number_sb.length - 1)
                    }

                    Log.d("stringbuilder", mobile_number_sb.toString())
                    Log.d("lastboxfilled", "" + (mobile_number_sb.length - 1))
                    true
                }
                false
            })
        }

        for (i in 0..arrayEditTexts1.size - 1){
            arrayEditTexts1.get(i).doAfterTextChanged {
                if (it.toString()?.length == 1 && i != 9){
                    arrayEditTexts1.get(i + 1).requestFocus()
                   // arrayEditTexts1.get(i+1).background = resources.getDrawable(R.drawable.mobile_number_digit_focus_bg)
                    mobile_number_sb.append(it.toString())
                }
            }
        }


        otp_mobile_number.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            cvloginwrong.visibility = GONE
//            textView23.visibility = VISIBLE

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
//            textView23.visibility = INVISIBLE
        } else {
            cvloginwrong.visibility = GONE
//            textView23.visibility = VISIBLE

        }
    }

    private fun doActionOnClick() {
        var phoneNumber: String = "+91" + makeMobileNumberString();
        if (!validatePhoneNumber(phoneNumber)) {
            // TODO make the error bar visible
            cvloginwrong.visibility = VISIBLE
//            textView23.visibility = INVISIBLE
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

    private fun makeMobileNumberString(): String{
        val sb = StringBuilder()
        for (i in 0..arrayEditTexts1.size - 1){
            sb.append(arrayEditTexts1.get(i).text.toString())
        }

        return sb.toString()
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
//            makeMobileNumberString().threshold = 1
//            otp_mobile_number.setAdapter(
//                    adapter
//            )
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

     class GenericTextWatcher(private val currentView: View, private val nextView: View?) : TextWatcher {
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.mobile_digit_1 -> if (text.length == 1) {
                    nextView!!.requestFocus()
                    Log.d("here", "expending")

                }
                R.id.mobile_digit_2 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_3 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_4 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_5 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_6 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_7 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_8 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_9 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                R.id.mobile_digit_10 -> if (text.length == 1) {
                    nextView!!.requestFocus()

                }
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

    }

    class GenericKeyEvent(private val currentView: EditText, private val previousView: EditText?) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.mobile_digit_1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }
}