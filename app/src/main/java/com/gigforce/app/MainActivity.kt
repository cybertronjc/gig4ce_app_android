package com.gigforce.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.popAllBackStates


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)) {

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
        //navController.navigate(R.id.authFlowFragment)
        navController.navigate(R.id.languageSelectFragment)
    }

//    override fun onBackPressed() {
//        val fragmentList: List<*> = supportFragmentManager.fragments
//
//        var handled = false
//        for (f in fragmentList) {
//            if (f is BaseFragment) {
//                handled = f.onBackPressed()
//                if (handled) {
//                    break
//                }
//            }
//        }
//
//        if (!handled) {
//            super.onBackPressed()
//        }
//    }
}
