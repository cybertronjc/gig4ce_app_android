package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import android.Manifest
import android.app.DatePickerDialog
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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.auth.ui.main.LoginSuccessfulFragment
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.app.utils.Lse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_ambsd_user_details.*
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import java.text.SimpleDateFormat
import java.util.*

class AddUserDetailsFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()

    private lateinit var userId: String
    private lateinit var phoneNumber: String
    private var dateOfBirth: Date? = null

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val dateOfBirthPicker: DatePickerDialog by lazy {

        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                dateOfBirth = newCal.time
                date_of_birth_et.setText(dateFormatter.format(newCal.time))
                dob_okay_iv.visible()
            },
            1995,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_details, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber = it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber = it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER, phoneNumber)
    }

    private fun initListeners() {
        user_name_et.textChanged {
            user_name_okay_iv.isVisible = it.length > 2
        }

        date_of_birth_et.setOnClickListener {

            dateOfBirthPicker.datePicker.maxDate = Date().time
            dateOfBirthPicker.show()
        }

        pin_code_et.textChanged {
            pin_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00
        }

        submitBtn.setOnClickListener {
            checkForGpsPermissionsAndGpsStatus()
        }

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }
    }

    private fun validateDataAndsubmit(
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        if (user_name_et.text.length <= 2) {
            showAlertDialog("Invalid name", "Name should be more than 2 characters")
            return
        }

        if (dateOfBirth == null) {
            showAlertDialog("Dob not filled", "Select your date of birth")
            return
        }

        if (gender_chip_group.checkedChipId == -1) {
            showAlertDialog("select Gender", "Select your gender")
            return
        }

//        if (pin_code_et.text.length != 6 && pin_code_et.text.toString().toInt() > 10_00_00) {
//            showAlertDialog("Invalid pincode", "Provide a valid Pin Code")
//            return
//        }

        if (highest_qual_chipgroup.checkedChipId == -1) {
            showAlertDialog("Select highest qualification", "Please fill highest qualification")
            return
        }

        viewModel.updateUserDetails(
            uid = userId,
            phoneNumber = phoneNumber,
            name = user_name_et.text.toString(),
            dateOfBirth = dateOfBirth!!,
            gender = gender_chip_group.findViewById<Chip>(gender_chip_group.checkedChipId).text.toString(),
            highestQualification = highest_qual_chipgroup.findViewById<Chip>(highest_qual_chipgroup.checkedChipId).text.toString(),
            latitude = latitude,
            longitude = longitude,
            address = address
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lse.Loading -> {
//                        UtilMethods.showLoading(requireContext())
                    }
                    Lse.Success -> {
                        //                      UtilMethods.hideLoading()
                        showToast("User Details submitted")
                        navigate(
                            R.id.addProfilePictureFragment, bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to user_name_et.text.toString(),
                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pin_code_et.text.toString()
                            )
                        )
                    }
                    is Lse.Error -> {
                        //                       UtilMethods.hideLoading()
                        showAlertDialog("Could not submit info", it.error)
                    }
                }
            })
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to go back")
            .setPositiveButton("Yes") { _, _ -> goBackToUsersList() }
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private fun goBackToUsersList() {
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    var userGpsDialogActionCount = 0
    private fun checkForGpsPermissionsAndGpsStatus() {
        val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val is_gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (userGpsDialogActionCount == 0 && !is_gps_enabled) {
            showEnableGPSDialog()
            checkInCheckOutSliderBtn?.resetSlider()
            return;
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
                ), GigPageFragment.PERMISSION_FINE_LOCATION
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
        title.text =
            "Important!!, Gps Not Turned On" + "\n" + "You will be redirected to settings page , Please turn on GPS and set mode to High Accuracy"
        val yesBtn = dialog.findViewById(R.id.yes) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            if (canToggleGPS())
                turnGPSOn()
            else {
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    startActivityForResult(
                        this,
                        LoginSuccessfulFragment.REQUEST_CODE_TOGGLE_GPS_MANUAL
                    )
                }
            }

            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            validateDataAndsubmit(0.0, 0.0, "")
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
            validateDataAndsubmit(0.0, 0.0, "")
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

        validateDataAndsubmit(latitude, longitude, locationAddress)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GigPageFragment.PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGPSRequestCompleted = true
                    initializeGPS()
                } else {
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == LoginSuccessfulFragment.REQUEST_CODE_TOGGLE_GPS_MANUAL){
            checkForGpsPermissionsAndGpsStatus()
        }
    }


}