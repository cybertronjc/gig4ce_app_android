package com.gigforce.app.modules.gigPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
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
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_navigation_bottom_sheet.*


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

        callCardView.setOnClickListener {

            gig?.gigContactDetails?.contactNumber?.let {

                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
                startActivity(intent)
            }
        }

        messageCardView.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }


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

        viewModel.watchGig(gigId!!)
    }

    private fun setDatOnView(gig: Gig) {
        contactPersonTV.text = gig.gigContactDetails?.contactName
        toReachTV.text = "to reach : ${gig.address}"
        callCardView.isVisible = gig.gigContactDetails?.contactNumber != 0L

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
            .title(getString(R.string.gig_location))

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