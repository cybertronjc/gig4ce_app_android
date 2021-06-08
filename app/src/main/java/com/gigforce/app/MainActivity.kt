package com.gigforce.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.modules.onboardingmain.OnboardingMainFragment
import com.gigforce.app.notification.ChatNotificationHandler
import com.gigforce.app.notification.MyFirebaseMessagingService
import com.gigforce.app.notification.NotificationConstants
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.IEventTracker
import com.gigforce.core.INavigationProvider
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.extensions.popAllBackStates
import com.gigforce.core.extensions.printDebugLog
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.landing_screen.landingscreen.LandingScreenFragment
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavFragmentsData,
    INavigationProvider {

    private var bundle: Bundle? = null
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val chatHeadersViewModel: ChatHeadersViewModel by lazy {
        ViewModelProvider(this).get(ChatHeadersViewModel::class.java)
    }

    fun getNavController(): NavController {
        return this.navController
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var shareDataAndCommUtil: SharedPreAndCommonUtilInterface

    override fun getINavigation(): INavigation {
        return navigation
    }

    private val chatNotificationHandler: ChatNotificationHandler by lazy {
        ChatNotificationHandler(applicationContext)
    }

    private val intentFilters =
        IntentFilter(NotificationConstants.BROADCAST_ACTIONS.SHOW_CHAT_NOTIFICATION)
    private val notificationIntentRecevier = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val remoteMessage: RemoteMessage =
                intent?.getParcelableExtra(MyFirebaseMessagingService.INTENT_EXTRA_REMOTE_MESSAGE)
                    ?: return

            if (!isUserLoggedIn()) {
                Log.d("MainActivity", "User Not logged in, not showing chat notification")
                return
            }

            if (navController.currentDestination?.label != "fragment_chat_list" &&
                navController.currentDestination?.label != "fragment_chat_page"
            )
                chatNotificationHandler.handleChatNotification(remoteMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        eventTracker.setUpAnalyticsTools()


        intent?.extras?.let {
            it.printDebugLog("printDebugLog")
        }

        navController = this.findNavController(R.id.nav_fragment)
        navController.handleDeepLink(intent)
        // sendCommandToService(TrackingConstants.ACTION_START_OR_RESUME_SERVICE)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(notificationIntentRecevier, intentFilters)

        when {
            intent.getBooleanExtra(StringConstants.NAV_TO_CLIENT_ACT.value, false) -> {

                if (!isUserLoggedIn()) {
                    proceedWithNormalNavigation()
                    return
                }

                navController.popBackStack()
                navController.navigate(
                    R.id.fragment_client_activation, bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to intent.getStringExtra(
                            StringConstants.JOB_PROFILE_ID.value
                        ),
                        StringConstants.INVITE_USER_ID.value to intent.getStringExtra(
                            StringConstants.INVITE_USER_ID.value
                        ),
                        StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value to true
                    )
                )
            }

            intent.getBooleanExtra(StringConstants.NAV_TO_ROLE.value, false) -> {

                if (!isUserLoggedIn()) {
                    proceedWithNormalNavigation()
                    return
                }

                navController.popBackStack()
                navController.navigate(
                    R.id.fragment_role_details, bundleOf(
                        StringConstants.ROLE_ID.value to intent.getStringExtra(
                            StringConstants.ROLE_ID.value
                        ),
                        StringConstants.INVITE_USER_ID.value to intent.getStringExtra(
                            StringConstants.INVITE_USER_ID.value
                        ),
                        StringConstants.ROLE_VIA_DEEPLINK.value to true
                    )
                )
            }
            intent.getStringExtra(IS_DEEPLINK) == "true" -> {
                handleDeepLink()
            }
            else -> {
                proceedWithNormalNavigation()
            }
        }

        if (firebaseAuth.currentUser != null) {
            lookForNewChatMessages()
        }

        profileDataSnapshot()

    }

    private fun profileDataSnapshot() {
        FirebaseAuth.getInstance().addAuthStateListener { it1 ->
            it1.currentUser?.uid?.let {
                FirebaseFirestore.getInstance().collection("Profiles").document(it)
                    .addSnapshotListener { value, e ->
                        value?.data?.let {
                            value.toObject(ProfileData::class.java)?.let {
                                shareDataAndCommUtil.saveLoggedInMobileNumber(
                                    it1.currentUser?.phoneNumber ?: ""
                                )
                                shareDataAndCommUtil.saveLoggedInUserName(it.name)
                                shareDataAndCommUtil.saveUserProfilePic(
                                    it.profileAvatarThumbnail ?: ""
                                )
                            }
                        }
                    }
            } ?: run {
                shareDataAndCommUtil.saveLoggedInMobileNumber("")
                shareDataAndCommUtil.saveLoggedInUserName("")
                shareDataAndCommUtil.saveUserProfilePic("")
            }

        }
    }

    private fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun lookForNewChatMessages() {
        chatHeadersViewModel.startWatchingChatHeaders()
    }

    private fun handleDeepLink() {
        if (!isUserLoggedIn()) {
            Log.d("MainActivity", "User Not logged in, not handling deep link")
            proceedWithNormalNavigation()
            return
        }

        val clickAction = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_CLICK_ACTION)
        Log.d("MainActivity", "Click action received $clickAction ")

        when (intent.getStringExtra(NotificationConstants.INTENT_EXTRA_CLICK_ACTION)) {
            NotificationConstants.CLICK_ACTIONS.OPEN_GIG_ATTENDANCE_PAGE -> {
                Log.d("MainActivity", "redirecting to attendance page")
                navController.popAllBackStates()
                navigation.navigateTo("gig/attendance",intent.extras)
//                GigNavigation.openGigAttendancePage(navController, false, intent.extras)
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_GIG_ATTENDANCE_PAGE_2 -> {
                Log.d("MainActivity", "redirecting to attendance page 2")
                navController.popAllBackStates()
                navigation.navigateTo("gig/attendance",intent.extras)
//                GigNavigation.openGigAttendancePage(navController, true, intent.extras)
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_VERIFICATION_PAGE -> {
                Log.d("MainActivity", "redirecting to gig verification page")
                navController.popAllBackStates()
                navController.navigate(
                    R.id.gigerVerificationFragment,
                    intent.extras
                )
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_CHAT_PAGE -> {
                Log.d("MainActivity", "redirecting to gig verification page")
                navController.popAllBackStates()
                navController.navigate(
                    R.id.chatPageFragment,
                    intent.extras.apply {
                        this?.putString(
                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE,
                            ChatConstants.CHAT_TYPE_USER
                        )
                    }
                )
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_GROUP_CHAT_PAGE -> {
                Log.d("MainActivity", "redirecting to gig verification page")
                navController.popAllBackStates()
                navController.navigate(
                    R.id.chatPageFragment,
                    intent.extras.apply {
                        this?.putString(
                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE,
                            ChatConstants.CHAT_TYPE_GROUP
                        )
                    }
                )
            }
            else -> {
                navController.popAllBackStates()
                navController.navigate(
                    R.id.landinghomefragment,
                    intent.extras
                )
            }
        }
    }

    private fun proceedWithNormalNavigation() {
        checkForAllAuthentication()
        GetFirebaseInstanceID()
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("MAIN_ACTIVITY_CREATED")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
        intent?.extras?.let {
            it.printDebugLog("printDebugLog")
        }

        if (intent?.getStringExtra(IS_DEEPLINK) == "true") {
            handleDeepLink()
        }
    }

    private fun GetFirebaseInstanceID() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Firebase/InstanceId", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = token //getString(R.string.msg_token_fmt, token)
                Log.v("Firebase/InstanceId", "Firebase Token Received")
                Log.v("Firebase/InstanceId", msg)
                //  Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
    }

    private fun checkForAllAuthentication() {
        navController.popAllBackStates()
        navController.navigate(R.id.authFlowFragment)
//        navController.navigate(R.id.languageSelectFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationIntentRecevier)

    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment?

        var fragmentholder: Fragment? =
            navHostFragment!!.childFragmentManager.fragments[navHostFragment.childFragmentManager.fragments.size - 1]
        var handled = false
        try {
            handled = (fragmentholder as IOnBackPressedOverride).onBackPressed()
        } catch (e: Exception) {
        }

        if (!handled) {
            if (isMainScreen(fragmentholder) || isOnBoarding(fragmentholder)) {
                doubleBackPressFun()
            } else super.onBackPressed()
        }

    }

    private fun isOnBoarding(fragmentholder: Fragment?): Boolean {
        try {
            var isOnBoarding = (fragmentholder as OnboardingMainFragment)
            if (isOnBoarding != null) return true
        } catch (e: Exception) {
        }
        return false

    }

    private fun isMainScreen(fragmentholder: Fragment?): Boolean {
        try {
            var isMainHome = (fragmentholder as LandingScreenFragment)
            if (isMainHome != null) return true
        } catch (e: Exception) {
        }
        return false
    }

    private fun doubleBackPressFun() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to close the app", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    companion object {
        const val IS_DEEPLINK = "is_deeplink"
    }

    override fun setData(bundle: Bundle) {
        this.bundle = bundle
    }

    override fun getData(): Bundle {
        return bundle ?: Bundle()
    }
}
