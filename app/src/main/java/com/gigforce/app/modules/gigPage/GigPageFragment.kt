package com.gigforce.app.modules.gigPage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
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
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
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
        if (savedInstanceState != null) {
            gigId = savedInstanceState.getString(INTENT_EXTRA_GIG_ID)!!
            comingFromCheckInScreen =
                savedInstanceState.getBoolean(INTENT_EXTRA_COMING_FROM_CHECK_IN)
        } else {
            gigId = arguments?.getString(INTENT_EXTRA_GIG_ID)!!
            comingFromCheckInScreen = arguments.getBoolean(INTENT_EXTRA_COMING_FROM_CHECK_IN)
        }
    }

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

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        contactUsLayout.setOnClickListener {
            navigate(R.id.contactScreenFragment)
        }


        provide_feedback.setOnClickListener {
            RateGigDialogFragment.launch(gigId, childFragmentManager)
        }

        contactUsBtn.setOnClickListener {

        }

        callCardView.setOnClickListener {

            gig?.gigContactDetails?.contactNumber?.let {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
                startActivity(intent)
            }
        }

        messageCardView.setOnClickListener {
            navigate(R.id.contactScreenFragment)
        }

        favoriteCB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && gig?.isFavourite!!.not()) {
                viewModel.favoriteGig(gigId)
                favoriteCB.buttonTintList=resources.getColorStateList(R.color.lipstick)
                showToast("Marked As Favourite")
            } else if (!isChecked && gig?.isFavourite!!) {
                viewModel.unFavoriteGig(gigId)
                favoriteCB.buttonTintList=resources.getColorStateList(R.color.black_42)

                showToast("Unmarked As Favourite")
            }
        }

        checkInCheckOutSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        var intent = Intent(context, ImageCaptureActivity::class.java)
                        startActivityForResult(
                            intent,
                            REQUEST_CODE_UPLOAD_SELFIE_IMAGE
                        )

                    } else {
                        requestPermissionForGPS()
                        checkInCheckOutSliderBtn.resetSlider()
                    }
                }
            }
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
        checkInCheckOutSliderBtn.resetSlider()
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
        if (ActivityCompat.checkSelfPermission(
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


        } else {
            requestPermissionForGPS()
        }
    }

    fun updateAttendanceOnDBCall(location: Location) {
        var geocoder = Geocoder(requireContext())
        var locationAddress = ""
        try {
            var addressArr = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            locationAddress = addressArr.get(0).getAddressLine(0)
        } catch (e: java.lang.Exception) {
        }
        if (gig!!.attendance == null || !gig!!.attendance!!.checkInMarked) {
            var markAttendance =
                GigAttendance(
                    true,
                    Date(),
                    location.latitude,
                    location.longitude,
                    selfieImg,
                    locationAddress
                )
            viewModel.markAttendance(markAttendance, gigId)

        } else {
            gig!!.attendance!!.setCheckout(
                true, Date(), location.latitude,
                location.longitude, selfieImg,
                locationAddress
            )
            viewModel.markAttendance(gig!!.attendance!!, gigId)

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

        toolbar.title = gig.title
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = "Gig Id : ${gig.gigId}"
        paymentAmountTV.text = "Rs. ${gig.gigAmount}"
        contactPersonTV.text = gig.gigContactDetails?.contactName
        callCardView.isVisible = gig.gigContactDetails?.contactNumber != 0L

        if (gig.isFavourite && favoriteCB.isChecked.not()) {
            favoriteCB.isChecked = true
        } else if (gig.isFavourite.not() && favoriteCB.isChecked) {
            favoriteCB.isChecked = false
        }

        if (gig.endDateTime != null) {
            durationTextTV.text =
                "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"
            shiftTV.text =
                "${timeFormatter.format(gig.startDateTime!!.toDate())} - ${timeFormatter.format(gig.endDateTime!!.toDate())}"
        } else {
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
            shiftTV.text = "${timeFormatter.format(gig.startDateTime!!.toDate())} - "
        }

        val gigAmountText = if (gig.gigAmount == 0.0)
            "--"
        else {
            if (gig.isMonthlyGig)
                "Gross Payment : Rs ${gig.gigAmount} per Month"
            else
                "Gross Payment : Rs ${gig.gigAmount} per Hour"

        }
        wageTV.text = gigAmountText

        gigHighlightsContainer.removeAllViews()
        inflateGigHighlights(gig.gigHighlights)

        gigRequirementsContainer.removeAllViews()
        inflateGigRequirements(gig.gigRequirements)

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

        if (gig.latitude != null) {
            gigLocationMapView.visible()
            addMarkerOnMap(
                latitude = gig.latitude!!,
                longitude = gig.longitude!!
            )
        } else {
            gigLocationMapView.gone()
        }

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

        if (gig.isCheckInAndCheckOutMarked()) {
            //Attendance have been marked show it
            checkInCheckOutSliderBtn.gone()
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
            checkInCheckOutSliderBtn.visible()
            presentFutureGigNoteTV.text =
                "Please contact the supervisor in case there’s an issue with marking attendance."

            if (gig.isCheckInMarked())
                presentGigpunchInTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkInTime!!)

            if (gig.isCheckOutMarked())
                presentGigpunchOutTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkOutTime!!)

            if (!gig.isCheckInMarked()) {
                checkInCheckOutSliderBtn.text = "Check In"
            } else if (!gig.isCheckOutMarked()) {
                checkInCheckOutSliderBtn.text = "Check Out"
            }
        }
    }

    private fun showPastgigDetails(gig: Gig) {
        checkInCheckOutSliderBtn.gone()
        presentOrFutureGigControls.gone()


        completedGigControlsLayout.visible()
        bt_download_id_gig_past_gigs.visible()

        gigRatingLayout.visible()

        showUserReceivedRating(gig)
        showUserFeedbackRating(gig)

        if (gig.isCheckInAndCheckOutMarked()) {
            gigPaymentLayout.visible()
            invoiceStatusBtn.visible()

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
        checkInCheckOutSliderBtn.gone()
        completedGigControlsLayout.gone()
        presentGigAttendanceCardView.gone()
        presentOrFutureGigControls.visible()

        val timeLeft = gig.startDateTime!!.toDate().time - Date().time
        val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeft)
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeft)

        presentFutureGigNoteTV.text =
            if (daysLeft > 0)
                "We are preparing your gig.It will start in next $daysLeft Days"
            else
                "We are preparing your gig.It will start in next $hoursLeft Hours"
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
                    // inflatedImageView.findViewById<View>(R.id.ic_delete_btn).setOnClickListener(onDeleteImageClickImageListener)

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
        } else {
            userReceviedFeedbackRatingLayout.gone()
        }
    }

    private fun showUserFeedbackRating(gig: Gig) {

        if (gig.gigRating > 0) {
            userFeedbackRatingLayout.visible()

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
                    // inflatedImageView.findViewById<View>(R.id.ic_delete_btn).setOnClickListener(onDeleteImageClickImageListener)

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
            userFeedbackRatingLayout.gone()
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

    private fun inflateLocationPics(locationPictures: List<String>) = locationPictures.forEach {
        locationImageContainer.inflate(R.layout.layout_gig_location_picture_item, true)
        val gigItem: View =
            locationImageContainer.getChildAt(locationImageContainer.childCount - 1) as View

        gigItem.setOnClickListener(locationImageItemClickListener)

        val locationImageView: ImageView = gigItem.findViewById(R.id.imageView)
        if (it.startsWith("http", true)) {
            gigItem.tag = it

            Glide.with(requireContext())
                .load(it)
                .into(locationImageView)
        } else {
            FirebaseStorage.getInstance()
                .getReference("companies_gigs_images")
                .child(it)
                .downloadUrl
                .addOnSuccessListener { fileUri ->
                    Glide.with(requireContext())
                        .load(fileUri)
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

            gigTitleTV.text = title
            contentTV.text = content.replace("<>", "\n")
        } else {
            gigRequirementsContainer.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gigRequirementsContainer.getChildAt(gigRequirementsContainer.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = it
        }
    }


    private fun inflateGigHighlights(gigHighLights: List<String>) = gigHighLights.forEach {
        gigHighlightsContainer.inflate(R.layout.gig_details_item, true)
        val gigItem: LinearLayout =
            gigHighlightsContainer.getChildAt(gigHighlightsContainer.childCount - 1) as LinearLayout
        val gigTextTV: TextView = gigItem.findViewById(R.id.text)
        gigTextTV.text = it
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