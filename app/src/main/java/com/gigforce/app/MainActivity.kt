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
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jaeger.library.StatusBarUtil


class MainActivity : AppCompatActivity() {

    val TAG:String = "activity/main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

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
        }else if (!loggedIn) {
            this.findNavController(R.id.nav_fragment)
                .navigate(R.id.Login, null, navOptionsPopToHome)
        }

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w: Window = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }*/
    }


}
