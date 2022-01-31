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
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.DateHelper
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.UserAndGroupDetailsFragmentBinding
import com.gigforce.modules.feature_chat.databinding.ViewLiveLocationFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

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
            if (it?.senderInfo?.id == FirebaseAuth.getInstance().currentUser?.uid){
                //current user is sender -> show stop sharing button and time left
                viewBinding.appBarComp.setAppBarTitle(it?.receiverInfo?.name ?: "Live location")
                viewBinding.stopSharing.visible()
            } else {
                viewBinding.appBarComp.setAppBarTitle(it?.senderInfo?.name ?: "Live location")
                viewBinding.stopSharing.gone()
                viewBinding.personName.text = it?.senderInfo?.name ?: ""
                viewBinding.timeLeft.text = "Updated " +  formatTimeAgo(DateHelper.getDateFromTimeStamp(it?.updatedAt?.toDate()!!))
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

fun formatTimeAgo(date1: String): String {  // Note : date1 must be in   "yyyy-MM-dd hh:mm:ss"   format
    var conversionTime =""
    try{
        val format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        val sdf = SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));

        val datetime= Calendar.getInstance()
        var date2= sdf.format(datetime.time).toString()

        Log.d("DateHere", " $date2")
        val dateObj1 = sdf.parse(date1)
        val dateObj2 = sdf.parse(date2)
        val diff = dateObj2.time - dateObj1.time

        val diffDays = diff / (24 * 60 * 60 * 1000)
        val diffhours = diff / (60 * 60 * 1000)
        val diffmin = diff / (60 * 1000)
        val diffsec = diff  / 1000
        conversionTime += if(diffDays in 1..7){
            diffDays.toString() + "days ago"
        } else if(diffhours>1){
            (diffhours-diffDays*24).toString() + "hours ago"
        }else if(diffmin>1){
            (diffmin-diffhours*60).toString() + "minutes ago"
        }else if(diffsec>1){
            (diffsec-diffmin*60).toString() + "seconds ago"
        }else {
            " " + "moments ago"
        }
    }catch (ex:java.lang.Exception){
        Log.d("formatTimeAgo",ex.toString())
    }

    return conversionTime
}



}