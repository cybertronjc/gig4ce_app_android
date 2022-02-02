package com.gigforce.modules.feature_chat.screens

import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.location.LocationUpdates
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.LocationSharingFragmentBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import java.lang.NullPointerException
import javax.inject.Inject

@AndroidEntryPoint
class LocationSharingFragment : BaseFragment2<LocationSharingFragmentBinding>(
        fragmentName = "LocationSharingFragment",
        layoutId = R.layout.location_sharing_fragment,
        statusBarColor = R.color.lipstick_2
) , OnMapReadyCallback,
    LocationUpdates.LocationUpdateCallbacks{

    companion object {
        fun newInstance() = LocationSharingFragment()
        const val TAG = "LocationSharingFragment"

    }

    //private lateinit var viewModel: ContactsAndGroupViewModel

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private var googleMap: GoogleMap? = null

    private var marker = MarkerOptions()

    private var location: Location? = null
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }

    var selectedTab = 0

    private lateinit var viewModel: LocationSharingViewModel


    override fun viewCreated(
        viewBinding: LocationSharingFragmentBinding,
        savedInstanceState: Bundle?
    ) {
       initLayout()
        initListeners()
        initViewModel()
        initMap()
    }

    private fun initListeners() = viewBinding.apply{

        imageViewStop.setOnClickListener {
            //share location
        }

        shareCurrentLocation.setOnClickListener {
            //share current location
        }

        shareLiveLocation.setOnClickListener {
            //show options
            liveLocationLayout.visible()
            optionsLayout.gone()

        }
    }

    private fun initMap() {

    }

    private fun initViewModel() {

    }

    private fun initLayout() = viewBinding.apply{

        appBarComp.apply {
            makeBackgroundMoreRound()
            changeBackButtonDrawable()
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

        intervalTabLayout.addTab(intervalTabLayout.newTab().setText("15 minutes"))
        intervalTabLayout.addTab(intervalTabLayout.newTab().setText("1 hour"))
        intervalTabLayout.addTab(intervalTabLayout.newTab().setText("8 hours"))

        val betweenSpace = 25

        val slidingTabStrip: ViewGroup = intervalTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        try {
            //showToast("position: ${selectedTab}")
            intervalTabLayout.getTabAt(selectedTab)?.select()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        intervalTabLayout.onTabSelected {
            selectedTab = it?.position!!
        }
    }

    private fun checkAndAskForPermission() {

        if(!isLocationPermissionGranted()){
            askForLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForLocationPermission() {
        Log.v("Location", "Permission Required. Requesting Permission")
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            CaptureLocationActivity.REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CaptureLocationActivity.REQUEST_LOCATION_PERMISSION) {
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }

            if (allPermsGranted) {
                //Okay
            } else
               showToast(getString(R.string.please_grant_location_permission_chat))
        }
    }




    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()

    }


    override fun onMapReady(p0: GoogleMap?) {

    }

    override fun locationReceiver(location: Location?) {

    }

    override fun lastLocationReceiver(location: Location?) {

    }

}