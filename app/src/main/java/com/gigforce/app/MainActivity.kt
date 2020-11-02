package com.gigforce.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.popAllBackStates
import com.gigforce.app.modules.landingscreen.LandingScreenFragment
import com.gigforce.app.modules.landingscreen.LandingScreenFragmentDirections
import com.gigforce.app.modules.onboardingmain.OnboardingMainFragment
import com.gigforce.app.notification.NotificationConstants
import com.gigforce.app.utils.NavFragmentsData
import com.gigforce.app.utils.StringConstants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity(), NavFragmentsData {

    private var bundle: Bundle? = null
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            finish();
            return;
        }
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        navController = this.findNavController(R.id.nav_fragment)
        navController.handleDeepLink(intent)
        when {
            intent.getBooleanExtra(StringConstants.NAV_TO_ROLE.value, false) -> {
//                LandingScreenFragmentDirections.openRoleDetailsHome( intent.getStringExtra(StringConstants.ROLE_ID.value),true)
                navController.popBackStack()
                navController.navigate(
                    R.id.fragment_role_details, bundleOf(
                        StringConstants.ROLE_ID.value to intent.getStringExtra(StringConstants.ROLE_ID.value),
                        StringConstants.INVITE_USER_ID.value to intent.getStringExtra(StringConstants.INVITE_USER_ID.value),
                        StringConstants.ROLE_VIA_DEEPLINK.value to true
                    )
                )
            }
            intent.getStringExtra(IS_DEEPLINK) == "true" -> {
                handleDeepLink()
            }
            else -> {
                proceedWithNormalNavigation()
            }
        }
    }

    private fun handleDeepLink() {

        val clickAction = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_CLICK_ACTION)
        Log.d("MainActivity", "Click action received $clickAction ")

        when (intent.getStringExtra(NotificationConstants.INTENT_EXTRA_CLICK_ACTION)) {
            NotificationConstants.CLICK_ACTIONS.OPEN_GIG_ATTENDANCE_PAGE -> {
                Log.d("MainActivity", "redirecting to attendance page")
                navController.popAllBackStates()
                navController.navigate(
                    R.id.gigAttendancePageFragment,
                    intent.extras
                )
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_VERIFICATION_PAGE -> {
                Log.d("MainActivity", "redirecting to gig verification page")
                navController.popAllBackStates()
                navController.navigate(
                    R.id.gigerVerificationFragment,
                    intent.extras
                )
            }
            else -> {
                navController.popAllBackStates()
                navController.navigate(
                    R.id.landinghomefragment,
                    intent.extras
                )
            }
        }
    }

    private fun proceedWithNormalNavigation() {
        checkForAllAuthentication()
        GetFirebaseInstanceID()
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("MAIN_ACTIVITY_CREATED")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    private fun GetFirebaseInstanceID() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Firebase/InstanceId", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = token //getString(R.string.msg_token_fmt, token)
                Log.v("Firebase/InstanceId", "Firebase Token Received")
                Log.v("Firebase/InstanceId", msg)
                //  Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
    }

    private fun checkForAllAuthentication() {
        navController.popAllBackStates()
        navController.navigate(R.id.authFlowFragment)
//        navController.navigate(R.id.languageSelectFragment)
    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment?

        var fragmentholder: Fragment? =
            navHostFragment!!.childFragmentManager.fragments[navHostFragment!!.childFragmentManager.fragments.size - 1]
        var handled = false
        try {
            handled = (fragmentholder as BaseFragment).onBackPressed()
        } catch (e: Exception) {
        }

        if (!handled) {
            if (isMainScreen(fragmentholder) || isOnBoarding(fragmentholder)) {
                doubleBackPressFun()
            } else super.onBackPressed()
        }

    }

    private fun isOnBoarding(fragmentholder: Fragment?): Boolean {
        try {
            var isOnBoarding = (fragmentholder as OnboardingMainFragment)
            if (isOnBoarding != null) return true
        } catch (e: Exception) {
        }
        return false

    }

    private fun isMainScreen(fragmentholder: Fragment?): Boolean {
        try {
            var isMainHome = (fragmentholder as LandingScreenFragment)
            if (isMainHome != null) return true
        } catch (e: Exception) {
        }
        return false
    }

    private fun doubleBackPressFun() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to close the app", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val IS_DEEPLINK = "is_deeplink"
    }

    override fun setData(bundle: Bundle) {
        this.bundle = bundle;
    }

    override fun getData(): Bundle {
        return bundle ?: Bundle()
    }
}
