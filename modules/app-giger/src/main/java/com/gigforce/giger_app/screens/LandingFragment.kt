package com.gigforce.giger_app.screens

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.configrepository.ConfigRepository
import com.gigforce.common_ui.deviceInfo_permission.DeviceInfoGatherer
import com.gigforce.common_ui.utils.BsBackgroundAndLocationAccess
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing.*
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class LandingFragment : Fragment(),
    BsBackgroundAndLocationAccess.OnLocationOkayButtonPressClickListener {
    val viewModel: LandingViewModel by viewModels()

    private val requestPermissionContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsAndResult ->
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
    lateinit var appDialogsInterface: AppDialogsInterface

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForLocationPermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel._allLandingData.observe(viewLifecycleOwner, Observer {
            landing_rv.collection = it
        })
        checkforForceupdate()
        logDeviceAndPermissionInfo()
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
                        getString(com.gigforce.landing_screen.R.string.new_version_available),
                        getString(com.gigforce.landing_screen.R.string.new_version_available_detail),
                        getString(com.gigforce.landing_screen.R.string.update_now),
                        getString(com.gigforce.landing_screen.R.string.cancel_update),
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
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }
}