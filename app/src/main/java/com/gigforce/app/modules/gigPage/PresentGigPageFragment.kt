package com.gigforce.app.modules.gigPage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigDetails
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.Lce
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import java.text.SimpleDateFormat


class PresentGigPageFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private lateinit var gigId: String

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

        checkInOrContactUsBtn.setOnClickListener {
            navigate(R.id.gigAttendancePageFragment)
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
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = gig.gigId

        if (isGigOfToday(gig.startDate)) {
            checkInOrContactUsBtn.text = "Check In"
            fetchingLocationTV.text = "Note :We are fetching your location to mark attendance."
        } else {
            checkInOrContactUsBtn.text = "Contact Us"
            fetchingLocationTV.text = "Note :We are preparing your gig.It will start in next 2 Days"
        }

        setGigDetails(gig.gigDetails)

        gigHighlightsContainer.removeAllViews()
        inflateGigHighlights(gig.gigHighLights)

        gigRequirementsContainer.removeAllViews()
        inflateGigRequirements(gig.gigRequirements)

        if (gig.gigLocationDetails != null) {
            fullMapAddresTV.text = gig.gigLocationDetails?.fullAddress
            addMarkerOnMap(
                    latitude = gig.gigLocationDetails!!.latitude!!,
                    longitude = gig.gigLocationDetails!!.longitude!!
            )
        } else {
            //make location layout invisivle
        }
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

    @SuppressLint("SetTextI18n")
    private fun setGigDetails(gigDetails: GigDetails) {
        durationTextTV.text =
                "${dateFormatter.format(gigDetails.startTime)} - ${dateFormatter.format(gigDetails.endTime)}"
        shiftTV.text = gigDetails.shiftDuration
        addressTV.text = gigDetails.address
        wageTV.text = gigDetails.wage
    }

    private fun isGigOfToday(startDate: Timestamp): Boolean {
        return true
    }
}