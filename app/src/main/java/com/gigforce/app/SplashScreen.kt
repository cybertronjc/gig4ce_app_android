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


    }

    override fun onResume() {
        super.onResume()
        handleDynamicLink()
    }

    fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val inviteID = deepLink?.getQueryParameter("invite")
                    val isAmbassador = deepLink?.getQueryParameter("is_ambassador")
                    val roleID = deepLink?.getQueryParameter("role_id")
                    val jobProfileID = deepLink?.getQueryParameter("job_profile_id")
                    val sp = SharedDataImp(this)
                    sp.saveData(
                        StringConstants.INVITE_USER_ID.value,
                        inviteID
                    )
                    if (!jobProfileID.isNullOrEmpty()) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(StringConstants.NAV_TO_CLIENT_ACT.value, true)
                        intent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        intent.putExtra(
                            StringConstants.JOB_PROFILE_ID.value,
                            jobProfileID
                        )
                        initApp(intent)
                        return@addOnSuccessListener
                    } else if (!roleID.isNullOrEmpty()) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(StringConstants.NAV_TO_ROLE.value, true)
                        intent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        intent.putExtra(StringConstants.ROLE_ID.value, roleID)
                        initApp(intent)
                        return@addOnSuccessListener
                    }else if(!isAmbassador.isNullOrEmpty()){
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(StringConstants.INVITE_BY_AMBASSADOR.value, true)
                        intent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        initApp(intent)
                        return@addOnSuccessListener
                    }
                }

                initApp(Intent(this, MainActivity::class.java))


            }
            .addOnFailureListener(this)
            { e ->
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