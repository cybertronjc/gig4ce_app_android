package com.gigforce.app.modules.gigPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class GigPageFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"
        const val TEXT_VIEW_ON_MAP = "(View On Map)"

    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private lateinit var gigId: String
    private var gig: Gig? = null
    private var comingFromCheckInScreen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_present, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initUi()
        initViewModel()
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

        checkInOrContactUsBtn.setOnClickListener {view ->
            gig?.let {

                if (it.isGigOfToday()) {

                    if (comingFromCheckInScreen) {
                        activity?.onBackPressed()
                    } else {
                        navigate(R.id.gigAttendancePageFragment, Bundle().apply {
                            this.putString(GigAttendancePageFragment.INTENT_EXTRA_GIG_ID, it.gigId)
                        })
                    }
                } else {
                    if (it.contactNo != null) {
                        val intent =
                            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.contactNo, null))
                        startActivity(intent)
                    } else {}
                }
            }
        }

        contactUsBtn.setOnClickListener {
            gig?.contactNo?.let {

                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it, null))
                startActivity(intent)
            }
        }

        favoriteCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && gig?.isFavourite!!.not())
                viewModel.favoriteGig(gigId)
            else if (!isChecked && gig?.isFavourite!!)
                viewModel.unFavoriteGig(gigId)
        }
    }


    private fun initViewModel() {
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

    private fun setGigDetailsOnView(gig: Gig) {
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
        gigIdTV.text = gig.gigId

        if (gig.isFavourite && favoriteCB.isChecked.not()) {
            favoriteCB.isChecked = true
        } else if (gig.isFavourite.not() && favoriteCB.isChecked) {
            favoriteCB.isChecked = false
        }

        if (gig.isGigOfToday()) {

            if (gig.isCheckInAndCheckOutMarked()) {
                //Attendance have been marked show it
                gigControlsLayout.gone()
                completedGigControlsLayout.visible()
                dateTV.text = DateHelper.getDateInDDMMYYYY(gig.startDateTime!!.toDate())

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
                completedGigControlsLayout.gone()
                gigControlsLayout.visible()

                if (!gig.isCheckInMarked()) {

                    checkInOrContactUsBtn.text = "Check In"
                    fetchingLocationTV.text =
                        "Note :We are fetching your location to mark attendance."
                } else if (!gig.isCheckOutMarked()) {

                    checkInOrContactUsBtn.text = "Check Out"
                    fetchingLocationTV.text =
                        "Note :We are fetching your location to mark attendance."
                }
            }
        } else if (gig.isGigOfFuture()) {
            completedGigControlsLayout.gone()
            gigControlsLayout.visible()

            val timeLeft = gig.startDateTime!!.toDate().time - Date().time
            val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeft)

            checkInOrContactUsBtn.text = "Contact Us"
            fetchingLocationTV.text =
                "Note :We are preparing your gig.It will start in next $daysLeft Days"
        } else if (gig.isGigOfPast()) {

            if (gig.isCheckInOrCheckOutMarked()) {

                gigControlsLayout.gone()
                completedGigControlsLayout.visible()

                dateTV.text = DateHelper.getDateInDDMMYYYY(gig.startDateTime!!.toDate())

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
                completedGigControlsLayout.gone()
                gigControlsLayout.visible()

                //Past Gig which user did not attended
                checkInOrContactUsBtn.text = "Contact Us"
                fetchingLocationTV.text = "Note :You did not attended this gig."
            }
        }

        if (gig.endDateTime != null)
            durationTextTV.text =
                "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"
        else
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "


        shiftTV.text = "${gig.duration} per Day "
        wageTV.text = "${gig.gigAmount} per Day "

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

            addMarkerOnMap(
                latitude = gig.latitude!!,
                longitude = gig.longitude!!
            )
        } else {
            //Hide Map maybe
        }

        if (gig.locationPictures.isNotEmpty()) {
            //Inflate Pics
            locationImageScrollView.visibility = View.VISIBLE
            locationImageContainer.removeAllViews()
            inflateLocationPics(gig.locationPictures)
        } else {
            locationImageScrollView.visibility = View.GONE
        }

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
        gigRequirementsContainer.inflate(R.layout.gig_details_item, true)
        val gigItem: LinearLayout =
            gigRequirementsContainer.getChildAt(gigRequirementsContainer.childCount - 1) as LinearLayout
        val gigTextTV: TextView = gigItem.findViewById(R.id.text)
        gigTextTV.text = it
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
}