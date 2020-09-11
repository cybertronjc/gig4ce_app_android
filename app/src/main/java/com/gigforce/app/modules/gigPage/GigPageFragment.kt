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
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import kotlinx.android.synthetic.main.fragment_gig_page_present.addressTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.callCardView
import kotlinx.android.synthetic.main.fragment_gig_page_present.companyLogoIV
import kotlinx.android.synthetic.main.fragment_gig_page_present.companyNameTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.contactPersonTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.durationTextTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.favoriteCB
import kotlinx.android.synthetic.main.fragment_gig_page_present.gigIdTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.gigTypeTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.messageCardView
import kotlinx.android.synthetic.main.fragment_gig_page_present.roleNameTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.shiftTV
import kotlinx.android.synthetic.main.fragment_gig_page_present.wageIV
import kotlinx.android.synthetic.main.fragment_gig_page_present.wageTV
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit


class GigPageFragment : BaseFragment(), View.OnClickListener {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"
        const val TEXT_VIEW_ON_MAP = "(View On Map)"

        const val PERMISSION_FINE_LOCATION = 100
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private lateinit var gigId: String
    private var gig: Gig? = null
    private var comingFromCheckInScreen = false
    var selfieImg: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_present, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initUi()
        initViewModel(view)
        initClicks()

    }

    private fun initClicks() {
        bt_download_id_gig_page.setOnClickListener(this)
        bt_download_id_gig_past_gigs.setOnClickListener(this)
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {
        savedInstanceState ?. let{
            gigId = it.getString(INTENT_EXTRA_GIG_ID)!!
            comingFromCheckInScreen = it.getBoolean(INTENT_EXTRA_COMING_FROM_CHECK_IN)
        } ?: run{
            arguments?.let {
                gigId = it.getString(INTENT_EXTRA_GIG_ID)!!
                comingFromCheckInScreen = it.getBoolean(INTENT_EXTRA_COMING_FROM_CHECK_IN)
            }?: run {
                FirebaseCrashlytics.getInstance().log("GigPageFragment getData method : savedInstanceState and arguments found null")
                FirebaseCrashlytics.getInstance().setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            }
        }
    }
    var userGpsDialogActionCount = 0
    private fun initUi() {
//        gigLocationMapView.getMapAsync {
//            mGoogleMap = it
//            gigLocationMapView.onCreate(null)
//
//            try {
//                MapsInitializer.initialize(requireContext())
//            } catch (e: GooglePlayServicesNotAvailableException) {
//                e.printStackTrace()
//            }
//        }

        toolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        contactUsLayout?.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }


        provide_feedback?.setOnClickListener {
            RateGigDialogFragment.launch(gigId, childFragmentManager)
        }

        contactUsBtn?.setOnClickListener {

        }

        callCardView?.setOnClickListener {

            gig?.gigContactDetails?.contactNumber?.let {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
                startActivity(intent)
            }
        }

        messageCardView?.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        favoriteCB?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && gig?.isFavourite!!.not()) {
                viewModel.favoriteGig(gigId)
                showToast("Marked As Favourite")
            } else if (!isChecked && gig?.isFavourite!!) {
                viewModel.unFavoriteGig(gigId)
                showToast("Unmarked As Favourite")
            }
        }

        checkInCheckOutSliderBtn?.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val is_gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if(userGpsDialogActionCount==0 && !is_gps_enabled){
                        showEnableGPSDialog()
                        checkInCheckOutSliderBtn ?.resetSlider()
                        return;
                    }

                    val has_permission_coarse_location = ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (userGpsDialogActionCount == 1 || has_permission_coarse_location) {
                        val intent = Intent(context, ImageCaptureActivity::class.java)
                        startActivityForResult(intent,
                            GigAttendancePageFragment.REQUEST_CODE_UPLOAD_SELFIE_IMAGE
                        )
                    } else {
                        requestPermissionForGPS()
                        checkInCheckOutSliderBtn?.resetSlider()
                    }
                }
            }

        gigHighlightsSeeMoreTV.setOnClickListener {

            if(gig == null)
                return@setOnClickListener

            if (gigHighlightsContainer.childCount == 4) {
                //Collapsed
                inflateGigHighlights(gig!!.gigHighlights.subList(4, gig!!.gigHighlights.size))
                gigHighlightsSeeMoreTV.text = getString(R.string.plus_see_less)
            }else{
                //Expanded
                gigHighlightsContainer.removeViews(4, gigHighlightsContainer.childCount - 4)
                gigHighlightsSeeMoreTV.text = getString(R.string.plus_see_more)
            }
        }

        gigRequirementsSeeMoreTV.setOnClickListener {

            if(gig == null)
                return@setOnClickListener

            if (gigRequirementsContainer.childCount == 4) {
                //Collapsed
                inflateGigRequirements(gig!!.gigRequirements.subList(4, gig!!.gigRequirements.size))
                gigRequirementsSeeMoreTV.text = getString(R.string.plus_see_less)
            }else{
                //Expanded
                gigRequirementsContainer.removeViews(4, gigRequirementsContainer.childCount - 4)
                gigRequirementsSeeMoreTV.text = getString(R.string.plus_see_more)
            }
        }
    }

    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(context?.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (!provider.contains("gps"))
        { //if gps is disabled
            val poke = Intent()
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            context ?. let {
                    it-> LocalBroadcastManager.getInstance(it).sendBroadcast(poke)
            } ?: run {

                FirebaseCrashlytics.getInstance().log("Context found null in GigPageFragment/turnGPSOn()")
            }
        }
    }
    private fun showEnableGPSDialog() {
        showConfirmationDialogType2("Please enable your GPS!!\n                                                               ",
            object : ConfirmationDialogOnClickListener {
                override fun clickedOnYes(dialog: Dialog?) {
                    if(canToggleGPS()) turnGPSOn()
                    else { showToast("Please Enable your GPS manually in setting!!") }
                    dialog?.dismiss()
                }

                override fun clickedOnNo(dialog: Dialog?) {
                    popFragmentFromStack(R.id.earningFragment)
                    userGpsDialogActionCount = 1
                    dialog?.dismiss()
                }

            })
    }


    private fun canToggleGPS():Boolean {
        val pacman = context?.getPackageManager()
        var pacInfo: PackageInfo? = null
        try
        {
            pacInfo = pacman?.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS)
        }
        catch (e: PackageManager.NameNotFoundException) {
            return false //package not found
        }
        catch (e:Exception){

        }
        if (pacInfo != null)
        {
            for (actInfo in pacInfo.receivers)
            {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported)
                {
                    return true
                }
            }
        }
        return false //default
    }

    private fun initViewModel(view: View) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> {
                        setGigDetailsOnView(it.content, view)

                    }
                    is Lce.Error -> {
                    }
                }
            })

        viewModel.watchGig(gigId)
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

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkInCheckOutSliderBtn?.resetSlider()
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_UPLOAD_SELFIE_IMAGE) {
            if (data != null)
                selfieImg = data.getStringExtra("image_name")
            checkAndUpdateAttendance()
        } else {
            showToast("Error in uploading - Try again")
        }
    }

    private fun initializeGPS() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun checkAndUpdateAttendance() {
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
                updateAttendanceOnDBCall(it)
            }
        }
        else if(userGpsDialogActionCount==0){
            requestPermissionForGPS()
        }
        else {
            if (gig!!.attendance == null || !gig!!.attendance!!.checkInMarked) {
                val markAttendance =
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

    fun updateAttendanceOnDBCall(location: Location?) {
        /*
                A?.B?.C?.D   return null if anything in between is null

                A!!.B  throw error if A is null

                location ?.latitude  ... return null or the value
                location ?.latitude ?: 0.0    return 0.0 if null or value
         */

        val latitude : Double = location ?.latitude ?: 0.0
        val longitude : Double = location ?.longitude ?: 0.0

        var locationAddress = ""
        try {
            val geocoder = Geocoder(requireContext())
            val addressArr = geocoder.getFromLocation(latitude, longitude, 1)
            locationAddress = addressArr?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {

        }

        gig ?. let{

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
            }else{
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

    private fun addMarkerOnMap(
        latitude: Double,
        longitude: Double
    ) = mGoogleMap?.let {

        it.clear()

        // create marker
        val marker = MarkerOptions()
        marker.position(LatLng(latitude, longitude))
            .title(getString(R.string.gig_location))

        // adding marker
        it.addMarker(marker)
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(15f)
            .build()
        it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun setGigDetailsOnView(gig: Gig, view: View) {
        this.gig = gig

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(gig.companyLogo)
                    .placeholder(getCircularProgressDrawable())
                    .into(companyLogoIV)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(gig.companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
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

        if (!gig.bannerImage.isNullOrBlank()) {
            if (gig.bannerImage!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(gig.bannerImage)
                    .placeholder(getCircularProgressDrawable())
                    .into(gigBannerImageIV)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("gig_images")
                    .child(gig.bannerImage!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
                            .into(gigBannerImageIV)
                    }
            }
        }


        toolbar.title = gig.title
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = "Gig Id : ${gig.gigId}"
        paymentAmountTV.text = if(gig.gigAmount != 0.0) "Rs. ${gig.gigAmount}" else "N/A"
        contactPersonTV.text = gig.gigContactDetails?.contactName
        callCardView.isVisible = gig.gigContactDetails?.contactNumber != 0L

        if (gig.isFavourite && favoriteCB.isChecked.not()) {
            favoriteCB.isChecked = true
        } else if (gig.isFavourite.not() && favoriteCB.isChecked) {
            favoriteCB.isChecked = false
        }

        if (gig.endDateTime != null) {
            val startDate = gig.startDateTime!!.toLocalDate()
            val endDate = gig.endDateTime!!.toLocalDate()

            if(startDate.isEqual(endDate))
                durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
            else
                durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"

            shiftTV.text = "${timeFormatter.format(gig.startDateTime!!.toDate())} - ${timeFormatter.format(gig.endDateTime!!.toDate())}"
        } else {
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
            shiftTV.text = "${timeFormatter.format(gig.startDateTime!!.toDate())} - "
        }

         if (gig.gigAmount == 0.0)
           {
               wageTV.text = "Payout : As per contract"
           }
        else {
             wageTV.text = if (gig.isMonthlyGig)
                "Payout : Rs ${gig.gigAmount} per Month"
            else
                "Payout : Rs ${gig.gigAmount} per Hour"
        }

        gigHighlightsContainer.removeAllViews()
        if(gig.gigHighlights.size > 4){
            inflateGigHighlights(gig.gigHighlights.take(4))

            gigHighlightsContainer.removeViews(4, gigHighlightsContainer.childCount - 4)
            gigHighlightsSeeMoreTV.visible()
        }else{
            inflateGigHighlights(gig.gigHighlights)
            gigHighlightsSeeMoreTV.gone()
        }

        gigRequirementsContainer.removeAllViews()
        if(gig.gigRequirements.size > 4) {
            inflateGigRequirements(gig.gigRequirements.take(4))
            gigRequirementsSeeMoreTV.visible()
        }else{
            inflateGigRequirements(gig.gigRequirements)
            gigRequirementsSeeMoreTV.gone()
        }

        addressTV.setOnClickListener {

            //Launch Map
            val lat = this.gig?.latitude
            val long = this.gig?.longitude

            if (lat != null && long != null) {

                val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        fullMapAddresTV.setOnClickListener {

            //Launch Map
            val lat = this.gig?.latitude
            val long = this.gig?.longitude

            if (lat != null && long != null) {

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

        if (gig.address.isNotBlank()) {
            if (gig.latitude != null)
                fullMapAddresTV.text = prepareAddress(gig.address)
            else
                fullMapAddresTV.text = gig.address
        }

//        if (gig.latitude != null) {
 //           gigLocationMapView.visible()
//            addMarkerOnMap(
//                latitude = gig.latitude!!,
//                longitude = gig.longitude!!
//            )
//        } else {
//            gigLocationMapView.gone()
//        }

        gigLocationMapView.gone()

        if (gig.locationPictures.isNotEmpty()) {
            //Inflate Pics
            locationImageScrollView.visibility = View.VISIBLE
            locationImageContainer.removeAllViews()
            inflateLocationPics(gig.locationPictures)
        } else {
            locationImageScrollView.visibility = View.GONE
        }

        if (!gig.isGigActivated) {
            showNotActivatedGigDetails(gig)
        } else if (gig.isPresentGig()) {
            showPresentGigDetails(gig)
        } else if (gig.isPastGig()) {
            showPastgigDetails(gig)
        } else if (gig.isUpcomingGig()) {
            showUpcomingGigDetails(gig)
        } else {
            showPastgigDetails(gig)
        }

    }

    private fun showPresentGigDetails(gig: Gig) {
        completedGigControlsLayout.gone()
        presentGigAttendanceCardView.visible()
        presentOrFutureGigControls.visible()
        hideFeedbackOption()

        if (gig.isCheckInAndCheckOutMarked()) {
            //Attendance have been marked show it
            checkInCheckOutSliderBtn?.gone()
            dateTV.text = DateHelper.getDateInDDMMYYYY(gig.startDateTime!!.toDate())

            if (gig.isCheckInMarked())
                presentGigpunchInTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkInTime!!)
            else
                presentGigpunchInTimeTV.text = "--:--"

            if (gig.isCheckOutMarked())
                presentGigpunchOutTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkOutTime!!)
            else
                presentGigpunchOutTimeTV.text = "--:--"
        } else {
            //Show Check In Controls
            checkInCheckOutSliderBtn?.visible()
            presentFutureGigNoteTV.text =
                "Please contact the supervisor in case there’s an issue with marking attendance."

            if (gig.isCheckInMarked())
                presentGigpunchInTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkInTime!!)

            if (gig.isCheckOutMarked())
                presentGigpunchOutTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkOutTime!!)

            if (!gig.isCheckInMarked()) {
                checkInCheckOutSliderBtn?.text = "Check-in"
            } else if (!gig.isCheckOutMarked()) {
                checkInCheckOutSliderBtn?.text = "Check-out"
            }
        }
    }

    private fun showPastgigDetails(gig: Gig) {
        checkInCheckOutSliderBtn?.gone()
        presentOrFutureGigControls.gone()
//        showFeedBackOption()
        hideFeedbackOption()
        completedGigControlsLayout.visible()
        bt_download_id_gig_past_gigs.visible()

        gigRatingLayout.visible()

        showUserReceivedRating(gig)
        showUserFeedbackRating(gig)

        if (gig.isCheckInAndCheckOutMarked()) {
            gigPaymentLayout.visible()
            invoiceStatusBtn.visible()

            if(gig.gigAmount == 0.0){
                paymentAmountTV.gone()
                payment_per_contract_label.visible()
            }else{
                paymentAmountTV.visible()
                payment_per_contract_label.gone()
            }

            processingLabel.text = gig.paymentStatus

            if (gig.invoiceGenerationDate != null) {
                val invoiceGenDate = gig.invoiceGenerationDate!!.toLocalDate()

                val invoiceDateTime = if (invoiceGenDate.equals(LocalDate.now())) {
                    timeFormatter.format(gig.invoiceGenerationDate!!.toDate())
                } else {
                    dateFormatter.format(gig.invoiceGenerationDate!!.toDate())
                }

                invoiceStatusBtn.text = "Invoice Generated"
                invoice_generation_date.text = "Invoice Generated : $invoiceDateTime"
            } else {
                invoice_generation_date.text = "Invoice Generated : --"
                invoiceStatusBtn.text = "Invoice Pending"
            }

        } else {
            gigPaymentLayout.gone()
            invoiceStatusBtn.gone()
        }

        pastGigNoteTV.text =
            "Please contact the supervisor in case there’s an issue with marking attendance."
        dateTV.text = DateHelper.getDateInDDMMYYYY(gig.startDateTime!!.toDate())

        if (gig.isCheckInMarked())
            pastGigpunchInTimeTV.text =
                timeFormatter.format(gig.attendance!!.checkInTime!!)
        else
            pastGigpunchInTimeTV.text = "--:--"

        if (gig.isCheckOutMarked())
            pastGigpunchOutTimeTV.text =
                timeFormatter.format(gig.attendance!!.checkOutTime!!)
        else
            pastGigpunchOutTimeTV.text = "--:--"
    }

    private fun showUpcomingGigDetails(gig: Gig) {
        checkInCheckOutSliderBtn?.gone()
        completedGigControlsLayout.gone()
        presentGigAttendanceCardView.gone()
        hideFeedbackOption()
        presentOrFutureGigControls.visible()

        val timeLeft = gig.startDateTime!!.toDate().time - Date().time
        val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeft)
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeft)

        presentFutureGigNoteTV.text =
            if (daysLeft > 0)
                "Your gig will start in next $daysLeft Days"
            else
                "Your gig will start in next $hoursLeft Hours"
    }

    private fun showUserReceivedRating(gig: Gig) {

        if (gig.ratingUserReceived > 0) {
            userReceviedFeedbackRatingLayout.visible()

            userReceivedRatingBar.rating = gig.ratingUserReceived
            if (gig.feedbackUserReceived != null) {
                userReceivedRatingFeedbackTV.visible()
                userReceivedRatingFeedbackTV.text = "“ ${gig.feedbackUserReceived} “"
            } else
                userReceivedRatingFeedbackTV.gone()

            userReceivedRatingAttachmentsContainer.removeAllViews()
            if (gig.ratingUserReceivedAttachments.isNotEmpty()) {

                userReceivedRatingAttachmentsContainer.visible()
                gig.ratingUserReceivedAttachments.map {
                    Uri.parse(it)
                }.forEach {
                    layoutInflater.inflate(
                        R.layout.dialog_rating_image_layout,
                        userReceivedRatingAttachmentsContainer,
                        true
                    )
                    val inflatedImageView = userReceivedRatingAttachmentsContainer.getChildAt(
                        userReceivedRatingAttachmentsContainer.childCount - 1
                    )

                    inflatedImageView.setOnClickListener(onClickImageListener)
                    inflatedImageView.findViewById<View>(R.id.ic_delete_btn)
                        .setOnClickListener(onDeleteUserReceivedFeedbackClickImageListener)

                    inflatedImageView.tag = it.toString()

                    val imageNameTV = inflatedImageView.findViewById<TextView>(R.id.imageNameTV)
                    imageNameTV.text = if (it.lastPathSegment!!.contains("/")) {
                        it.lastPathSegment!!.substringAfterLast("/")
                    } else
                        it.lastPathSegment!!
                }
            } else {
                userReceivedRatingAttachmentsContainer.gone()
            }
            userReceivedRateTv.text = getString(R.string.rating_you_received)


        } else {
            userReceviedFeedbackRatingLayout.visible()
            userReceivedRateTv.text = getString(R.string.pending_rating_from_client)
        }
    }

    private fun showUserFeedbackRating(gig: Gig) {

        if (gig.gigRating > 0) {
            userFeedbackRatingLayout.visible()
            whatsYourRateTv.text =getString(R.string.you_have_rated_this_gig_as)


            userFeedbackRatingBar.rating = gig.gigRating
            if (gig.gigUserFeedback != null) {
                usersFeedbackTV.visible()
                usersFeedbackTV.text = "“ ${gig.gigUserFeedback} “"
            } else
                usersFeedbackTV.gone()

            userFeedbackAttachmentsContainer.removeAllViews()
            if (gig.gigUserFeedbackAttachments.isNotEmpty()) {
                userFeedbackAttachmentsContainer.visible()

                gig.gigUserFeedbackAttachments.map {
                    Uri.parse(it)
                }.forEach {
                    layoutInflater.inflate(
                        R.layout.dialog_rating_image_layout,
                        userFeedbackAttachmentsContainer,
                        true
                    )
                    val inflatedImageView = userFeedbackAttachmentsContainer.getChildAt(
                        userFeedbackAttachmentsContainer.childCount - 1
                    )

                    inflatedImageView.setOnClickListener(onClickImageListener)
                    inflatedImageView.findViewById<View>(R.id.ic_delete_btn)
                        .setOnClickListener(onDeleteUserFeedbackClickImageListener)

                    inflatedImageView.tag = it.toString()

                    val imageNameTV = inflatedImageView.findViewById<TextView>(R.id.imageNameTV)
                    imageNameTV.text = if (it.lastPathSegment!!.contains("/")) {
                        it.lastPathSegment!!.substringAfterLast("/")
                    } else
                        it.lastPathSegment!!
                }
            } else {
                userFeedbackAttachmentsContainer.gone()
            }
        } else {
            userFeedbackRatingLayout.visible()
            userFeedbackRatingBar.visible()
            whatsYourRateTv.text = getString(R.string.provide_feedback)
            userFeedbackRatingLayout.setOnClickListener {
                RateGigDialogFragment.launch(gigId, childFragmentManager)

            }
        }
    }


    private fun showNotActivatedGigDetails(gig: Gig) {


    }

    private val onClickImageListener = View.OnClickListener { imageView ->
        //TAG INFO - Parent Container Layout have Fixed
        val uriString = imageView.tag.toString()
        val uri = Uri.parse(uriString)
        ViewFullScreenImageDialogFragment.showImage(childFragmentManager, uri)
    }

    private val onDeleteUserFeedbackClickImageListener = View.OnClickListener { deleteImageView ->
        //TAG INFO - Parent Container Layout have Fixed

        val parentLinearLayout: View = deleteImageView.parent as View
        val imageNameTV: TextView = parentLinearLayout.findViewById(R.id.imageNameTV)

        val attachmentToDeleteName = imageNameTV.text.toString()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Remove Attachment : $attachmentToDeleteName ?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteUserFeedbackAttachment(gigId, attachmentToDeleteName)
            }
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private val onDeleteUserReceivedFeedbackClickImageListener =
        View.OnClickListener { deleteImageView ->
            //TAG INFO - Parent Container Layout have Fixed
            val parentLinearLayout: View = deleteImageView.parent as View
            val imageNameTV: TextView = parentLinearLayout.findViewById(R.id.imageNameTV)

            val attachmentToDeleteName = imageNameTV.text.toString()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Remove Attachment : $attachmentToDeleteName ?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteUserReceivedFeedbackAttachment(gigId, attachmentToDeleteName)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }

    private fun inflateLocationPics(locationPictures: List<String>) = locationPictures.forEach {
        locationImageContainer.inflate(R.layout.layout_gig_location_picture_item, true)
        val gigItem: View =
            locationImageContainer.getChildAt(locationImageContainer.childCount - 1) as View

        gigItem.setOnClickListener(locationImageItemClickListener)

        val locationImageView: ImageView = gigItem.findViewById(R.id.imageView)
        if (it.startsWith("http", true)) {
            gigItem.tag = it

            GlideApp.with(requireContext())
                .load(it)
                .placeholder(getCircularProgressDrawable())
                .into(locationImageView)
        } else {
            FirebaseStorage.getInstance()
                .getReference("companies_gigs_images")
                .child(it)
                .downloadUrl
                .addOnSuccessListener { fileUri ->

                    GlideApp.with(requireContext())
                        .load(fileUri)
                        .placeholder(getCircularProgressDrawable())
                        .into(locationImageView)

                    (locationImageView.parent as View).tag = fileUri.toString()
                }
        }
    }

    private val locationImageItemClickListener = View.OnClickListener { view ->
        val imageUri = view.tag?.toString()

        if (imageUri != null)
            ViewFullScreenImageDialogFragment.showImage(childFragmentManager, Uri.parse(imageUri))
    }

    private fun prepareAddress(address: String): SpannableString {
        if (address.isBlank())
            return SpannableString("")

        val string = SpannableString(address + TEXT_VIEW_ON_MAP)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), address.length + 1, string.length - 1, 0)

        return string
    }

    private fun inflateGigRequirements(gigRequirements: List<String>) = gigRequirements.forEach {

        if (it.contains(":")) {
            gigRequirementsContainer.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gigRequirementsContainer.getChildAt(gigRequirementsContainer.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gigRequirementsContainer.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gigRequirementsContainer.getChildAt(gigRequirementsContainer.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    private fun inflateGigHighlights(gigHighLights: List<String>) = gigHighLights.forEach {

        if (it.contains(":")) {
            gigHighlightsContainer.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gigHighlightsContainer.getChildAt(gigHighlightsContainer.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gigHighlightsContainer.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gigHighlightsContainer.getChildAt(gigHighlightsContainer.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }

    }

    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }


    private fun hideFeedbackOption() {
        right_arrow2.gone()
        provide_fb_txt.gone()
        contact_icon.gone()
        provide_feedback.gone()
        textView127.gone()
    }

    private fun showFeedBackOption() {
        right_arrow2.visible()
        provide_fb_txt.visible()
        contact_icon.visible()
        provide_feedback.visible()
        textView127.visible()
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_download_id_gig_page -> {
                navigate(R.id.giger_id_fragment, Bundle().apply {
                    this.putString(INTENT_EXTRA_GIG_ID, gig?.gigId)
                })
            }
            R.id.bt_download_id_gig_past_gigs -> {
                navigate(R.id.giger_id_fragment, Bundle().apply {
                    this.putString(INTENT_EXTRA_GIG_ID, gig?.gigId)
                })
            }
        }
    }
}