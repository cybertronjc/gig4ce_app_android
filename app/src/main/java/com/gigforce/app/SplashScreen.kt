package com.gigforce.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.di.implementations.SharedPreAndCommonUtilDataImp
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import io.branch.referral.Branch
import io.branch.referral.Branch.BranchReferralInitListener
import io.branch.referral.BranchError
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    val TAG: String = "activity/main"

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                val mainIntent = Intent(this, MainActivity::class.java)
                //handling deep links
                intent?.let {
                    val uri = intent.data
                    if (uri != null) {
                        val parameters = uri.pathSegments
                        // after that we are extracting string from that parameters.
                        val param = parameters[parameters.size - 1]
                        if (param == "login_summary"){
                            Log.d("datahere", "login summary")
                            sharedPreAndCommonUtilInterface.saveDataBoolean("deeplink_login", true)
                            mainIntent.putExtra(StringConstants.LOGIN_SUMMARY_VIA_DEEP_LINK.value, true)
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    }
                }

                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val inviteID = deepLink?.getQueryParameter("invite")
                    val isAmbassador = deepLink?.getQueryParameter("is_ambassador")
                    val roleID = deepLink?.getQueryParameter("role_id")
                    val jobProfileID = deepLink?.getQueryParameter("job_profile_id")
                    val ambassadorLatitude = deepLink?.getQueryParameter("latitude")
                    val ambassadorLongitude = deepLink?.getQueryParameter("longitude")
                    val sp =
                        SharedPreAndCommonUtilDataImp(
                            this
                        )
                    sp.saveData(
                        StringConstants.INVITE_USER_ID.value,
                        inviteID
                    )
                    if (!jobProfileID.isNullOrEmpty()) {
                        mainIntent.putExtra(StringConstants.NAV_TO_CLIENT_ACT.value, true)
                        mainIntent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        mainIntent.putExtra(
                            StringConstants.JOB_PROFILE_ID.value,
                            jobProfileID
                        )
                        initApp(mainIntent)
                        return@addOnSuccessListener
                    } else if (!roleID.isNullOrEmpty()) {
                        mainIntent.putExtra(StringConstants.NAV_TO_ROLE.value, true)
                        mainIntent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        mainIntent.putExtra(StringConstants.ROLE_ID.value, roleID)
                        initApp(mainIntent)
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
                        mainIntent.putExtra(StringConstants.INVITE_BY_AMBASSADOR.value, true)
                        mainIntent.putExtra(StringConstants.INVITE_USER_ID.value, inviteID)
                        mainIntent.putExtra(StringConstants.AMBASSADOR_LATITUDE.value,ambassadorLatitude?.toDouble())
                        mainIntent.putExtra(StringConstants.AMBASSADOR_LONGITUDE.value,ambassadorLongitude?.toDouble())
                        initApp(mainIntent)
                        return@addOnSuccessListener
                    }
                }

                initApp(mainIntent)


            }
            .addOnFailureListener(this)
            { e ->
                run {
                    val mainIntent = Intent(this, MainActivity::class.java)
                    //handling deep links
                    intent?.let {
                        val uri = intent.data
                        if (uri != null) {
                            val parameters = uri.pathSegments
                            // after that we are extracting string from that parameters.
                            val param = parameters[parameters.size - 1]
                            if (param == "login_summary"){
                                mainIntent.putExtra(StringConstants.LOGIN_SUMMARY_VIA_DEEP_LINK.value, true)
                            }
                        }
                    }
                    initApp(mainIntent)
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