package com.gigforce.app.modules.gigPage

import android.Manifest
import android.app.Activity
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
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.addressTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.attendanceCardView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.callCardView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.companyLogoIV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.companyNameTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.contactPersonTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.durationTextTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.favoriteCB
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.gigIdTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.gigTypeTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.messageCardView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.roleNameTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.shiftTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.wageTV
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class GigAttendancePageFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    DeclineGigDialogFragmentResultListener {
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
    }

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_FINE_LOCATION = 100
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm aa", Locale.getDefault())

    private val viewModel: GigViewModel by viewModels()

    private lateinit var gigId: String
    private var gig: Gig? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_attendance, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel(savedInstanceState)

        requestPermissionForGPS()
        listener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
    }

    var userGpsDialogActionCount = 0

    private fun listener() {
        startNavigationSliderBtn?.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {
                    override fun onSlideComplete(view: SlideToActView) {
                        var manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        var statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        if (userGpsDialogActionCount == 0 && !statusOfGPS) {
                            showEnableGPSDialog()
                            startNavigationSliderBtn?.resetSlider()
                            return;
                        }

                        if (userGpsDialogActionCount == 1 || ContextCompat.checkSelfPermission(
                                        requireActivity(),
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            var intent = Intent(context, ImageCaptureActivity::class.java)
                            startActivityForResult(intent, REQUEST_CODE_UPLOAD_SELFIE_IMAGE)
                        } else {
                            requestPermissionForGPS()
                            startNavigationSliderBtn?.resetSlider()
                        }
                    }
                }

    }

    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(context?.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.setData(Uri.parse("3"))
            context?.let { it -> LocalBroadcastManager.getInstance(it).sendBroadcast(poke) }

        }
    }

    private fun showEnableGPSDialog() {
        showConfirmationDialogType2("Please enable your GPS!!\n                                                               ",
                object : ConfirmationDialogOnClickListener {
                    override fun clickedOnYes(dialog: Dialog?) {
                        if (canToggleGPS()) turnGPSOn()
                        else {
                            showToast("Please Enable your GPS manually in setting!!")
                        }
                        dialog?.dismiss()
                    }

                    override fun clickedOnNo(dialog: Dialog?) {
                        popFragmentFromStack(R.id.earningFragment)
                        userGpsDialogActionCount = 1
                        dialog?.dismiss()
                    }

                })
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


    private fun initView() {
        cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        ellipses_btn.setOnClickListener {

            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_gig_attendance, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this@GigAttendancePageFragment)
            popupMenu.show()
        }

        seeMoreBtn.setOnClickListener {

            navigate(R.id.presentGigPageFragment, Bundle().apply {
                this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, gigId)
                this.putBoolean(GigPageFragment.INTENT_EXTRA_COMING_FROM_CHECK_IN, true)
            })
        }

        messageCardView.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        favoriteCB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && gig?.isFavourite!!.not()) {
                viewModel.favoriteGig(gigId)
                showToast("Marked As Favourite")
            } else if (!isChecked && gig?.isFavourite!!) {
                viewModel.unFavoriteGig(gigId)
                showToast("Unmarked As Favourite")
            }
        }

        callCardView.setOnClickListener {

            if (gig?.gigContactDetails?.contactNumber != 0L) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.gigContactDetails?.contactNumber?.toString(), null))
                startActivity(intent)
            }
        }
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID)!!
        } ?: run {
            arguments?.let {
                gigId = it.getString(INTENT_EXTRA_GIG_ID)!!
            }?.run {
                FirebaseCrashlytics.getInstance().log("GigAttendancePageFragment getData method : savedInstanceState and arguments found null")
                FirebaseCrashlytics.getInstance().setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            }
        }
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lce.Loading -> {
                        }
                        is Lce.Content -> setGigDetailsOnView(it.content)
                        is Lce.Error -> {
                        }
                    }
                })

        viewModel.watchGig(gigId)
    }

    private fun setGigDetailsOnView(gig: Gig) {
        this.gig = gig
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = "Gig Id : ${gig.gigId}"

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                Glide.with(requireContext())
                        .load(gig.companyLogo)
                        .into(companyLogoIV)
            } else {
                FirebaseStorage.getInstance()
                        .getReference("companies_gigs_images")
                        .child(gig.companyLogo!!)
                        .downloadUrl
                        .addOnSuccessListener { fileUri ->
                            Glide.with(requireContext())
                                    .load(fileUri)
                                    .into(companyLogoIV)
                        }
            }
        } else {
            val companyInitials = if (gig.companyName.isNullOrBlank())
                "C"
            else
                gig.companyName!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            companyLogoIV.setImageDrawable(drawable)
        }

        if (gig.endDateTime != null) {

            val startDate = gig.startDateTime!!.toLocalDate()
            val endDate = gig.endDateTime!!.toLocalDate()

            if (startDate.isEqual(endDate))
                durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
            else
                durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"

            shiftTV.text =
                    "${timeFormatter.format(gig.startDateTime!!.toDate())} - ${timeFormatter.format(gig.endDateTime!!.toDate())}"
        } else {
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
            shiftTV.text = "${timeFormatter.format(gig.startDateTime!!.toDate())} - "
        }

        if (gig.gigAmount == 0.0) {
            wageTV.text = "Payout : As per contract"
        } else {
            wageTV.text = if (gig.isMonthlyGig)
                "Payout : Rs ${gig.gigAmount} per Month"
            else
                "Payout : Rs ${gig.gigAmount} per Hour"
        }

        addressTV.text = gig.address

        if (gig.isFavourite && favoriteCB.isChecked.not()) {
            favoriteCB.isChecked = true
        } else if (gig.isFavourite.not() && favoriteCB.isChecked) {
            favoriteCB.isChecked = false
        }

        contactPersonTV.text = gig.gigContactDetails?.contactName
        callCardView.isVisible = gig.gigContactDetails?.contactNumber != 0L

        addressTV.setOnClickListener {

            //Launch Map
            val lat = gig.latitude
            val long = gig.longitude

            if (lat != null) {
                val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        if (gig.latitude != null) {
            addressTV.text = prepareAddress(gig.address)
        } else {
            addressTV.text = gig.address
        }

        if (gig.isPresentGig()) {

            if (gig.isCheckInAndCheckOutMarked()) {
                //Attendance have been marked show it
                startNavigationSliderBtn?.gone()

                if (gig.isCheckInMarked())
                    punchInTimeTV.text =
                            timeFormatter.format(gig.attendance!!.checkInTime!!)
                else
                    punchInTimeTV.text = "--:--"

                if (gig.isCheckOutMarked())
                    punchOutTimeTV.text =
                            timeFormatter.format(gig.attendance!!.checkOutTime!!)
                else
                    punchOutTimeTV.text = "--:--"
            } else {
                //Show Check In Controls
                startNavigationSliderBtn?.visible()

                if (!gig.isCheckInMarked()) {
                    startNavigationSliderBtn?.let {
                        if (it.isCompleted()) {
                            it.resetSlider()
                        }
                    }


                    attendanceCardView.setBackgroundColor(
                            ResourcesCompat.getColor(
                                    resources,
                                    R.color.light_pink,
                                    null
                            )
                    )

                    startNavigationSliderBtn?.text = "Check-in"
                } else if (!gig.isCheckOutMarked()) {
                    startNavigationSliderBtn?.let {
                        if (it.isCompleted()) {
                            it.resetSlider()
                        }
                    }


                    if (gig.isCheckInMarked())
                        punchInTimeTV.text =
                                timeFormatter.format(gig.attendance!!.checkInTime!!)
                    else
                        punchInTimeTV.text = "--:--"

                    startNavigationSliderBtn?.text = "Check-out"
                }
            }
        } else if (gig.isPastGig()) {
            startNavigationSliderBtn?.gone()

            if (gig.isCheckInMarked())
                punchInTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkInTime!!)
            else
                punchInTimeTV.text = "--:--"

            if (gig.isCheckOutMarked())
                punchOutTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkOutTime!!)
            else
                punchOutTimeTV.text = "--:--"

        } else {
            startNavigationSliderBtn?.gone()
        }
    }

    private fun prepareAddress(address: String): SpannableString {
        if (address.isBlank())
            return SpannableString("")

        val string = SpannableString(address + GigPageFragment.TEXT_VIEW_ON_MAP)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), address.length + 1, string.length - 1, 0)
        return string
    }

    private fun initializeGPS() {
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    fun requestPermissionForGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ), PERMISSION_FINE_LOCATION
            )
        }
    }

    private fun checkAndUpdateAttendance() {
        var manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (statusOfGPS && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!isGPSRequestCompleted) {
                initializeGPS()
            }

            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                updateAttendanceOnDBCall(it)
            }
        } else if (userGpsDialogActionCount == 0) {
            requestPermissionForGPS()
        } else {
            if (gig!!.attendance == null || !gig!!.attendance!!.checkInMarked) {
                var markAttendance =
                        GigAttendance(
                                true,
                                Date(),
                                0.0,
                                0.0,
                                selfieImg,
                                ""
                        )
                viewModel.markAttendance(markAttendance, gigId)

            } else {
                gig!!.attendance!!.setCheckout(
                        true, Date(), 0.0,
                        0.0, selfieImg,
                        ""
                )
                viewModel.markAttendance(gig!!.attendance!!, gigId)

            }
        }

    }

    var selfieImg: String = ""

    fun updateAttendanceOnDBCall(location: Location?) {
        val latitude: Double = location?.latitude ?: 0.0
        val longitude: Double = location?.longitude ?: 0.0

        var locationAddress = ""
        try {
            val geocoder = Geocoder(requireContext())
            val addressArr = geocoder.getFromLocation(latitude, longitude, 1)
            locationAddress = addressArr?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {

        }



        gig?.let {

            val ifAttendanceMarked = it.attendance?.checkInMarked ?: false

            if (!ifAttendanceMarked) {
                val markAttendance =
                        GigAttendance(
                                true,
                                Date(),
                                latitude,
                                longitude,
                                selfieImg,
                                locationAddress
                        )
                viewModel.markAttendance(markAttendance, gigId)
            } else {
                it.attendance?.setCheckout(
                        true,
                        Date(),
                        latitude,
                        longitude,
                        selfieImg,
                        locationAddress
                )
                viewModel.markAttendance(it.attendance!!, gigId)
            }

        } ?: run {
            FirebaseCrashlytics.getInstance().log("Gig not found : GigAttendance Page Fragment")
            FirebaseCrashlytics.getInstance().setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startNavigationSliderBtn?.resetSlider()
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_UPLOAD_SELFIE_IMAGE) {
            if (data != null)
                selfieImg = data.getStringExtra("image_name")
            checkAndUpdateAttendance()
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_UPLOAD_SELFIE_IMAGE && resultCode == Activity.RESULT_OK) {
//            selfieImg = data?.getStringExtra("filename")
//            var profilePicRef: StorageReference =
//                FirebaseStorage.getInstance().reference.child("attendance").child(imageName.toString())
//            selfieImg = profilePicRef.toString()
//            updateAttendanceToDB()
//            showToast(profilePicRef.toString())
//        }
//    }

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
                } else {
                    userGpsDialogActionCount = 1
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item ?: return false

        return when (item.itemId) {
            R.id.action_help -> {
                navigate(R.id.contactScreenFragment)
                true
            }
            R.id.action_share -> {
                true
            }
            R.id.action_decline_gig ->{
                if(gig == null)
                    return true

                if(gig!!.startDateTime!!.toLocalDateTime() < LocalDateTime.now()){
                    //Past or ongoing gig

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Cannot decline past or ongoing gig")
                        .setPositiveButton(getString(R.string.okay_text)){_,_ -> }
                        .show()

                    return true
                }

                if(gig != null ) {
                    declineGigDialog()
                }
                true
            }
            else -> false
        }
    }

    private fun declineGigDialog() {
        DeclineGigDialogFragment.launch(gigId, childFragmentManager, this)
    }

    override fun gigDeclined() {
        activity?.onBackPressed()
    }
}