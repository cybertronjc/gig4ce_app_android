package com.gigforce.app

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.findNavController
import com.gigforce.app.utils.popAllBackStates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jaeger.library.StatusBarUtil


class MainActivity : AppCompatActivity() {

    val TAG:String = "activity/main"

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        navController = this.findNavController(R.id.nav_fragment)

        this.setInitialNav()
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w: Window = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }*/
        FirebaseAuth.getInstance().addAuthStateListener {
            onAuthStateChanged(it.currentUser)
        }
    }

    private fun onAuthStateChanged(currentUser: FirebaseUser?) {
        if(currentUser == null) {
            // User Logged Out
            navController.popAllBackStates()
            navController.navigate(R.id.Login)
        }else {
            navController.popAllBackStates()
            navController.navigate(R.id.homeScreenIcons)
        }
    }

    fun setInitialNav(){
        val navOptionsPopToHome: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()

        val sp:SharedPreferences = getSharedPreferences("appsettings", 0)
        val lang = sp.getString("app_lang", null)
        val introComplete = sp.getBoolean("intro_complete", false)
        val loggedIn = FirebaseAuth.getInstance().currentUser != null

        // Select Language by Default
        if(lang == null) {
            this.findNavController(R.id.nav_fragment)
                .navigate(R.id.languageSelectFragment, null, navOptionsPopToHome)
        }else if (introComplete) {
            this.findNavController(R.id.nav_fragment)
                .navigate(R.id.introSlidesFragment, null, navOptionsPopToHome)
        }
    }


}
