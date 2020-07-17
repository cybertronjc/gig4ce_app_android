package com.gigforce.app.modules.gigPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_navigation_bottom_sheet.*
import java.util.*


class GigPageNavigationFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private var gig: Gig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_navigation, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel(savedInstanceState)
    }

    private fun initUi() {
//        mapView.getMapAsync {
//            mGoogleMap = it
//            mapView.onCreate(null)
//
//            try {
//                MapsInitializer.initialize(requireContext())
//            } catch (e: GooglePlayServicesNotAvailableException) {
//                e.printStackTrace()
//            }
//        }


        startNavigationSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (gig != null && gig!!.latitude != null) {

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=${gig?.latitude},${gig?.longitude}")
                        )
                        startActivity(intent)
                    }
                }
            }
    }


    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {}
                    is Lce.Content -> {
                        this.gig = it.content
                        setDatOnView(it.content)
                    }
                    is Lce.Error -> {

                    }
                }
            })

        val gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(INTENT_EXTRA_GIG_ID)
        } else {
            arguments?.getString(INTENT_EXTRA_GIG_ID)
        }

        viewModel.watchGig(gigId!!)
    }

    private fun setDatOnView(content: Gig) {
        contactPersonTV.text = content.gigContactDetails?.contactName
        toReachTV.text = "to reach : ${content.address}"
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

    private fun isGigOfToday(startDate: Date): Boolean {
        return true
    }
}