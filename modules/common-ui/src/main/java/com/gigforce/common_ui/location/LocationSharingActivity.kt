package com.gigforce.common_ui.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.gigforce.common_ui.R
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.databinding.ActivityLocationSharingBinding
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.core.AppConstants.INTENT_EXTRA_CHAT_HEADER_ID
import com.gigforce.core.AppConstants.INTENT_EXTRA_CHAT_MESSAGE_ID
import com.gigforce.core.AppConstants.INTENT_EXTRA_CHAT_TYPE
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.BaseActivity
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.location.LocationUpdates
import com.gigforce.core.navigation.INavigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationSharingActivity : BaseActivity(), OnMapReadyCallback,
    LocationUpdates.LocationUpdateCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {

        const val INTENT_EXTRA_LATITUDE = "latitude"
        const val INTENT_EXTRA_LONGITUDE = "longitude"
        const val INTENT_EXTRA_PHYSICAL_ADDRESS = "physical_address"
        const val INTENT_EXTRA_MAP_IMAGE_FILE = "map_image"
        const val INTENT_EXTRA_IS_LIVE_LOCATION = "is_live_location"
        const val INTENT_EXTRA_LIVE_END_TIME = "live_end_time"
        const val REQUEST_LOCATION_PERMISSION = 244
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private val viewModel: LocationSharingViewModel by viewModels()

    private var chatMessage: ChatMessage? = null


    // The BroadcastReceiver used to listen from broadcasts from the service.
    private val myReceiver =  object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val location =
                intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                Log.d("LocationUpdatesLocation", "loc: ${location.latitude} , ${location.longitude}")
            }
        }
    }

    private var locationAddress = ""
    private lateinit var supportMapFragment: SupportMapFragment


    private var googleMap: GoogleMap? = null

    private var marker = MarkerOptions()

    private var location: Location? = null
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }

    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    private var chatType: String = ChatConstants.CHAT_TYPE_USER
    private var chatHeaderOrGroupId: String? = null
    private var chatMessageId: String? = null

    var selectedTab = 0
    private lateinit var viewBinding: ActivityLocationSharingBinding

    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLocationSharingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        changeStatusBarColor()
//        setStatusBarIcons(false)
        getDataFromIntents(savedInstanceState)
        checkAndAskForPermission()
        initLayout()
        initListeners()
        initViewModel()
        initMap()
    }

    private fun getDataFromIntents(bundle: Bundle?) {
        val bundle1 = intent.extras
        if (bundle1 != null) {
            chatType = bundle1.getString(INTENT_EXTRA_CHAT_TYPE)
                ?: throw IllegalArgumentException("please provide INTENT_EXTRA_CHAT_TYPE in intent extra")
            chatHeaderOrGroupId = bundle1.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            chatMessageId = bundle1.getString(INTENT_EXTRA_CHAT_MESSAGE_ID) ?: ""
        }
    }


    private fun initListeners() = viewBinding.apply {

        imageViewStop.setOnClickListener {
            //share location
            var endTime: Date? = null
            val date: Calendar = Calendar.getInstance()
            val t: Long = date.timeInMillis
            if (selectedTab == 0){
                //15 minutes
                endTime = Date(t + (15 * 60000))
            } else if (selectedTab == 1){
                //1 hour
                endTime = Date(t + (60 * 60000))
            } else if (selectedTab == 2){
                //8 hours
                endTime = Date(t + (480 * 60000))
            }
            Log.d("LocationSharingActivity", "Start: ${date.time} , End: $endTime")
            mService?.requestLocationUpdates(chatType, chatHeaderOrGroupId , date.time, endTime)
            if (endTime != null) {
                //check if there is already live location sharing in other chat -> pending
                sharedPreAndCommonUtilInterface.saveData("recent_loc_message", "")
                sharedPreAndCommonUtilInterface.saveData("recent_receiverId_message", "")
                sendResultsBack(true, endTime)
            }
        }

        shareCurrentLocation.setOnClickListener {
            //share current location
            sendResultsBack(false, null)
        }

        shareLiveLocation.setOnClickListener {
            //show options
            liveLocationLayout.visible()
            optionsLayout.gone()

        }

        stopSharing.setOnClickListener {
            if (stopSharing.text == "Stop sharing"){
                MaterialAlertDialogBuilder(this@LocationSharingActivity)
                    .setTitle("Alert")
                    .setMessage("Are you sure to stop sharing location?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        if (chatType == "user"){
                            viewModel.stopSharingLocation(chatHeaderOrGroupId!!, messageId = chatMessageId!!, receiverId = chatMessage?.receiverInfo?.id.toString())
                        } else {
                            viewModel.stopSharingGroupLocation(chatHeaderOrGroupId!!, messageId = chatMessageId!!)
                        }
                        sharedPreAndCommonUtilInterface.saveData("recent_loc_message", "")
                        sharedPreAndCommonUtilInterface.saveData("recent_receiverId_message", "")
                        mService?.stopLocationUpdates()
                        onBackPressed()
                    }
                    .setNegativeButton("No") {dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun initMap() = viewBinding.apply {
        //map.getMapAsync(this@LocationSharingActivity)
        supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this@LocationSharingActivity)
    }


    private fun addMarkerOnMap(latitude: Double, longitude: Double) {
        if (googleMap == null) {
            Log.d("CheckInFragment", "Unable to Show Location on Map")
            return
        }
        googleMap?.clear()

        // create marker

        marker.position(LatLng(latitude, longitude)).title("Current location")
        val mapIcon = bitmapDescriptorFromVector(this, R.drawable.ic_map_marker)
        marker.icon(mapIcon)

        // adding marker
        googleMap?.addMarker(marker)
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(15f)
            .build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun initViewModel() {
        if (chatHeaderOrGroupId?.isNotEmpty() == true && chatMessageId?.isNotEmpty() == true && chatType == "user"){
            Log.d("LocationSharingActivity", "User -> header: $chatHeaderOrGroupId , messageId: $chatMessageId")
            viewModel.addSnapshotToLiveLocationMessage(chatHeaderOrGroupId!!, chatMessageId!!)
            viewModel.startListeningForHeaderChanges(chatHeaderOrGroupId!!)
        } else if (chatHeaderOrGroupId?.isNotEmpty() == true && chatMessageId?.isNotEmpty() == true && chatType == "group") {
            Log.d("LocationSharingActivity", "Group -> groupId: $chatHeaderOrGroupId , messageId: $chatMessageId")
            viewModel.addSnapShotToGroupLiveLocationMessage(chatHeaderOrGroupId!!, chatMessageId!!)
            viewModel.startWatchingGroupDetails(chatHeaderOrGroupId!!)
        }

        viewModel.liveLocationMessage.observe(this, androidx.lifecycle.Observer {
            Log.d("ViewLiveLocationFragment", "message: ${it?.isCurrentlySharingLiveLocation} ${it?.id} , ${it?.location} , ${it?.isLiveLocation}")
            chatMessage = it
            if (it?.location != null){
                Log.d("CheckInFragment", "Setting live location")
                it.location?.let { it1 -> addMarkerOnMap(it1.latitude, it1.longitude) }
            }
            if (it?.isCurrentlySharingLiveLocation == true){
                if (it?.senderInfo?.id == FirebaseAuth.getInstance().currentUser?.uid){
                    //current user is sender -> show stop sharing button and time left
                    viewBinding.stopSharing.visible()
                    viewBinding.personName.text = "You"
                    viewBinding.timeLeft.text = it?.liveEndTime?.let { it1 -> getDuration(it1) } + " left"
                } else {
                    viewBinding.stopSharing.gone()
                    viewBinding.personName.text = it?.senderInfo?.name ?: ""
                    viewBinding.timeLeft.text = "Updated " + it?.updatedAt?.toDate()?.let { it1 ->
                        com.gigforce.core.utils.DateHelper.getDateFromTimeStamp(it1)
                    }?.let { it2 -> formatTimeAgo(it2) }
                }
            } else {
                viewBinding.appBarComp.setAppBarTitle(it?.receiverInfo?.name ?: "Live location")
                viewBinding.stopSharing.gone()
                viewBinding.timeLeft.gone()
                viewBinding.personName.gone()
                viewBinding.locationEnded.visible()
                viewBinding.lastUpdatedAt.visible()
                viewBinding.lastUpdatedAt.text = "Last updated " + it?.updatedAt?.toDate()?.let { it1 ->
                    com.gigforce.core.utils.DateHelper.getDateFromTimeStamp(it1)
                }?.let { it2 -> formatTimeAgo(it2) }
            }
        })

        viewModel.headerInfo.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            Log.d("ViewLiveLocationFragment", "header info : ${it.otherUser?.name}")

            if (it.otherUser != null){
                viewBinding.appBarComp.setAppBarTitle(it?.otherUser?.name ?: "Live location")
            }
        })

        viewModel.groupInfo.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer

            if (it.name != null){
                viewBinding.appBarComp.setAppBarTitle(it?.name ?: "Live location")
            }
        })
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

    private fun initLayout() = viewBinding.apply {
        Log.d("LocationSharingActivity", "initLayout()")
        appBarComp.apply {
            makeRefreshVisible(false)
            makeSearchVisible(false)
            makeBackgroundMoreRound()
            changeBackButtonDrawable()
            setBackButtonListener(View.OnClickListener {
                this@LocationSharingActivity?.onBackPressed()
            })
        }

        if (chatMessageId.isNullOrBlank()){
            bottomLayout.visible()
            bottomLayoutSharing.gone()
        } else {
            //user came here to view the live location or view & stop
            bottomLayout.gone()
            bottomLayoutSharing.visible()

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

        if (!isLocationPermissionGranted()) {
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
                Toast.makeText(
                    this,
                    getString(R.string.please_grant_location_permission_chat),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun sendResultsBack(isLiveLocation: Boolean, endTime: Date?) {
        if (location == null)
            return

        if (googleMap != null) {
            googleMap!!.snapshot {

                val file = File(filesDir, "map-${DateHelper.getFullDateTimeStamp()}.png")
                ImageUtils.writeBitmapToDisk(it, file)

                val resultIntent = Intent()
                resultIntent.putExtra(INTENT_EXTRA_LATITUDE, location!!.latitude)
                resultIntent.putExtra(INTENT_EXTRA_LONGITUDE, location!!.longitude)
                resultIntent.putExtra(INTENT_EXTRA_PHYSICAL_ADDRESS, locationAddress)
                resultIntent.putExtra(INTENT_EXTRA_MAP_IMAGE_FILE, file)
                resultIntent.putExtra(INTENT_EXTRA_IS_LIVE_LOCATION, isLiveLocation)
                resultIntent.putExtra(INTENT_EXTRA_LIVE_END_TIME, endTime)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra(INTENT_EXTRA_LATITUDE, location!!.latitude)
            resultIntent.putExtra(INTENT_EXTRA_LONGITUDE, location!!.longitude)
            resultIntent.putExtra(INTENT_EXTRA_PHYSICAL_ADDRESS, locationAddress)
            resultIntent.putExtra(INTENT_EXTRA_IS_LIVE_LOCATION, isLiveLocation)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
//        if (chatMessageId.isNullOrBlank()){
            PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
            // Bind to the service. If the service is in foreground mode, this signals to the service
            // that since this activity is in the foreground, the service can exit foreground mode.
            bindService(
                Intent(this, LocationUpdatesService::class.java), mServiceConnection,
                BIND_AUTO_CREATE
            )
        //}

    }

    override fun onPause() {
        if (myReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
        };
        super.onPause()
        setStatusBarIcons(true)
        locationUpdates.stopLocationUpdates(this)
    }


    override fun onResume() {
        super.onResume()
        if (chatMessageId.isNullOrBlank()) {
            locationUpdates.setLocationUpdateCallbacks(this)
            locationUpdates.startUpdates(this)
        } else {
            if (myReceiver != null) {
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    myReceiver,
                    IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
                )
            }
        }
    }
        override fun onStop() {
//            if (chatMessageId.isNullOrBlank()) {
                if (mBound) {
                    // Unbind from the service. This signals to the service that this activity is no longer
                    // in the foreground, and the service can respond by promoting itself to a foreground
                    // service.
                    unbindService(mServiceConnection)
                    mBound = false
                }
                PreferenceManager.getDefaultSharedPreferences(this)
                    .unregisterOnSharedPreferenceChangeListener(this)
            //}
            super.onStop()
        }

        @SuppressLint("MissingPermission")
        override fun onMapReady(p0: GoogleMap?) {
            this.googleMap = p0
        }

        override fun locationReceiver(location: Location?) {
            this.location = location
            val address = processLocationAndUpdateUserDetails(location!!)
            Log.d("received","$location")
            Log.d("CheckInFragment", "Setting current location")
            addMarkerOnMap(location?.latitude, location?.longitude)
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


        override fun lastLocationReceiver(location: Location?) {
            Log.d("lastLocation","$location")
        }

        private fun changeStatusBarColor() {
            var win: Window? = this?.window
            // clear FLAG_TRANSLUCENT_STATUS flag:
            win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // finally change the color
            win?.statusBarColor = resources.getColor(R.color.pink)
        }

        fun setStatusBarIcons(shouldChangeStatusBarTintToDark: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor: View = this?.window?.decorView!!
                if (shouldChangeStatusBarTintToDark) {
                    decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    // We want to change tint color to white again.
                    // You can also record the flags in advance so that you can turn UI back completely if
                    // you have set other flags before, such as translucent or full screen.
                    decor.systemUiVisibility = 0
                }
            }
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
            // Update the buttons state depending on whether location updates are being requested.
            if (p1.equals("requesting_location_updates")) {
//            setButtonsState(sharedPreferences.getBoolean("requesting_location_updates",
//                false));
            }
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

    private fun getDuration(d: Date): String? {
        val datetime= Calendar.getInstance()
        var diff: Duration = Duration.between(datetime.time.toInstant(), d.toInstant())
        val days: Long = diff.toDays()
        diff = diff.minusDays(days)
        val hours: Long = diff.toHours()
        diff = diff.minusHours(hours)
        val minutes: Long = diff.toMinutes()
        diff = diff.minusMinutes(minutes)
        val seconds: Long = diff.toMillis()
        val formattedDiff = StringBuilder()
        if (days != 0L) {
            if (days == 1L) {
                formattedDiff.append("$days Day ")
            } else {
                formattedDiff.append("$days Days ")
            }
        }
        if (hours != 0L) {
            if (hours == 1L) {
                formattedDiff.append("$hours hour ")
            } else {
                formattedDiff.append("$hours hours ")
            }
        }
        if (minutes != 0L) {
            if (minutes == 1L) {
                formattedDiff.append("$minutes minute ")
            } else {
                formattedDiff.append("$minutes minutes ")
            }
        }
        else {
            formattedDiff.append("0 minute ")
        }
//        if (seconds != 0L) {
//            if (seconds == 1L) {
//                formattedDiff.append("$seconds second ")
//            } else {
//                formattedDiff.append("$seconds seconds ")
//            }
//        }
        return formattedDiff.toString()
    }

}


