package com.gigforce.modules.feature_chat.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.AppConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.UserAndGroupDetailsFragmentBinding
import com.gigforce.modules.feature_chat.databinding.ViewLiveLocationFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewLiveLocationFragment : BaseFragment2<ViewLiveLocationFragmentBinding>(
    fragmentName = "ViewLiveLocationFragment",
    layoutId = R.layout.view_live_location_fragment,
    statusBarColor = R.color.lipstick_2
) , OnMapReadyCallback {

    companion object {
        fun newInstance() = ViewLiveLocationFragment()
    }

    private val viewModel: ViewLiveLocationViewModel by viewModels()

    private lateinit var supportMapFragment: SupportMapFragment

    private var googleMap: GoogleMap? = null

    private var headerId = ""
    private var messageId = ""

    private var marker = MarkerOptions()

    override fun viewCreated(
        viewBinding: ViewLiveLocationFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntents(arguments, savedInstanceState)
        initLayout()
        initListeners()
        initViewModel()
        initMap()
    }

    private fun initViewModel() {
        if (headerId.isNotEmpty() && messageId.isNotEmpty()){
            viewModel.addSnapshotToLiveLocationMessage(headerId, messageId)
        }

        viewModel.liveLocationMessage.observe(viewLifecycleOwner, Observer {
            Log.d("ViewLiveLocationFragment", "message: ${it?.id} , ${it?.location} , ${it?.isLiveLocation}")
            if (it?.location != null){
                it.location?.let { it1 -> addMarkerOnMap(it1.latitude, it1.longitude) }
            }
        })

    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            //groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
            headerId = it.getString(AppConstants.INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            messageId = it.getString(AppConstants.INTENT_EXTRA_CHAT_MESSAGE_ID) ?: ""
        }
        savedInstanceState?.let {
            headerId = it.getString(AppConstants.INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            messageId = it.getString(AppConstants.INTENT_EXTRA_CHAT_MESSAGE_ID) ?: ""
        }
    }


    private fun initMap() = viewBinding.apply {
        //map.getMapAsync(this@LocationSharingActivity)
        supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentView) as SupportMapFragment
        supportMapFragment.getMapAsync(this@ViewLiveLocationFragment)
    }

    private fun initListeners() {

    }

    private fun addMarkerOnMap(latitude: Double, longitude: Double) {
        if (googleMap == null) {
            Log.d("CheckInFragment", "Unable to Show Location on Map")
            return
        }
        googleMap?.clear()

        // create marker

        marker.position(LatLng(latitude, longitude)).title("Live location")
        val mapIcon = context?.let { bitmapDescriptorFromVector(it, com.gigforce.common_ui.R.drawable.ic_map_marker) }
        marker.icon(mapIcon)

        // adding marker
        googleMap?.addMarker(marker)
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(15f)
            .build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun initLayout() = viewBinding.apply{
        appBarComp.apply {
            makeBackgroundMoreRound()
            changeBackButtonDrawable()
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        this.googleMap = p0
    }


}