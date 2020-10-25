package com.gigforce.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.core.base.shareddata.SharedDataImp
import com.gigforce.app.utils.StringConstants
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class SplashScreen : AppCompatActivity() {

    val TAG: String = "activity/main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Handling if Firebase Dynamic link is being clicked in any other application
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val sp = SharedDataImp(this)
                    deepLink?.getQueryParameter("role_id")?.let {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(StringConstants.NAV_TO_ROLE.value, true)
                        intent.putExtra(StringConstants.ROLE_ID.value, it)
                        initApp(intent)
                        return@addOnSuccessListener

                    }
                    sp.saveData(
                        StringConstants.INVITE_USER_ID.value,
                        deepLink?.getQueryParameter("invite")
                    )

                }

                initApp(Intent(this, MainActivity::class.java))


            }
            .addOnFailureListener(this) { e ->
                run {
                    initApp(Intent(this, MainActivity::class.java))

                }
            }

    }

    fun initApp(intent: Intent) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            startActivity(intent)
            finish();
            return;
        }
        startActivity(intent)
        finish()
    }
}