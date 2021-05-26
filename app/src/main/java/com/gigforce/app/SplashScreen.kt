package com.gigforce.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.core.base.shareddata.SharedDataImp
import com.gigforce.app.utils.StringConstants
import com.gigforce.common_image_picker.image_capture_camerax.CameraActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import io.branch.referral.Branch
import io.branch.referral.Branch.BranchReferralInitListener
import io.branch.referral.BranchError
import org.json.JSONObject


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
                    val ambassadorLatitude = deepLink?.getQueryParameter("latitude")
                    val ambassadorLongitude = deepLink?.getQueryParameter("longitude")
                    val sp = SharedDataImp(this)

                    Log.d("GigforceSplash","Invite Id = $inviteID")
                    Log.d("GigforceSplash","Role Id = $roleID")

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
                        sp.saveData(
                            StringConstants.INVITE_BY_AMBASSADOR.value,
                            "true"
                        )
                        sp.saveData(
                            StringConstants.AMBASSADOR_LATITUDE.value,
                            ambassadorLatitude?:"0.0"
                        )
                        sp.saveData(
                            StringConstants.AMBASSADOR_LONGITUDE.value,
                            ambassadorLongitude?:"0.0"
                        )
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(StringConstants.INVITE_BY_AMBASSADOR.value, true)
                        intent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        intent.putExtra(StringConstants.AMBASSADOR_LATITUDE.value,ambassadorLatitude?.toDouble())
                        intent.putExtra(StringConstants.AMBASSADOR_LONGITUDE.value,ambassadorLongitude?.toDouble())
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

    override fun onStart() {
        super.onStart()
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener)
            .withData(if (intent != null) intent.data else null).init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
    }

    private val branchReferralInitListener: BranchReferralInitListener =
        object : BranchReferralInitListener {
            override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
                // do stuff with deep link data (nav to page, display content, etc)
            }

        }

}