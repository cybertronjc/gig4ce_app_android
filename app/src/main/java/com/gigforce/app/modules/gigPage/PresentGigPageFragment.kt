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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_gig_page_present.*
import java.text.SimpleDateFormat
import java.util.*


class PresentGigPageFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_present, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel(savedInstanceState)
    }

    private fun initUi() {
        gigLocationMapView.getMapAsync {
            mGoogleMap = it
            gigLocationMapView.onCreate(null)

            try {
                MapsInitializer.initialize(requireContext())
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        gigLocationMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        gigLocationMapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        gigLocationMapView.onDestroy()
        super.onDestroy()
    }


    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                setGigDetailsOnView(it)
            })

        val gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(INTENT_EXTRA_GIG_ID)
        } else {
            arguments?.getString(INTENT_EXTRA_GIG_ID)
        }

        viewModel.getPresentGig(gigId!!)
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
            TODO("calc days")
            fetchingLocationTV.text = "Note :We are preparing your gig.It will start in next 2 Days"
        }

        setGigDetails(gig.gigDetails)

        gigHighlightsContainer.removeAllViews()
        inflateGigHighlights(gig.gigHighLights)

        gigRequirementsContainer.removeAllViews()
        inflateGigRequirements(gig.gigHighLights)

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
        gigRequirementsContainer.inflate(R.layout.gig_details_item)
        val gigItem: LinearLayout =
            gigRequirementsContainer.getChildAt(gigRequirementsContainer.childCount - 1) as LinearLayout
        val gigTextTV: TextView = gigItem.findViewById(R.id.text)
        gigTextTV.text = it
    }


    private fun inflateGigHighlights(gigHighLights: List<String>) = gigHighLights.forEach {
        gigHighlightsContainer.inflate(R.layout.gig_details_item)
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

    private fun isGigOfToday(startDate: Date): Boolean {
        return true
    }
}