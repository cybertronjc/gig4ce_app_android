package com.gigforce.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.popAllBackStates
import com.gigforce.app.modules.landingscreen.LandingScreenFragment
import com.gigforce.app.modules.onboardingmain.OnboardingMainFragment
import com.gigforce.app.utils.StringConstants


class MainActivity : AppCompatActivity() {

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
        checkForAllAuthentication()
    }


    private fun checkForAllAuthentication() {
        navController.popAllBackStates()
        navController.navigate(
            R.id.authFlowFragment)
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
}
