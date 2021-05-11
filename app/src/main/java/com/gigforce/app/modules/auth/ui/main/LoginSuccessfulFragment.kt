package com.gigforce.app.modules.auth.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
//import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.IEventTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginSuccessfulFragment : BaseFragment() {

    @Inject
    lateinit var eventTracker: IEventTracker

    private val SPLASH_TIME_OUT: Long = 2000 // 1 sec
    var layout: View? = null
    private lateinit var viewModel: LoginSuccessfulViewModel
    private var profileData: ProfileData? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        this.setDarkStatusBarTheme()
        layout = inflateView(R.layout.fragment_login_success, inflater, container)
        return layout
    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginSuccessfulViewModel::class.java)
        observer()
//        layout?.setOnClickListener() {
//        }
//        successful_screen.setOnClickListener(){
//            popFragmentFromStack(R.id.homeScreenIcons)
//            navigateWithAllPopupStack(R.id.homeScreenIcons1);
//        }
        checkForGpsPermissionsAndGpsStatus()
    }

    private fun observer() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            profileData = profile

            profileData?.let {
                if (it.status) {
                    popFragmentFromStack(R.id.loginSuccessfulFragment)
                    if (it.isonboardingdone != null && it.isonboardingdone) {
                        saveOnBoardingCompleted()
//                        navigateWithAllPopupStack(R.id.landinghomefragment)
                        navigateWithAllPopupStack(R.id.onboardingLoaderfragment)
                    } else {

                        navigateWithAllPopupStack(R.id.onboardingfragment)

                    }
                } else
                    showToast(it.errormsg)
            }
        })
    }


    var userGpsDialogActionCount = 0
    private fun checkForGpsPermissionsAndGpsStatus() {
        val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val is_gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (userGpsDialogActionCount == 0 && !is_gps_enabled) {
            showEnableGPSDialog()
            checkInCheckOutSliderBtn?.resetSlider()
            return
        }

        val has_permission_coarse_location = ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (userGpsDialogActionCount == 1 || has_permission_coarse_location) {
            checkAndUpdateUserDetails()
        } else {
            requestPermissionForGPS()
            checkInCheckOutSliderBtn?.resetSlider()
        }
    }

    fun requestPermissionForGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSION_FINE_LOCATION
            )
        }
    }

    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(
                context?.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName(
                    "com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            context?.let { it ->
                LocalBroadcastManager.getInstance(it).sendBroadcast(poke)
            } ?: run {

                FirebaseCrashlytics.getInstance()
                        .log("Context found null in GigPageFragment/turnGPSOn()")
            }
        }
    }

    private fun showEnableGPSDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.confirmation_custom_alert_type1)
        val title = dialog?.findViewById(R.id.title) as TextView
        title.text = "GPS should be turned on to access accurate Location.\n" +
                "Do you want to turn on the GPS?"
        val yesBtn = dialog.findViewById(R.id.yes) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            if (canToggleGPS())
                turnGPSOn()
            else {
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    startActivityForResult(this, REQUEST_CODE_TOGGLE_GPS_MANUAL)
                }
            }

            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            viewModel.getProfileData(0.0, 0.0, "")
            userGpsDialogActionCount = 1

            dialog.dismiss()
        }
        dialog.show()
    }



    private fun canToggleGPS(): Boolean {
        val pacman = context?.getPackageManager()
        var pacInfo: PackageInfo? = null
        try {
            pacInfo = pacman?.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS)
        } catch (e: PackageManager.NameNotFoundException) {
            return false //package not found
        } catch (e: Exception) {

        }
        if (pacInfo != null) {
            for (actInfo in pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true
                }
            }
        }
        return false //default
    }

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private fun initializeGPS() {
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun checkAndUpdateUserDetails() {
        val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val is_GPS_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val has_GPS_permission = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (is_GPS_enabled && has_GPS_permission) {

            if (!isGPSRequestCompleted) {
                initializeGPS()
            }

            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                processLocationAndUpdateUserDetails(it)
            }
        } else if (userGpsDialogActionCount == 0) {
            requestPermissionForGPS()
        } else {
            viewModel.getProfileData(0.0, 0.0, "")
        }
    }

    fun processLocationAndUpdateUserDetails(location: Location?) {

        val latitude: Double = location?.latitude ?: 0.0
        val longitude: Double = location?.longitude ?: 0.0

        var locationAddress = ""
        try {
            val geocoder = Geocoder(requireContext())
            val addressArr = geocoder.getFromLocation(latitude, longitude, 1)
            locationAddress = addressArr?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {

        }

        viewModel.getProfileData(latitude, longitude, locationAddress)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TOGGLE_GPS_MANUAL) {
            checkForGpsPermissionsAndGpsStatus()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGPSRequestCompleted = true
                    initializeGPS()
                    checkForGpsPermissionsAndGpsStatus()
                } else {
                    viewModel.getProfileData(0.0, 0.0, "")
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_TOGGLE_GPS_MANUAL = 121
        const val PERMISSION_FINE_LOCATION = 233
    }
}
