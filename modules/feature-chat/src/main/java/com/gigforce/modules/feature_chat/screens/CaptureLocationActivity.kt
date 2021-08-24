package com.gigforce.modules.feature_chat.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.location.LocationUpdates
import com.gigforce.modules.feature_chat.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_capture_location.*
import java.io.File


class CaptureLocationActivity : AppCompatActivity(), OnMapReadyCallback,
        LocationUpdates.LocationUpdateCallbacks {


    //View
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var physicalAddressTV: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var locationIV: ImageView
    private var googleMap: GoogleMap? = null

    private var marker = MarkerOptions()

    private var location: Location? = null
    private var locationAddress = ""
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_location)

        checkAndAskForPermission()
        initView()
        initViewModel()
    }

    private fun checkAndAskForPermission() {

        if(!isLocationPermissionGranted()){
            askForLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                this,
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
                REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
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
                Toast.makeText(this, getString(R.string.please_grant_location_permission_chat), Toast.LENGTH_SHORT).show()
        }
    }




    override fun onPause() {
        super.onPause()
        locationUpdates.stopLocationUpdates(this)
    }

    override fun onResume() {
        super.onResume()
        locationUpdates.setLocationUpdateCallbacks(this@CaptureLocationActivity)
        locationUpdates.startUpdates(this)
    }

    private fun initView() {

        share_iv.setOnClickListener {
            sendResultsBack()
        }

        back_arrow.setOnClickListener {
            onBackPressed()
        }

        progressBar = findViewById(R.id.progress_bar)
        locationIV = findViewById(R.id.locationIconIV)
        physicalAddressTV = findViewById(R.id.address_tv)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    private fun initViewModel() {
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
    }

    override fun locationReceiver(location: Location?) {
        this.location = location
        val address = processLocationAndUpdateUserDetails(location!!)

        addMarkerOnMap(location.latitude, location.longitude)

        progressBar.gone()
        physicalAddressTV.visible()
        locationIV.visible()

        physicalAddressTV.text = address
    }

    private fun addMarkerOnMap(latitude: Double, longitude: Double) {
        if (googleMap == null) {
            Log.d("CheckInFragment", "Unable to Show Location on Map")
            return
        }
        googleMap?.clear()

        // create marker
        marker.position(LatLng(latitude, longitude)).title("Current locaton")

        // adding marker
        googleMap?.addMarker(marker)
        val cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15f)
                .build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                sendResultsBack()
            }
            else -> {
            }
        }

        return true
    }

    private fun sendResultsBack() {
        if (location == null)
            return

        if (googleMap != null) {
            googleMap!!.snapshot {

                val file = File(filesDir,"map-${DateHelper.getFullDateTimeStamp()}.png")
                ImageUtils.writeBitmapToDisk(it,file)

                val resultIntent = Intent()
                resultIntent.putExtra(INTENT_EXTRA_LATITUDE, location!!.latitude)
                resultIntent.putExtra(INTENT_EXTRA_LONGITUDE, location!!.longitude)
                resultIntent.putExtra(INTENT_EXTRA_PHYSICAL_ADDRESS, locationAddress)
                resultIntent.putExtra(INTENT_EXTRA_MAP_IMAGE_FILE, file)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra(INTENT_EXTRA_LATITUDE, location!!.latitude)
            resultIntent.putExtra(INTENT_EXTRA_LONGITUDE, location!!.longitude)
            resultIntent.putExtra(INTENT_EXTRA_PHYSICAL_ADDRESS, locationAddress)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun lastLocationReceiver(location: Location?) {
    }

    private fun processLocationAndUpdateUserDetails(location: Location): String {

        val latitude: Double = location.latitude
        val longitude: Double = location.longitude

        locationAddress = ""
        try {
            val geocoder = Geocoder(this)
            val addressArr = geocoder.getFromLocation(latitude, longitude, 1)

            locationAddress = if (addressArr.isNotEmpty())
                addressArr?.get(0)?.getAddressLine(0) ?: ""
            else
                ""
        } catch (e: Exception) {

        }

        return locationAddress
    }


    companion object {

        const val INTENT_EXTRA_LATITUDE = "latitude"
        const val INTENT_EXTRA_LONGITUDE = "longitude"
        const val INTENT_EXTRA_PHYSICAL_ADDRESS = "physical_address"
        const val INTENT_EXTRA_MAP_IMAGE_FILE = "map_image"

        const val REQUEST_LOCATION_PERMISSION = 244
    }
}