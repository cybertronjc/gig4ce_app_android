package com.gigforce.app

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
import com.jaeger.library.StatusBarUtil


class MainActivity : AppCompatActivity() {

    val TAG:String = "activity/main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        val navOptionsPopToHome: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()

        // Select Language by Default
        this.findNavController(R.id.nav_fragment).navigate(R.id.languageSelectFragment, null, navOptionsPopToHome);

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
