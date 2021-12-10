package com.gigforce.giger_app.screens

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.configrepository.ConfigRepository
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.deviceInfo_permission.DeviceInfoGatherer
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.utils.BsBackgroundAndLocationAccess
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.visible
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.calendar_home_screen.*
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class LandingFragment : Fragment(),
    BsBackgroundAndLocationAccess.OnLocationOkayButtonPressClickListener {
    val viewModel: LandingViewModel by viewModels()

    @Inject lateinit var leadManagementRepository: LeadManagementRepository

    private val requestPermissionContract =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsAndResult ->
            handlePermissionResults(permissionsAndResult)
        }

    private fun handlePermissionResults(permissionsAndResult: MutableMap<String, Boolean>) {
        val hasFinePermission =
            permissionsAndResult[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val permissionRationaleFinePermission =
            requireActivity().shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val hasCoarsePermission =
            permissionsAndResult[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val permissionRationaleCoarsePermission =
            requireActivity().shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //Below Android 10 Background permission access is not required

            if (hasFinePermission && hasCoarsePermission) {
                //we got the permission, don;t do anything
            } else {
                //Permission denied
                if (permissionRationaleFinePermission) {
                    //Permission denied but user didn;t check dont ask again
                } else {
                    //Permission denied and checked dont ask again ,redirect to settings
                    DeviceInfoGatherer.setPermissionAsDeniedAndDontAskAgain(
                        listOf(
                            "Manifest.permission.ACCESS_COARSE_LOCATION",
                            "Manifest.permission.ACCESS_FINE_LOCATION"
                        )
                    )

                    openSettingsPage()
                }
            }
        } else {
            val hasBackgroundLocationPermission =
                permissionsAndResult[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
            val permissionRationaleBckPermission =
                requireActivity().shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            if (hasFinePermission && hasCoarsePermission && hasBackgroundLocationPermission) {
                // Has foreground and background both location access
            } else if (hasFinePermission && hasCoarsePermission) {
                //Just foreground location access
            } else {

                //Permission denied
                if (permissionRationaleFinePermission) {
                    //Permission denied but didn;t check dont ask again
                } else {
                    //Permission denied and checked dont ask again
                    //redirect to settings

                    DeviceInfoGatherer.setPermissionAsDeniedAndDontAskAgain(
                        listOf(
                            "Manifest.permission.ACCESS_COARSE_LOCATION",
                            "Manifest.permission.ACCESS_FINE_LOCATION",
                            "Manifest.permission.ACCESS_BACKGROUND_LOCATION"
                        )
                    )
                    openSettingsPage()
                }
            }
        }
    }

    private fun openSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private val locationAccessDialog: BsBackgroundAndLocationAccess by lazy {
        BsBackgroundAndLocationAccess()
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    @Inject
    lateinit var appDialogsInterface: AppDialogsInterface
    private val chatHeadersViewModel: ChatHeadersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForLocationPermission()
        checkForDeepLink()
        checkForPendingJoining()
    }

    private fun checkForPendingJoining()  = lifecycleScope.launch{
        try {
            leadManagementRepository.getPendingJoinings().apply {
                if(isNotEmpty()){
                    first().let {
                        navigation.navigateTo(
                            "LeadMgmt/PendingJoiningDetails", bundleOf(
                                "joining_id" to it.joiningId
                            ),
                            navOptions = NavigationOptions.getNavOptions()
                        )
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun checkForDeepLink() {
        try {
            when{
                sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_login")?:false->{
                    navigation.navigateTo("gig/tlLoginDetails", bundleOf(
                        StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value to true
                    )
                    )
                }
                sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_onboarding")?:false->{
                    navigation.navigateTo("LeadMgmt/joiningListFragment", bundleOf(
                        StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value to true
                    )
                    )
                }

                sharedPreAndCommonUtilInterface.getDataBoolean(StringConstants.BANK_DETAIL_SP.value)?:false->{
                    navigation.navigateTo("verification/bank_account_fragment")
                }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                        StringConstants.PAN_CARD_SP.value)?:false->{
                    navigation.navigateTo("verification/pancardimageupload")

                        }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                        StringConstants.AADHAR_DETAIL_SP.value)?:false->{
                    navigation.navigateTo("verification/AadharDetailInfoFragment")

                        }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                    StringConstants.DRIVING_LICENCE_SP.value)?:false->{
                    navigation.navigateTo("verification/drivinglicenseimageupload")
                    }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                        StringConstants.VERIFICATION_SP.value)?:false->{
                    navigation.navigateTo("verification/main")

                }
            }



//            val cameFromLoginDeepLink = sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_login")
//            val cameFromOnboardingDeepLink = sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_onboarding")
//            if (cameFromLoginDeepLink == true){
//                Log.d("deepLink", "login")
//                navigation.navigateTo("gig/tlLoginDetails", bundleOf(
//                    StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value to true
//                )
//                )
//            }else if (cameFromOnboardingDeepLink == true){
//                Log.d("deepLink", "onboarding")
//                navigation.navigateTo("LeadMgmt/joiningListFragment", bundleOf(
//                    StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value to true
//                )
//                )
//            }
        }catch (e: Exception){

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel._allLandingData.observe(viewLifecycleOwner, Observer {
            landing_rv.collection = it
        })
        checkforForceupdate()
        logDeviceAndPermissionInfo()
        checkForChatCounts()
    }

    private fun checkForChatCounts() {
        unread_message_count_tv.visible()
        chatHeadersViewModel.unreadMessageCount
            .observe(viewLifecycleOwner, Observer {

                if (it == 0) {
                    unread_message_count_tv.setImageDrawable(null)
                } else {
                    val drawable = TextDrawable.builder().buildRound(
                        it.toString(),
                        ResourcesCompat.getColor(requireContext().resources, R.color.lipstick, null)
                    )
                    unread_message_count_tv.setImageDrawable(drawable)
                }
            })

        chatHeadersViewModel.startWatchingChatHeaders()
    }

    private fun checkForLocationPermission() {
        val locationPermissionGranted = EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!locationPermissionGranted) {
            showLocationDialog()
        }
    }

    private fun showLocationDialog() {

        if (locationAccessDialog.dialog == null || locationAccessDialog.dialog?.isShowing == false) {
            locationAccessDialog.isCancelable = false
            locationAccessDialog.setOnLocationOkayClickListener(this)
            locationAccessDialog.show(
                childFragmentManager,
                BsBackgroundAndLocationAccess::class.simpleName
            )
        }
    }

    private fun logDeviceAndPermissionInfo() {
        try {
            DeviceInfoGatherer.updateDeviceInfoAndPermissionGranted(
                requireContext().applicationContext
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkforForceupdate() {
        ConfigRepository().getForceUpdateCurrentVersion(object :
            ConfigRepository.LatestAPPUpdateListener {
            override fun getCurrentAPPVersion(latestAPPUpdateModel: ConfigRepository.LatestAPPUpdateModel) {
                if (latestAPPUpdateModel.active && isNotLatestVersion(latestAPPUpdateModel))
                //doubt
                    appDialogsInterface.showConfirmationDialogType3(
                        getString(R.string.new_version_available_app_giger),
                        getString(R.string.new_version_available_detail_app_giger),
                        getString(R.string.update_now_app_giger),
                        getString(R.string.cancel_update_app_giger),
                        object :
                            ConfirmationDialogOnClickListener {
                            override fun clickedOnYes(dialog: Dialog?) {
                                redirectToStore("https://play.google.com/store/apps/details?id=com.gigforce.app")
                            }

                            override fun clickedOnNo(dialog: Dialog?) {
                                if (latestAPPUpdateModel.force_update_required)
                                    activity?.finish()
                                dialog?.dismiss()
                            }

                        })
            }
        })
    }

    private fun isNotLatestVersion(latestAPPUpdateModel: ConfigRepository.LatestAPPUpdateModel): Boolean {
        try {
            var currentAppVersion = getAppVersion()
            if (currentAppVersion.contains("Dev")) {
                currentAppVersion = currentAppVersion.split("-")[0]
            }
            var appVersion = currentAppVersion.split(".").toTypedArray()
            var serverAPPVersion =
                latestAPPUpdateModel.force_update_current_version.split(".").toTypedArray()
            if (appVersion.size == 0 || serverAPPVersion.size == 0) {
                FirebaseCrashlytics.getInstance()
                    .log("isNotLatestVersion method : appVersion or serverAPPVersion has zero size!!")
                return false
            } else {
                if (appVersion.get(0).toInt() < serverAPPVersion.get(0).toInt()) {
                    return true
                } else if (appVersion.get(0).toInt() == serverAPPVersion.get(0)
                        .toInt() && appVersion.get(1).toInt() < serverAPPVersion.get(1).toInt()
                ) {
                    return true
                } else return appVersion.get(0).toInt() == serverAPPVersion.get(0)
                    .toInt() && appVersion.get(1).toInt() == serverAPPVersion.get(1)
                    .toInt() && appVersion.get(2).toInt() < serverAPPVersion.get(2).toInt()

            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("isNotLatestVersion Method Exception")

            return false
        }
    }

    fun getAppVersion(): String {
        var result = ""

        try {
            result = context?.packageManager?.getPackageInfo(requireContext().packageName, 0)
                ?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {

        }

        return result
    }

    fun redirectToStore(playStoreUrl: String) {
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(), ResourcesCompat.getColor(
                resources,
                android.R.color.white,
                null
            )
        )

        trySyncingUnsyncedFirebaseData()
    }

    private fun trySyncingUnsyncedFirebaseData() {
        FirebaseFirestore
            .getInstance()
            .waitForPendingWrites()
            .addOnSuccessListener {
                Log.d(
                    "LandingFragment",
                    "Success no pending writes found"
                )
                CrashlyticsLogger.d(
                    "LandingFragment",
                    "Success no pending writes found"
                )
            }.addOnFailureListener {
                Log.e(
                    "LandingFragment",
                    "while syncning data to server",
                    it
                )
                CrashlyticsLogger.e(
                    "LandingFragment",
                    "while syncning data to server",
                    it
                )
            }
    }

    override fun onRequestLocationPermissionButtonClicked() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissionContract.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {

            requestPermissionContract.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION/*,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION*/
                )
            )
        }
    }
}