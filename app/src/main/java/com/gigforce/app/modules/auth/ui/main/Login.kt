package com.gigforce.app.modules.auth.ui.main

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.analytics.AuthEvents
import com.gigforce.core.navigation.INavigation
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_frament.*
import kotlinx.android.synthetic.main.mobile_number_digit_layout.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.jvm.Throws

@AndroidEntryPoint
class Login : Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    @Inject
    lateinit var eventTracker: IEventTracker
    @Inject lateinit var navigation : INavigation
    private val viewModel: LoginViewModel by viewModels()
    private val INDIAN_MOBILE_NUMBER =
        Pattern.compile("^[+][9][1][6-9][0-9]{9}\$")

    //        private val INDIAN_MOBILE_NUMBER =
//        Pattern.compile("^[+][0-9]{12}\$")
    lateinit var match: Matcher
    private var mobile_number: String = ""
    private var arrayEditTexts1 = ArrayList<EditText>()
    private var win: Window? = null
    private val RC_HINT = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mobile_number = it.getString("mobileno") ?: ""
        }
//        showKeyboard()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //this.setDarkStatusBarTheme(false);

        return inflater.inflate(R.layout.login_frament, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventTracker.pushEvent(TrackingEventArgs(AuthEvents.LOGIN_OR_SIGNUP_LOADED, null))
        viewModel.activity = this.requireActivity()
        invisible_edit_mobile.setText(mobile_number)
        populateMobileInEditTexts(mobile_number)
        Log.d("mobile_number", mobile_number)
        changeStatusBarColor()
//        getAllEarlierMobileNumbers()
        prepareEditTextList()
        listeners()
        observer()
        requestHint()
        //back button
        back_button_login.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }
//            showKeyboard()

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

    @Throws(IntentSender.SendIntentException::class)
    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val intent: PendingIntent? =
            context?.let { Credentials.getClient(it).getHintPickerIntent(hintRequest) }
        startIntentSenderForResult(intent?.intentSender, RC_HINT, null, 0, 0, 0, null)
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
            navigation.popBackStack("login")
            navigation.navigateTo("authFlowFragment")
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
        if (navigation.getNavController().currentDestination?.id == R.id.Login) {
            try {
                findNavController().navigate(
                    LoginDirections.actionLogin2ToVerifyOTP(
                        viewModel.verificationId!!,
                        invisible_edit_mobile.text.toString()
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

        invisible_edit_mobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                // going forward
                if (before == 0 && count == 1) {
                    fillMobileDigitBoxes(
                        text.toString().length,
                        text.toString()[start].toString(),
                        true
                    )
                }
                // going backward
                else if (before == 1 && count == 0) {
                    fillMobileDigitBoxes(text.toString().length, "", false)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                showWrongMobileNoLayout(false)
                if (invisible_edit_mobile.text.toString().length == 10) {
                    hideKeyboard()
                    login_button.isEnabled = true
                    login_button.background = resources.getDrawable(R.drawable.gradient_button)
                } else {
                    login_button.isEnabled = false
                    login_button.background =
                        resources.getDrawable(R.drawable.app_gradient_button_disabled)
                }
            }

        })


        invisible_edit_mobile.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            cvloginwrong.visibility = GONE
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                login_button.isEnabled = false
                login_button.background =
                    resources.getDrawable(R.drawable.app_gradient_button_disabled)
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

    private fun fillMobileDigitBoxes(ind: Int, md: String, add: Boolean) {

        if (!add && ind < 11) {
            Log.d("remove", md + "ind " + ind + " remove")
            arrayEditTexts1.get(ind).setText("")
        } else if (add && ind > 0) {
            Log.d("add", md + "ind " + ind + " add")
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
            login_button.isEnabled = true
            login_button.background = resources.getDrawable(R.drawable.gradient_button)
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

//    private fun getAllEarlierMobileNumbers() {
//        var deviceMobileNos = ArrayList<String>()
//        var oldMobileNumbers = getAllMobileNumber()
//        if (!oldMobileNumbers.equals("")) {
//            var oldDeviceMobileNosList = oldMobileNumbers?.split(",")
//            for (i in 0 until (oldDeviceMobileNosList?.size!!)) {
//                deviceMobileNos.add(oldDeviceMobileNosList.get(i))
//            }
//            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
//                requireActivity(),
//                android.R.layout.select_dialog_item,
//                deviceMobileNos
//            )
////            makeMobileNumberString().threshold = 1
////            invisible_edit_mobile.setAdapter(
////                    adapter
////            )
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_HINT && resultCode == Activity.RESULT_OK) {

            try {/*You will receive user selected phone number here if selected and send it to the server for request the otp*/
                var credential: Credential = data!!.getParcelableExtra(Credential.EXTRA_KEY)
                if (credential.id != null) {

                    invisible_edit_mobile.setText(credential.id.substring(3))
                    populateMobileInEditTexts(credential.id.substring(3))
                    Log.d("phone detected", credential.id.substring(3))
                } else {
                    Log.d("phone detected", "Couldn't detect Phone number")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}