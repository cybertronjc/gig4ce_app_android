package com.gigforce.giger_gigs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.utils.ViewFullScreenImageDialogFragment
import com.gigforce.core.extensions.inflate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_navigation_bottom_sheet.*


class GigPageNavigationFragment : Fragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private val viewModel: GigViewModel by viewModels()
    private var mGoogleMap: GoogleMap? = null
    private var gig: Gig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_navigation, container,false)

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

        callCardView.setOnClickListener {

            gig?.gigContactDetails?.contactNumber?.let {

                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
                startActivity(intent)
            }
        }

        messageCardView.setOnClickListener {
          //  navigate(R.id.fakeGigContactScreenFragment)
        }


        startNavigationSliderBtn.setOnClickListener {

            if (gig != null && gig!!.latitude != null) {

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=${gig?.latitude},${gig?.longitude}")
                )
                startActivity(intent)
            }

        }
    }


    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
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

        viewModel.fetchGigDetails(gigId!!)
    }

    private fun setDatOnView(gig: Gig) {
        contactPersonTV.text = gig.gigContactDetails?.contactName
        toReachTV.text = "to reach : ${gig.address}"
        callCardView.isVisible = gig.gigContactDetails?.contactNumberString.isNullOrEmpty()==false

        if (gig.locationPictures.isNotEmpty()) {
            //Inflate Pics
            gigLocationLabel.visibility = View.VISIBLE
            locationImageScrollView.visibility = View.VISIBLE
            locationImageContainer.removeAllViews()
            inflateLocationPics(gig.locationPictures)

        } else {
            locationImageScrollView.visibility = View.GONE
            gigLocationLabel.visibility = View.GONE
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
            .title(getString(R.string.gig_location_giger_gigs))

        // adding marker
        it.addMarker(marker)
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(15f)
            .build()
        it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
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

}