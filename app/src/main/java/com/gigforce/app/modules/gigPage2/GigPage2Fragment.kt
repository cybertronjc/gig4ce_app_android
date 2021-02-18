package com.gigforce.app.modules.gigPage2

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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.*
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.gigPage2.models.OtherOption
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.core.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_2.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_address.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_completiton_payment_details.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_gig_type.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_other_options.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_payment_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_timer_layout.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_toolbar.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class GigPage2Fragment : BaseFragment(), OtherOptionClickListener,
    PopupMenu.OnMenuItemClickListener, DeclineGigDialogFragmentResultListener {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_FINE_LOCATION = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_2, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if (::gigId.isLateinit.not()) {
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigPage2Fragment: No Gig id found")
        }
    }

    private fun initUi() {

        details_label.setOnClickListener {
            navigate(
                R.id.gigDetailsFragment, bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
                )
            )
        }

//        gig_page_top_bar.setOnClickListener {
//            val gig = viewModel.currentGig ?: return@setOnClickListener
//            val gigStartEndTime = gig.startDateTime!!.toLocalDateTime()
//
//            navigate(R.id.gigMonthlyAttendanceFragment , bundleOf(
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_MONTH to gigStartEndTime.monthValue,
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_YEAR to gigStartEndTime.year,
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_LOGO to gig.companyLogo,
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_NAME to gig.companyName,
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_ROLE to gig.title,
//                GigMonthlyAttendanceFragment.INTENT_EXTRA_RATING to gig.gigRating
//                ))
//        }

        gig_page_timer_layout.setOnClickListener {
            val gig = viewModel.currentGig ?: return@setOnClickListener

            navigate(
                R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
                    GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to gig.gigId
                )
            )
        }

        expand_iv.setOnClickListener {
            navigate(
                R.id.gigDetailsFragment, bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
                )
            )
        }

        gig_cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        gig_ellipses_iv.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_gig_attendance, popupMenu.menu)

            viewModel.currentGig?.let {

                if (it.isPresentGig() || it.isPastGig()) {
                    popupMenu.menu.findItem(R.id.action_decline_gig).setVisible(false)
                } else {
                    popupMenu.menu.findItem(R.id.action_decline_gig).setVisible(true)
                }
            }

            popupMenu.setOnMenuItemClickListener(this@GigPage2Fragment)
            popupMenu.show()
        }

        requestPermissionForGPS()
        listener()
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

    private fun listener() {
        checkInCheckOutSliderBtn?.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    var manager =
                        activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    var statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (userGpsDialogActionCount == 0 && !statusOfGPS) {
                        showEnableGPSDialog()
                        checkInCheckOutSliderBtn?.resetSlider()
                        return;
                    }

                    if (userGpsDialogActionCount == 1 || ContextCompat.checkSelfPermission(
                            requireActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        var intent = Intent(context, ImageCaptureActivity::class.java)
                        startActivityForResult(
                            intent,
                            GigAttendancePageFragment.REQUEST_CODE_UPLOAD_SELFIE_IMAGE
                        )
                    } else {
                        requestPermissionForGPS()
                        checkInCheckOutSliderBtn?.resetSlider()
                    }
                }
            }

    }

    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(
            context?.getContentResolver(),
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.setData(Uri.parse("3"))
            context?.let { it -> LocalBroadcastManager.getInstance(it).sendBroadcast(poke) }

        }
    }

    var userGpsDialogActionCount = 0
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


    private fun initViewModel() {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> showGigDetailsAsLoading()
                    is Lce.Content -> setGigDetailsOnView(it.content)
                    is Lce.Error -> showErrorWhileLoadingGigData(it.error)
                }
            })

        viewModel.watchGig(gigId)
    }

    private fun showErrorWhileLoadingGigData(error: String) {

    }

    private fun showGigDetailsAsLoading() {

    }

    private fun setGigDetailsOnView(gig: Gig) {

        showCommonDetails(gig)

        if (gig.isPastGig())
            showPastGigDetails(gig)
        else if (gig.isPresentGig())
            showPresentGigDetails(gig)
        else if (gig.isUpcomingGig())
            showFutureGigDetails(gig)
        else {
//            FirebaseCrashlytics.getInstance().apply {
//                log("Gig did not qualify for any criteria")
//            }
        }
    }

    private fun showCommonDetails(gig: Gig) {

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(gig.companyLogo)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(gig.companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
                            .into(company_logo_iv)
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

            company_logo_iv.setImageDrawable(drawable)
        }

        gig_title_tv.text = gig.title
        gig_company_name_tv.text = "@ ${gig.companyName}"
        company_rating_tv.text = if (gig.gigRating == 0.0f) "--" else gig.gigRating.toString()

        gig_type.text = if (gig.isMonthlyGig) ": Monthly" else ": Daily"

        if (gig.endDateTime != null) {
            val startDate = gig.startDateTime!!.toLocalDate()
            val endDate = gig.endDateTime!!.toLocalDate()

            if (startDate.isEqual(endDate))
                gig_duration.text = ": ${dateFormatter.format(gig.startDateTime!!.toDate())}"
            else
                gig_duration.text = ": ${dateFormatter.format(gig.startDateTime!!.toDate())} - ${
                    dateFormatter.format(
                        gig.endDateTime!!.toDate()
                    )
                }"
        } else {
            gig_duration.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
        }

        image_view.isVisible = gig.latitude != null && gig.latitude != 0.0
        gig_address_tv.text = gig.address

        //TODO show people to expect when availabel
        people_to_expect_layout.gone()
        divider_below_people_to_expect.gone()

        if (gig.isMonthlyGig) {
            gross_wage_label.text = "Gross payment : "
            gross_wage.text =
                if (gig.gigAmount == 0.0) "As per contract" else "Rs ${gig.gigAmount}/mo"
        } else {
            gross_wage_label.text = "Gross wage per hour : "
            gross_wage.text =
                if (gig.gigAmount == 0.0) "As per contract" else "Rs ${gig.gigAmount}/hr"
        }

        showOtherOptions()
    }

    private fun showOtherOptions() {

        val optionList = listOf(DRESS_CODE, REIMBURSMENT, ID_CARD, HELP)

        other_options_recycler_view.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val adapter = OtherOptionsAdapter(
            requireContext(),
            optionList
        ).apply {
            setListener(this@GigPage2Fragment)
        }
        other_options_recycler_view.adapter = adapter
    }

    private fun showPastGigDetails(gig: Gig) {

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(gig.companyLogo)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(gig.companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
                            .into(company_logo_iv)
                    }
            }
        } else {
            val companyInitials = if (gig.companyName.isNullOrBlank())
                "C"
            else
                gig.companyName!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().beginConfig().textColor(R.color.colorPrimary).endConfig().buildRound(
                companyInitials,
                ResourcesCompat.getColor(resources, R.color.white, null)
            )

            company_logo_iv.setImageDrawable(drawable)
        }

        checkInCheckOutSliderBtn?.gone()
        gig_checkin_time_tv.gone()
        gig_page_completiton_layout.visible()

        if (gig.isCheckInAndCheckOutMarked()) {
            gig_page_completiton_layout.visible()
            amount_tv.text = "${gig.gigAmount} Rs"
        } else {
            gig_page_completiton_layout.gone()
        }

        gig_page_top_bar.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_pink,
                null
            )
        )
        gig_page_timer_layout.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_pink_light,
                null
            )
        )

        gig_date_tv.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
        gig_status_tv.text = "Completed"
        gig_status_iv.setImageResource(R.drawable.round_green)

        if (gig.isCheckInAndCheckOutMarked()) {
            val checkInTime = gig.attendance!!.checkInTime!!
            val checkOutTime = gig.attendance!!.checkOutTime!!

            val diffInMillisec: Long = checkOutTime.time - checkInTime.time
            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins"
            val checkoutTime = gig.attendance!!.checkOutTime
            gig_checkin_time_tv.text = "${timeFormatter.format(checkInTime)} - ${
                timeFormatter.format(checkoutTime)
            }"
        } else if (gig.isCheckInMarked()) {
            val gigStartDateTime = gig.startDateTime!!.toDate()
            val currentTime = Date().time

            val diffInMillisec: Long = currentTime - gigStartDateTime.time
            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "No check-out marked"
            gig_checkin_time_tv.text = "${timeFormatter.format(gigStartDateTime)} -"
        } else {
            gig_timer_tv.text = "No Check-in"
            gig_checkin_time_tv.gone()
        }
    }

    private fun showPresentGigDetails(gig: Gig) {
        gig_page_completiton_layout.gone()

        if (gig.isCheckInAndCheckOutMarked()) {
            //Attendance have been marked show it
            checkInCheckOutSliderBtn?.gone()
        } else {
            //Show Check In Controls
            checkInCheckOutSliderBtn?.visible()

            if (!gig.isCheckInMarked()) {
                checkInCheckOutSliderBtn?.let {
                    if (it.isCompleted()) {
                        it.resetSlider()
                    }
                }
                checkInCheckOutSliderBtn?.text = "Check-in"
            } else if (!gig.isCheckOutMarked()) {
                checkInCheckOutSliderBtn?.let {
                    if (it.isCompleted()) {
                        it.resetSlider()
                    }
                }

                checkInCheckOutSliderBtn?.text = "Check-out"
            }
        }


        gig_page_top_bar.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_green,
                null
            )
        )
        gig_page_timer_layout.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_green_light,
                null
            )
        )

        gig_date_tv.text = "Today, ${dateFormatter.format(gig.startDateTime!!.toDate())}"
        gig_status_tv.text = "Ongoing"
        gig_status_iv.setImageResource(R.drawable.round_green)

        if (gig.isCheckInMarked()) {
            val gigStartDateTime = gig.attendance?.checkInTime!!
            val currentTime = Date().time

            val diffInMillisec: Long = currentTime - gigStartDateTime.time
            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins"
            gig_checkin_time_tv.text = "Since ${timeFormatter.format(gigStartDateTime)}, Today"
        } else {
            gig_timer_tv.text = "No Checkin yet"
            gig_checkin_time_tv.gone()
        }
    }

    private fun showFutureGigDetails(gig: Gig) {
        checkInCheckOutSliderBtn?.gone()
        gig_page_completiton_layout.gone()
        gig_checkin_time_tv.gone()

        gig_status_iv.setImageResource(R.drawable.round_yellow)
        gig_status_tv.text = "Pending"

        gig_page_top_bar.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_orange,
                null
            )
        )
        gig_page_timer_layout.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gig_orange_light,
                null
            )
        )

        gig_date_tv.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
        val gigStartDateTime = gig.startDateTime!!.toDate()
        val currentTime = Date().time

        val diffInMillisec: Long = gigStartDateTime.time - currentTime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)

        if(diffInHours > 24){
            val days = diffInHours / 24
            val hours = diffInHours % 24

            gig_timer_tv.text = "$days Days : $hours Hrs"
        } else {
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins"
            gig_checkin_time_tv.text = "Since ${timeFormatter.format(gigStartDateTime)}, Today"
        }
    }

    override fun onOptionClicked(option: OtherOption) {

        when (option.id) {
            ID_HELP -> {
                navigate(R.id.fakeGigContactScreenFragment)
            }
            ID_IDENTITY_CARD -> {
                navigate(R.id.giger_id_fragment, Bundle().apply {
                    this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, viewModel.currentGig?.gigId)
                })
            }
            else -> {
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
                showToast("This feature is under development")
                true
            }
            R.id.action_decline_gig -> {
                if (viewModel.currentGig == null)
                    return true

                if (viewModel.currentGig!!.startDateTime!!.toLocalDateTime() < LocalDateTime.now()) {
                    //Past or ongoing gig

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Cannot decline past or ongoing gig")
                        .setPositiveButton(getString(R.string.okay_text)) { _, _ -> }
                        .show()

                    return true
                }

                if (viewModel.currentGig != null) {
                    declineGigDialog()
                }
                true
            }
            else -> false
        }
    }

    private fun declineGigDialog() {
        DeclineGigDialogFragment.launch(gigId, childFragmentManager, this@GigPage2Fragment)
    }

    override fun gigDeclined() {

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
                } else {
                    userGpsDialogActionCount = 1
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }

    private fun checkAndUpdateAttendance() {
        val gig = viewModel.currentGig ?: return

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



        viewModel.currentGig?.let {

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
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkInCheckOutSliderBtn?.resetSlider()
        if (resultCode == Activity.RESULT_OK && requestCode == GigAttendancePageFragment.REQUEST_CODE_UPLOAD_SELFIE_IMAGE) {
            if (data != null)
                selfieImg = data.getStringExtra("image_name")
            checkAndUpdateAttendance()
        }
    }

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"
        const val TEXT_VIEW_ON_MAP = "(View On Map)"

        const val PERMISSION_FINE_LOCATION = 100
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333

        private const val ID_DRESS_CODE = "apodZsdEbx"
        private const val ID_REIMBURSMENT = "TnovE9tzXl"
        private const val ID_IDENTITY_CARD = "knnp4f4ZUi"
        private const val ID_HELP = "NXyRQLeIol"

        private val DRESS_CODE = OtherOption(
            id = ID_DRESS_CODE,
            name = "Dress code",
            icon = R.drawable.ic_clothes
        )

        private val REIMBURSMENT = OtherOption(
            id = ID_REIMBURSMENT,
            name = "Reimbursment",
            icon = R.drawable.ic_compensation
        )

        private val ID_CARD = OtherOption(
            id = ID_IDENTITY_CARD,
            name = "Idenitity card",
            icon = R.drawable.ic_id_card
        )

        private val HELP = OtherOption(
            id = ID_HELP,
            name = "Help",
            icon = R.drawable.ic_question
        )
    }
}