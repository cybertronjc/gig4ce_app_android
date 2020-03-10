package com.gigforce.app
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.MainActivity

class SplashScreen : AppCompatActivity() {
    val TAG:String = "activity/main"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}