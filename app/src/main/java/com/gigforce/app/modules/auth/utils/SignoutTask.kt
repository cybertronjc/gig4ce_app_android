package com.gigforce.app.modules.auth.utils

import android.app.Activity
import android.content.Intent
import com.gigforce.app.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SignoutTask (){

    companion object{
        fun invoke(activity: Activity) {
            FirebaseAuth.getInstance().signOut()
            val intent: Intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}