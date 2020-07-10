package com.gigforce.app.modules.gigPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
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
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit


class PresentGigPageFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val TEXT_VIEW_ON_MAP = "(View On Map)"
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private lateinit var gigId: String
    private var gig: Gig? = null

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
        gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(INTENT_EXTRA_GIG_ID)!!
        } else {
            arguments?.getString(INTENT_EXTRA_GIG_ID)!!
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

        checkInOrContactUsBtn.setOnClickListener {
            if (gig == null)
                return@setOnClickListener

            if (isGigOfToday() || isGigOfFuture()) {

                navigate(R.id.gigAttendancePageFragment, Bundle().apply {
                    this.putString(GigAttendancePageFragment.INTENT_EXTRA_GIG_ID, gigId)
                })
            } else {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.contactNo, null))
                startActivity(intent)
            }
        }

        contactUsBtn.setOnClickListener {

            if (gig != null) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.contactNo, null))
                startActivity(intent)
            }
        }

        favoriteCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                viewModel.favoriteGig(gigId)
            else
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

        if (gig.companyLogo != null) {
            FirebaseStorage.getInstance()
                .getReference("folder")
                .child(gig.companyLogo!!)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(requireContext())
                        .load(it)
                        .into(companyLogoIV)
                }

        }

        toolbar.title = gig.title
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = gig.gigId

        favoriteCB.isChecked = gig.isFavourite

        if (gig.isGigCompleted) {
            gigControlsLayout.visibility = View.GONE
            completedGigControlsLayout.visibility = View.VISIBLE

            if (gig.startDateTime != null)
                dateTV.text = dateFormatter.format(gig.startDateTime!!.toDate())
            else
                dateTV.text = "-"

            invoiceStatusBtn.text = gig.gigStatus

            if (gig.attendance != null) {

                if (gig.attendance?.checkInTime != null)
                    punchInTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkInTime!!.toDate())
                else
                    punchInTimeTV.text = "--:--"


                if (gig.attendance?.checkOutTime != null)
                    punchOutTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkOutTime!!.toDate())
                else
                    punchOutTimeTV.text = "--:--"

            } else {
                punchInTimeTV.text = "--:--"
                punchOutTimeTV.text = "--:--"
            }


        } else {
            completedGigControlsLayout.visibility = View.GONE
            gigControlsLayout.visibility = View.VISIBLE

            if (isGigOfToday()) {
                checkInOrContactUsBtn.text = "Check In"
                fetchingLocationTV.text = "Note :We are fetching your location to mark attendance."
            } else if (isGigOfPast()) {
                //Past Gig which user did not attended
                checkInOrContactUsBtn.text = "Contact Us"
                fetchingLocationTV.text = "Note :You did not attended this gig."
            } else {
                //Future Gig
                val timeLeft = gig.startDateTime!!.toDate().time - java.util.Date().time
                val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeft)

                checkInOrContactUsBtn.text = "Contact Us"
                fetchingLocationTV.text =
                    "Note :We are preparing your gig.It will start in next $daysLeft Days"
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
        inflateGigHighlights(gig.gigHighLights)

        gigRequirementsContainer.removeAllViews()
        inflateGigRequirements(gig.gigRequirements)

        if (gig.gigLocationDetails?.latitude != null) {
            addressTV.text = prepareAddress(gig.address)
        } else {
            addressTV.text = gig.address
        }

        if (gig.gigLocationDetails != null) {
            gigLocationLayout.visibility = View.VISIBLE

            if (gig.address.isNotBlank()) {
                if (gig.gigLocationDetails?.latitude != null)
                    fullMapAddresTV.text = prepareAddress(gig.address)
                else
                    fullMapAddresTV.text = gig.address
            }

            if (gig.gigLocationDetails?.latitude != null) {

                addMarkerOnMap(
                    latitude = gig.gigLocationDetails!!.latitude!!,
                    longitude = gig.gigLocationDetails!!.longitude!!
                )
            } else {
                //Hide Map maybe
            }

            if (gig.gigLocationDetails!!.locationPictures.isNotEmpty()) {
                //Inflate Pics
                locationImageScrollView.visibility = View.VISIBLE
                inflateLocationPics(gig.gigLocationDetails!!.locationPictures)
            } else {
                locationImageScrollView.visibility = View.GONE
            }
        } else {
            gigLocationLayout.visibility = View.GONE
        }
    }

    private fun inflateLocationPics(locationPictures: List<String>) = locationPictures.forEach {
        locationImageContainer.inflate(R.layout.gig_details_item, true)
        val gigItem: View =
            locationImageContainer.getChildAt(locationImageContainer.childCount - 1) as View

        gigItem.setOnClickListener(locationImageItemClickListener)

        val locationImageView: ImageView = gigItem.findViewById(R.id.imageView)
        FirebaseStorage.getInstance()
            .getReference("folder")
            .child(it)
            .downloadUrl
            .addOnSuccessListener { fileUri ->
                Glide.with(requireContext())
                    .load(fileUri)
                    .into(locationImageView)

                (locationImageView.parent as View).tag = fileUri.toString()
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

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //Launch Map
                val lat = gig?.gigLocationDetails?.latitude
                val long = gig?.gigLocationDetails?.longitude

                val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        string.setSpan(clickableSpan, address.length + 1, string.length - 1, 0)

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

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormatter = SimpleDateFormat("hh.mm aa")


    private fun isGigOfToday(): Boolean {
        if (gig == null)
            return false

        val gigDate =
            gig!!.startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    private fun isGigOfFuture(): Boolean {
        if (gig == null)
            return false

        val gigDate =
            gig!!.startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    private fun isGigOfPast(): Boolean {
        if (gig == null)
            return false

        val gigDate =
            gig!!.startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isBefore(currentDate)
    }
}