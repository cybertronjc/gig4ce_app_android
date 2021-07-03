package com.gigforce.app

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.gigforce.app.modules.onboardingmain.OnboardingMainFragment
import com.gigforce.app.notification.ChatNotificationHandler
import com.gigforce.app.notification.MyFirebaseMessagingService
import com.gigforce.app.notification.NotificationConstants
import com.gigforce.app.utils.GigNavigation
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.viewdatamodels.landing.VersionUpdateInfo
import com.gigforce.core.IEventTracker
import com.gigforce.core.INavigationProvider
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.popAllBackStates
import com.gigforce.core.extensions.printDebugLog
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.landing_screen.landingscreen.LandingScreenFragment
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.GsonBuilder
import com.moengage.core.internal.utils.MoEUtils.showToast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavFragmentsData,
    INavigationProvider, InstallStateUpdatedListener
{

    private var bundle: Bundle? = null
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false
    val MIXPANEL_TOKEN = "536f16151a9da631a385119be6510d56"
    private lateinit var appUpdateManager: AppUpdateManager
    var currentPriority: Int? = 0
    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }
//    var mixpanel : MixpanelAPI? = null
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
    lateinit var navigation:INavigation
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    @Inject
    lateinit var appDialogsInterface: AppDialogsInterface
    @Inject lateinit var eventTracker: IEventTracker

    override fun getINavigation(): INavigation {
        return navigation
    }

    private val chatNotificationHandler: ChatNotificationHandler by lazy {
        ChatNotificationHandler(applicationContext)
    }

    private val intentFilters = IntentFilter(NotificationConstants.BROADCAST_ACTIONS.SHOW_CHAT_NOTIFICATION)
    private val notificationIntentRecevier = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val remoteMessage: RemoteMessage = intent?.getParcelableExtra(MyFirebaseMessagingService.INTENT_EXTRA_REMOTE_MESSAGE)
                    ?: return

            if(!isUserLoggedIn()){
                Log.d("MainActivity", "User Not logged in, not showing chat notification")
                return
            }

            if (navController.currentDestination?.label != "fragment_chat_list" &&
                    navController.currentDestination?.label  != "fragment_chat_page")
                chatNotificationHandler.handleChatNotification(remoteMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            finish();
            return;
        }
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        eventTracker.setUpAnalyticsTools()
        appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        setupFirebaseConfig()

        intent?.extras?.let {
            it.printDebugLog("printDebugLog")
        }

        navController = this.findNavController(R.id.nav_fragment)
        navController.handleDeepLink(intent)
       // sendCommandToService(TrackingConstants.ACTION_START_OR_RESUME_SERVICE)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationIntentRecevier,
            intentFilters
        )
        if (Intent.ACTION_SEND == intent.action && isUserLoggedIn()) {
            //User Clicked on share in gallery
            formatDataSharedAndOpenChat(intent!!)
        } else if (Intent.ACTION_SEND_MULTIPLE == intent.action && isUserLoggedIn()) {
            formatMultipleDataSharedAndOpenChat(intent)
        } else {
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

//                LandingScreenFragmentDirections.openRoleDetailsHome( intent.getStringExtra(StringConstants.ROLE_ID.value),true)
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
        }

        if (firebaseAuth.currentUser != null) {
            lookForNewChatMessages()
        }
    }

    private fun formatDataSharedAndOpenChat(intent: Intent) {
        if (intent.type?.startsWith(MimeTypes.IMAGE_MATCHER) == true) {
            handleSendImage(intent) // Handle single image being sent
        } else if (intent.type?.startsWith(MimeTypes.VIDEO_MATCHER) == true) {
            handleVideoImage(intent)
        } else if (
                MimeTypes.DOC == intent.type ||
                MimeTypes.DOCX == intent.type ||
                MimeTypes.PDF == intent.type ||
                MimeTypes.XLS == intent.type ||
                MimeTypes.XLSX == intent.type
        ) {
            handleDocumentSend(intent)
        }
    }

    private fun formatMultipleDataSharedAndOpenChat(intent: Intent) {
        val filesSelectedUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)

        if(filesSelectedUris.size > 10){
            Toast.makeText(this, "Max 10 files can be shared at once", Toast.LENGTH_SHORT).show()
            proceedWithNormalNavigation()
            return
        }

        val documentsUris :ArrayList<Uri> = arrayListOf()
        val imagesUri :ArrayList<Uri> = arrayListOf()
        val videosUri :ArrayList<Uri> = arrayListOf()

        filesSelectedUris.forEach {

            val mimeType = contentResolver.getType(it)

            if (mimeType?.startsWith(MimeTypes.IMAGE_MATCHER) == true) {
                imagesUri.add(it)
            } else if (mimeType?.startsWith(MimeTypes.VIDEO_MATCHER) == true) {
                videosUri.add(it)
            } else if (
                    MimeTypes.DOC == mimeType ||
                    MimeTypes.DOCX == mimeType ||
                    MimeTypes.PDF == mimeType||
                    MimeTypes.XLS == mimeType ||
                    MimeTypes.XLSX == mimeType
            ) {
                documentsUris.add(it)
            }
        }

        val itemSharedBundle = bundleOf(
                ChatPageFragment.INTENT_EXTRA_SHARED_VIDEOS to videosUri,
                ChatPageFragment.INTENT_EXTRA_SHARED_IMAGES to imagesUri,
                ChatPageFragment.INTENT_EXTRA_SHARED_DOCUMENTS to documentsUris
        )

        navController.navigate(R.id.chatListFragment,
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to itemSharedBundle)
        )
    }

    private fun handleDocumentSend(intent: Intent) {
        val documentUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val sharedFileBundle = bundleOf(
                ChatPageFragment.INTENT_EXTRA_SHARED_DOCUMENTS to arrayListOf(documentUri)
        )

        navController.navigate(R.id.chatListFragment,
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to sharedFileBundle)
        )
    }

    private fun handleVideoImage(intent: Intent) {
        val documentUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val sharedFileBundle = bundleOf(
                ChatPageFragment.INTENT_EXTRA_SHARED_VIDEOS to arrayListOf(documentUri)
        )

        navController.navigate(R.id.chatListFragment,
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to sharedFileBundle)
        )
    }

    private fun handleSendImage(intent: Intent) {
        val documentUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val sharedFileBundle = bundleOf(
                ChatPageFragment.INTENT_EXTRA_SHARED_IMAGES to arrayListOf(documentUri)
        )

        navController.navigate(R.id.chatListFragment,
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to sharedFileBundle)
        )
    }

    private fun isUserLoggedIn(): Boolean {
       return FirebaseAuth.getInstance().currentUser != null
    }

    private fun lookForNewChatMessages() {
        chatHeadersViewModel.startWatchingChatHeaders()
    }

    private fun handleDeepLink() {
        if(!isUserLoggedIn()){
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
                GigNavigation.openGigAttendancePage(navController, false, intent.extras)
            }
            NotificationConstants.CLICK_ACTIONS.OPEN_GIG_ATTENDANCE_PAGE_2 -> {
                Log.d("MainActivity", "redirecting to attendance page 2")
                navController.popAllBackStates()
                GigNavigation.openGigAttendancePage(navController, true, intent.extras)
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
//        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("MAIN_ACTIVITY_CREATED")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
        intent?.extras?.let {
            it.printDebugLog("printDebugLog")
        }

        if (Intent.ACTION_SEND == intent?.action && isUserLoggedIn()) {
            //User Clicked on share in gallery
            formatDataSharedAndOpenChat(intent)
        } else if (Intent.ACTION_SEND_MULTIPLE == intent?.action && isUserLoggedIn()) {
            formatMultipleDataSharedAndOpenChat(intent)
        } else if (intent?.getStringExtra(IS_DEEPLINK) == "true") {
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
//        mixpanel?.flush();
        appUpdateManager.unregisterListener(this)

    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment?

        var fragmentholder: Fragment? =
            navHostFragment!!.childFragmentManager.fragments[navHostFragment!!.childFragmentManager.fragments.size - 1]
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

    private fun setupFirebaseConfig(){
        val update_cancelled = sharedPreAndCommonUtilInterface.getDataBoolean("update_cancelled")
//        Log.d("Update_cancelled", ""+update_cancelled)
        try {
            //Log.d("Update", "Data fetched from Remote Config")
            //showToast("Data fetched from Remote Config", this)
            val appUpdatePriority = firebaseRemoteConfig.getString("app_update_priority")
            if (update_cancelled == true) runOnceADay(appUpdatePriority) else checkforUpdate(appUpdatePriority)
        }
        catch (e: Exception){
            e.printStackTrace()
            //Log.d("Update", "Fetching error from Remote config")
            //showToast("Fetching error from Remote config", this)
        }
    }
    private fun showRestartDialog() {
        //appUpdateManager?.completeUpdate()
        appDialogsInterface.showConfirmationDialogType3(
            getString(R.string.restart_update),
            getString(R.string.new_version_available_detail),
            getString(R.string.restart),
            getString(R.string.cancel_update),
            object :
                ConfirmationDialogOnClickListener {
                override fun clickedOnYes(dialog: Dialog?) {
                    //restartAppUpdate()
                    appUpdateManager?.completeUpdate()
                }
                override fun clickedOnNo(dialog: Dialog?) {
                    dialog?.dismiss()
                }
            })
    }

    private fun restartAppUpdate() {
        val intent = baseContext?.packageManager?.getLaunchIntentForPackage(baseContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun checkforUpdate(appUpdatePriority: String?) {
        val gson = GsonBuilder().create()
        appUpdatePriority?.let {
            val versionUpdateInfo = gson.fromJson(it, VersionUpdateInfo::class.java)
            currentPriority = getCurrentVersionCode()?.let { getUpdatePriority(it, versionUpdateInfo) }
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    if ((currentPriority == 0 /* flexible priority */ || currentPriority == -1 /* default priority*/)
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Request the update.
                        requestUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                        appUpdateManager.registerListener(this@MainActivity)
                        showToast("Update Available", this)

                    } else if (currentPriority == 1 /* immediate priority */
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        //request for immediate update
                        requestUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                        showToast("Update Available", this)
                    }
                }else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    //showToast("Update not available", this)
                }
            }
        }
    }
    fun getCurrentVersionCode(): Int? {
        try {
            val versionCode = BuildConfig.VERSION_CODE
            return versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
    fun getUpdatePriority(currentAppVersion: Int, info: VersionUpdateInfo) : Int {
        val mostImportantUpdate = info.updates
            .filter { it.version > currentAppVersion }
            ?.sortedByDescending { it.updatePriority }
        return if (mostImportantUpdate.size > 0) mostImportantUpdate[0].updatePriority else -1
    }
    private fun requestUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        try {
            //showToast("Start update intent", this)
            Log.d("Update", "Start update intent")
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                updateType, //  HERE specify the type of update flow you want
                    this@MainActivity,   //  the instance of an activity
                UPDATE_REQUEST_CODE
            )
        }
        catch (e: java.lang.Exception){
            e.printStackTrace()
            Log.d("Update", "Start update intent error")
            //showToast("Start update intent error", this)
        }

    }
    fun runOnceADay(appUpdatePriority: String?) {
        val lastCheckedMillis = sharedPreAndCommonUtilInterface.getLong("once_a_day")
        val update_cancelled = sharedPreAndCommonUtilInterface.getDataBoolean("update_cancelled")
        val now = System.currentTimeMillis()
        val diffMillis = now - lastCheckedMillis
        if (update_cancelled == true && (diffMillis >= oneHour * 12)) { // in  12 hours
            sharedPreAndCommonUtilInterface.saveLong("once_a_day", now)
            //check for update
            checkforUpdate(appUpdatePriority)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    Log.d("Update", "" + "Result Ok")
                    eventTracker.pushEvent(TrackingEventArgs("Update Requested by User", null))
                    if (currentPriority == 0){
                        showToast("Update is being downloaded in background", this)
                    }
                }
                RESULT_CANCELED -> {
                    //  handle user's rejection
                    //showToast("Update Cancelled by User", this)
                    eventTracker.pushEvent(TrackingEventArgs("Update Cancelled by User", null))
                    if (currentPriority == 1) {
                        //request the update again
                        appDialogsInterface.showConfirmationDialogType3(
                            getString(com.gigforce.landing_screen.R.string.new_version_available),
                            getString(com.gigforce.landing_screen.R.string.new_version_available_detail),
                            getString(com.gigforce.landing_screen.R.string.update_now),
                            getString(com.gigforce.landing_screen.R.string.cancel_update),
                            object :
                                ConfirmationDialogOnClickListener {
                                override fun clickedOnYes(dialog: Dialog?) {
                                    appUpdateManager
                                        ?.appUpdateInfo
                                        ?.addOnSuccessListener { appUpdateInfo ->
                                            requestUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                                        }
                                }
                                override fun clickedOnNo(dialog: Dialog?) {
                                    dialog?.dismiss()
                                    finish()
                                }
                            })
                    } else {
                        //user can use the app
                        sharedPreAndCommonUtilInterface.saveDataBoolean("update_cancelled", true)
                    }
                }
                RESULT_IN_APP_UPDATE_FAILED -> {
                    //if you want to request the update again just call checkUpdate()
                    Log.d("Update", "" + "Update Internal Failure")
                    //  handle update failure
                    //showToast("Update Failure Internal", this)
                    eventTracker.pushEvent(TrackingEventArgs("Update Failed", null))
                    CrashlyticsLogger.d("InAppUpdate", "Update Internal Failure" )
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showRestartDialog()
                    showToast("Update Downloaded", this)
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS && currentPriority == 1
                ) {
                    showToast("Update download in progress", this)
                    // If an in-app update is already running, resume the update.
                    requestUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                }
                else {
                    //Update failed
                    eventTracker.pushEvent(TrackingEventArgs("Update Failed on Resume", null))
                    CrashlyticsLogger.d("InAppUpdate", "Update Failed on Resume , ${appUpdateInfo.installStatus()}")
                }

            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        /*
        todo: Check if hiding Keyboard is really required!
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }*/
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val IS_DEEPLINK = "is_deeplink"
        private const val UPDATE_REQUEST_CODE = 100
        private const val oneHour = 1000 * 60 * 60 // this result to 3600000
    }

    override fun setData(bundle: Bundle) {
        this.bundle = bundle;
    }

    override fun getData(): Bundle {
        return bundle ?: Bundle()
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED){
            showToast("download completed", this)
            showRestartDialog()
            appUpdateManager?.unregisterListener(this)
//            currentPriority?.let {
//                if (currentPriority == 0) showRestartDialog() else restartAppUpdate()
//            }
        }
    }

}
