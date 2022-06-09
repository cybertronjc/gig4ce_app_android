package com.gigforce.giger_gigs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.common_image_picker.image_capture_camerax.CameraActivity
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.decors.VerticalItemDecorator
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.LocationUpdates
import com.gigforce.common_ui.utils.LocationUtils
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewState
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.gigpage.ContactPerson
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.models.AttendanceType
import com.gigforce.core.datamodels.gigpage.models.OtherOption
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toFirebaseTimeStamp
import com.gigforce.core.extensions.toLocalDateTime
import com.gigforce.core.extensions.visible
import com.gigforce.core.location.GpsSettingsCheckCallback
import com.gigforce.core.location.LocationHelper
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.adapters.GigPeopleToExpectAdapter
import com.gigforce.giger_gigs.adapters.GigPeopleToExpectAdapterClickListener
import com.gigforce.giger_gigs.adapters.OtherOptionClickListener
import com.gigforce.giger_gigs.adapters.OtherOptionsAdapter
import com.gigforce.giger_gigs.bottomsheets.EarlyOrLateCheckInBottomSheet
import com.gigforce.giger_gigs.bottomsheets.PermissionRequiredBottomSheet
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragmentResultListener
import com.gigforce.giger_gigs.dialogFragments.NotInGigRangeDialogFragment
import com.gigforce.giger_gigs.dialogFragments.RateGigDialogFragment
import com.gigforce.user_tracking.schedular.TrackingScheduler
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gig_page_2.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_address.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_feedback.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_gig_type.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_main.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_other_options.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_people_to_expect.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_toolbar.*
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GigPage2Fragment : Fragment(),
    OtherOptionClickListener,
    PopupMenu.OnMenuItemClickListener,
    DeclineGigDialogFragmentResultListener,
    GigPeopleToExpectAdapterClickListener,
    PermissionRequiredBottomSheet.PermissionBottomSheetActionListener,
    LocationUpdates.LocationUpdateCallbacks,
    EarlyOrLateCheckInBottomSheet.OnEarlyOrLateCheckInBottomSheetClickListener,
    EasyPermissions.PermissionCallbacks {


    private val gigSharedViewModel: SharedGigViewModel by activityViewModels()
    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String
    private var location: Location? = null
    private var imageClickedPath: String? = null
    private var manager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null
    private var isRequestingLocation = false
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var gigNavigation: GigNavigation

    @Inject
    lateinit var eventTracker: IEventTracker
    private val locationHelper: LocationHelper by lazy {
        LocationHelper(requireContext())
            .apply {
                setRequiredGpsPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                setLocationCallback(locationCallback)
                init()
            }
    }

    private val trackingScheduler: TrackingScheduler by lazy {
        TrackingScheduler(requireContext())
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null)
                return

            location = locationResult.lastLocation
        }
    }

    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    private val peopleToExpectAdapter: GigPeopleToExpectAdapter by lazy {
        GigPeopleToExpectAdapter(requireContext()).apply {
            this.setListener(this@GigPage2Fragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_page_2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfFragmentIsVisible()
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        setUpReviewFlow()

        if (isNecessaryPermissionGranted())
            checkForGpsStatus()
        else
            showPermissionRequiredAndTheirReasonsDialog(true)
    }


    private fun checkIfFragmentIsVisible() {
        val fragment = childFragmentManager.findFragmentByTag(EarlyOrLateCheckInBottomSheet.TAG)
        if (fragment != null && fragment is EarlyOrLateCheckInBottomSheet) {
            fragment.onEarlyOrLateCheckInBottomSheetClickListener = this
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if (::gigId.isLateinit.not()) {
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigPage2Fragment: No Gig id found")
        }

        Log.d(TAG, "Gig Id : $gigId")
    }

    private fun initUi() {

        details_label.setOnClickListener {
            Log.d(TAG, "Opening Details Page for gig ${viewModel.currentGig?.gigId}")
            navigation.navigateTo(
                "gig/gigDetailsFragment", bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
                )
            )
        }

        image_view.setOnClickListener {

            val gig = viewModel.currentGig ?: return@setOnClickListener
            if (gig.latitude != null && gig.longitude != 0.0) {
                val uri =
                    "http://maps.google.com/maps?q=loc:${gig.latitude},${gig.longitude} (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            } else if (gig.geoPoint != null) {
                val uri =
                    "http://maps.google.com/maps?q=loc:${gig.geoPoint!!.latitude},${gig.geoPoint!!.longitude} (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        people_to_expect_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        people_to_expect_rv.addItemDecoration(VerticalItemDecorator(30))
        people_to_expect_rv.adapter = peopleToExpectAdapter

        expand_iv.setOnClickListener {
            navigation.navigateTo(
                "gig/gigDetailsFragment", bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
                )
            )
        }

        gig_cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        feedback_layout.setOnClickListener {

            val gig = viewModel.currentGig ?: return@setOnClickListener

            if (gig.gigRating == 0.0f) {
                showFeedbackBottomSheet()
            }
        }

        gig_ellipses_iv.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_gig_2, popupMenu.menu)
            viewModel.currentGig?.let {

                val status = GigStatus.fromGig(it)
                popupMenu.menu.findItem(R.id.action_decline_gig).isVisible =
                    status == GigStatus.UPCOMING || status == GigStatus.PENDING
                popupMenu.menu.findItem(R.id.action_feedback)
                    .setVisible(status == GigStatus.COMPLETED)
            }

            popupMenu.setOnMenuItemClickListener(this@GigPage2Fragment)
            popupMenu.show()
        }

        checkInCheckOutSliderBtn?.setOnClickListener {

            val gig = viewModel.currentGig ?: return@setOnClickListener

            //event

            FirebaseAuth.getInstance().currentUser?.uid?.let {
                gig.getFullCompanyName()?.let { it1 ->
                    val map = mapOf(
                        "Giger ID" to gig.gigerId,
                        "TL ID" to it,
                        "Business Name" to it1,
                        "gigId" to gigId
                    )
                    eventTracker.pushEvent(TrackingEventArgs("giger_attempted_checkin", map))
                }
            }
            if (isNecessaryPermissionGranted()) {

                if (!gig.isCheckInAndCheckOutMarked()) {
                    if(isLocationMandatory(gig) && location == null){
                        checkForGpsStatus()
                        return@setOnClickListener
                    }
                    if (imageClickedPath != null) {
                        Log.e("location",location?.toString()?:"")
                        //event
                        FirebaseAuth.getInstance().currentUser?.uid?.let {
                            val map = mapOf("TL ID" to it, "gigId" to gigId)
                            eventTracker.pushEvent(TrackingEventArgs("giger_marked_checkin", map))
                        }

                        checkForLateOrEarlyCheckIn()
                    } else {
                        startCameraForCapturingSelfie()
                    }
                } else {
                    //Start regularisation
                    startRegularisation()
                }
            } else {
                showPermissionRequiredAndTheirReasonsDialog(false)
            }
        }

    }

    fun isLocationMandatory( gig: Gig):Boolean{
        if(gig.isCheckInMarked())
        {
            return gig.activityConfig?.locationConfig?.checkOutLocationMandatory?:false
        }else{
            return gig.activityConfig?.locationConfig?.checkInLocationMandatory?:false
        }

    }

    override fun onResume() {
        super.onResume()


        StatusBarUtil.setColorNoTranslucent(
            requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
            )
        )

        if (location == null) {
            startLocationUpdates()
        } else {
            Log.d(TAG, "onResume() : Location already found")
        }
    }

    private fun startLocationUpdates() {
        locationHelper.startLocationUpdates()
    }

    private fun checkForGpsStatus() {


        locationHelper.checkForGpsSettings(object : GpsSettingsCheckCallback {

            override fun requiredGpsSettingAreUnAvailable(status: ResolvableApiException) {
                if (!isAdded) return

                startIntentSenderForResult(
                    status.resolution.intentSender,
                    REQUEST_UPGRADE_GPS_SETTINGS,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }

            override fun requiredGpsSettingAreAvailable() {
                if (!isAdded) return

                locationHelper.startLocationUpdates()
            }

            override fun gpsSettingsNotAvailable() {
                if (!isAdded) return
                showRedirectToGpsPageDialog()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startRegularisation() {

    }

    private fun startCheckInOrCheckOutProcess(
        checkInTimeAccToUser: Timestamp? = null
    ) {

        if (imageClickedPath == null) {
            showAlertDialog(getString(R.string.image_not_captured_giger_gigs))
            return
        }

        var distanceBetweenGigAndUser: Float = -1.0f
        if (location != null) {

            val currentGig = viewModel.currentGig ?: return
            if (currentGig.latitude != null && currentGig.latitude != 0.0) {
                //Gig has location
                val userLocation = Location("user-location").apply {
                    this.latitude = location!!.latitude
                    this.longitude = location!!.longitude
                }

                val gigLocation = Location("gig-location").apply {
                    this.latitude = currentGig.latitude!!
                    this.longitude = currentGig.longitude!!
                }

                distanceBetweenGigAndUser = userLocation.distanceTo(gigLocation)
                val maxAllowedDistanceFromGigString =
                    firebaseRemoteConfig.getString("max_checkin_distance_from_gig")
                val maxAllowedDistanceFromGig: Long = if (maxAllowedDistanceFromGigString.isEmpty())
                    MAX_ALLOWED_LOCATION_FROM_GIG_IN_METERS
                else
                    maxAllowedDistanceFromGigString.toLong()

                if (distanceBetweenGigAndUser <= maxAllowedDistanceFromGig) {
                    markAttendance(checkInTimeAccToUser, distanceBetweenGigAndUser)
                } else {
                    showLocationNotInRangeDialog(distanceBetweenGigAndUser)
                    return
                }
            } else {
                markAttendance(checkInTimeAccToUser, distanceBetweenGigAndUser)
            }
        } else {
            markAttendance(checkInTimeAccToUser, -1.0f)
        }
    }

    private fun showLocationNotInRangeDialog(
        distanceFromGig: Float
    ) {
        NotInGigRangeDialogFragment.launch(distanceFromGig, childFragmentManager)
    }

    private fun markAttendance(
        checkInTimeAccToUser: Timestamp?,
        distanceBetweenGigAndUser: Float
    ) {
        val locationPhysicalAddress = if (location != null) {
            LocationUtils.getPhysicalAddressFromLocation(
                context = requireContext(),
                latitude = location!!.latitude,
                longitude = location!!.longitude
            )
        } else {
            ""
        }

        imageClickedPath?.let {
            viewModel.markAttendance(
                location = location,
                locationPhysicalAddress = locationPhysicalAddress,
                image = it,
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = "test",
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
            )
        }


    }

    private fun showAlertDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_giger_gigs))
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.setSharedGigViewModel(gigSharedViewModel)

        lifecycleScope.launchWhenCreated {

            gigSharedViewModel.gigSharedViewModelState
                .collect {
                    it ?: return@collect

                    when (it) {
                        is SharedGigViewState.UserOkayWithNotBeingInLocationRange -> markAttendance(
                            null,
                            it.distanceBetweenGigAndUser
                        )
                        is SharedGigViewState.UserRatedGig -> viewModel.userRatedTheGig(
                            it.rating,
                            it.feedback
                        )
                        else -> {
                        }
                    }
                }
        }

        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showGigDetailsAsLoading()
                    is Lce.Content -> setGigDetailsOnView(it.content)
                    is Lce.Error -> showErrorWhileLoadingGigData(it.error)
                }
            })

        viewModel.markingAttendanceState
            .observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                when (it) {
                    Lce.Loading -> {
                        checkInCheckOutSliderBtn.isEnabled = false
                    }
                    is Lce.Content -> {
                        checkInCheckOutSliderBtn.isEnabled = true

                        if (it.content == AttendanceType.CHECK_OUT) {
                            showToast(getString(R.string.checkout_marked_giger_gigs))
                            //showFeedbackBottomSheet()
                            showReviewFlow(reviewInfo)
                        } else {
                            showToast(getString(R.string.checkin_marked_giger_gigs))
                            plantLocationTrackers()

                            eventTracker.pushEvent(
                                TrackingEventArgs(
                                    "attendance",
                                    mapOf("isPresent" to true, "gigId" to gigId)
                                )
                            )
                            //  plantLocationTrackers()
                            showReviewFlow(reviewInfo)
                        }
                    }
                    is Lce.Error -> {
                        checkInCheckOutSliderBtn.isEnabled = true
                        showAlertDialog(getString(R.string.error_marking_attendance_giger_gigs) + it.error)
                    }
                    else -> {
                    }
                }
            })

        viewModel.fetchGigDetails(gigId, true)
    }

    private fun plantLocationTrackers() {

        try {
            val gig = viewModel.currentGig ?: return
            trackingScheduler.scheduleTrackerForGig(gig)
        } catch (e: Exception) {

//            MaterialAlertDialogBuilder(requireContext())
//                .setMessage("Unable to plant trackers")
//                .setPositiveButton("Okay") { _, _ -> }
//                .show()

            e.printStackTrace()
            CrashlyticsLogger.e(
                TAG,
                "While planting trackers",
                e
            )
        }
    }

    private fun showErrorWhileLoadingGigData(error: String) {
        gig_page_2_progressbar.gone()
        gig_page_2_main_layout.gone()
        gig_page_2_error.visible()

        gig_page_2_error.text = error
    }

    private fun showGigDetailsAsLoading() {
        gig_page_2_error.gone()
        gig_page_2_main_layout.gone()
        gig_page_2_progressbar.visible()
    }

    private fun setGigDetailsOnView(gig: Gig) {
        gig_page_2_error.gone()
        gig_page_2_progressbar.gone()
        gig_page_2_main_layout.visible()

        imageClickedPath = null
        showCommonDetails(gig)
        gig_page_timer_layout.setGigData(gig)
        setAttendanceButtonVisibility(gig)

        val gigStatus = GigStatus.fromGig(gig)
        if (gigStatus == GigStatus.CANCELLED) {
            gig_page_top_bar.setBackgroundResource(R.drawable.bck_gig_toolbar_grey)
        } else {
            gig_page_top_bar.setBackgroundResource(R.drawable.bck_gig_toolbar_pink)
        }

        val status = GigStatus.fromGig(gig)
        gig_ellipses_iv.isVisible =
            status == GigStatus.COMPLETED || status == GigStatus.UPCOMING || status == GigStatus.PENDING
    }

    private fun setAttendanceButtonVisibility(gig: Gig) = when (GigStatus.fromGig(gig)) {
        GigStatus.UPCOMING -> hideAttendanceSliderButton(gig)
        GigStatus.DECLINED -> hideAttendanceSliderButton(gig)
        GigStatus.CANCELLED -> hideAttendanceSliderButton(gig)
        GigStatus.ONGOING -> showAttendanceSliderButton(gig)
        GigStatus.PENDING -> showAttendanceSliderButton(gig)
        GigStatus.NO_SHOW -> showAttendanceSliderButton(gig)
        GigStatus.COMPLETED -> hideAttendanceSliderButton(gig)
        GigStatus.MISSED -> hideAttendanceSliderButton(gig)
    }

    private fun showAttendanceSliderButton(
        gig: Gig
    ) {

        if (!gig.isCheckInMarked()) {

            checkInCheckOutSliderBtn.visible()
            checkInCheckOutSliderBtn.text = getString(R.string.check_in_giger_gigs)
        } else if (!gig.isCheckOutMarked()) {

            val checkInTime = gig.attendance!!.checkInTime?.toLocalDateTime()
            val currentTime = LocalDateTime.now()

            val minutes = Duration.between(checkInTime, currentTime).toMinutes()
            val minTimeBtwCheckInCheckOut = getMinAllowedTimeBetweenCheckInAndCheckOut()

            if (minutes >= minTimeBtwCheckInCheckOut) {
                checkInCheckOutSliderBtn.visible()
                checkInCheckOutSliderBtn.text = getString(R.string.check_out_common_ui)
            } else {
                checkInCheckOutSliderBtn.gone()
            }
        }
    }

    private fun getMinAllowedTimeBetweenCheckInAndCheckOut(): Long {
        val minTimeBtwCheckInCheckOutString = try {
            firebaseRemoteConfig.getLong(
                REMOTE_CONFIG_MIN_TIME_BTW_CHECK_IN_CHECK_OUT
            )
        } catch (e: Exception) {
            0L
        }

        return if (minTimeBtwCheckInCheckOutString < 1L) {
            2L
        } else {
            minTimeBtwCheckInCheckOutString
        }
    }

    private fun hideAttendanceSliderButton(
        gig: Gig
    ) {
        checkInCheckOutSliderBtn.gone()
    }

    private fun showCommonDetails(gig: Gig) {

        if (!gig.getFullCompanyLogo().isNullOrBlank()) {
            if (gig.getFullCompanyLogo()!!.startsWith("http", true)) {

                Glide.with(requireContext())
                    .load(gig.getFullCompanyLogo())
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                    .reference
                    .child(gig.getFullCompanyLogo()!!)

                Glide.with(requireContext())
                    .load(imageRef)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            }
        } else {
            val companyInitials = if (gig.getFullCompanyName().isNullOrBlank())
                "C"
            else
                gig.getFullCompanyName()!![0].toString().toUpperCase()

            val drawable = TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            company_logo_iv.setImageDrawable(drawable)
        }

        gig_title_tv.text = gig.getGigTitle()
        gig_company_name_tv.text = "${gig.getFullCompanyName()}"

        gig_type.text =
            if (gig.isFullDay) getString(R.string.full_time_giger_gigs) else getString(R.string.part_time_giger_gigs)

        gig_duration.text =
            ": ${timeFormatter.format(gig.startDateTime.toDate())} - ${
                timeFormatter.format(
                    gig.endDateTime.toDate()
                )
            }"

        if ((gig.latitude != null && gig.longitude != 0.0) || gig.geoPoint != null) {
            Glide.with(requireContext()).load(R.drawable.map_demo).into(image_view)
        } else {
            Glide.with(requireContext()).load(R.drawable.ic_location_illus).into(image_view)
        }

        gig_address_tv.text = gig.address

        if (gig.businessContact != null ||
            gig.agencyContact != null
        ) {
            people_to_expect_layout.visible()
            divider_below_people_to_expect.visible()

            val contactPersons = mutableListOf<ContactPerson>()

            if (gig.businessContact != null && gig.businessContact?.name != null)
                contactPersons.add(gig.businessContact!!)

            if (gig.agencyContact != null && gig.agencyContact?.name != null)
                contactPersons.add(gig.agencyContact!!)

            peopleToExpectAdapter.updatePeopleToExpect(contactPersons)
        } else {
            people_to_expect_layout.gone()
            divider_below_people_to_expect.gone()
        }

        showOtherOptions(gig)
        showUserFeedback(gig)
    }

    private fun showUserFeedback(gig: Gig) {
        if (gig.isCheckInAndCheckOutMarked()) {
            feedback_layout.visible()
            divider_below_feedback.visible()
        } else {
            feedback_layout.gone()
            divider_below_feedback.gone()
        }

        userFeedbackTV.isVisible = !gig.gigUserFeedback.isNullOrBlank()
        userFeedbackTV.text = getString(R.string.user_feedback_giger_gigs) + gig.gigUserFeedback
        userFeedbackRatingBar.rating = gig.gigRating
    }

    private fun showOtherOptions(gig: Gig) {
        val status = GigStatus.fromGig(gig)
        //get gigorder

        val IDENTITY_CARD = OtherOption(
            id = ID_IDENTITY_CARD,
            name = getString(R.string.identity_card),
            icon = R.drawable.ic_identity_card
        )

        val OFFER_LETTER = OtherOption(
            id = ID_OFFER_LETTER,
            name = getString(R.string.offer_letter),
            icon = R.drawable.ic_offer_letter_pink
        )

        val ATTENDANCE_HISTORY = OtherOption(
            id = ID_ATTENDANCE_HISTORY,
            name = getString(R.string.attendance_history),
            icon = R.drawable.ic_attendance
        )

        val DECLINE_GIG = OtherOption(
            id = ID_DECLINE_GIG,
            name = getString(R.string.decline_gig),
            icon = R.drawable.ic_gig_decline
        )


        val optionsList = mutableListOf<OtherOption>()
        if (viewModel.currentGig?.offerLetter?.isNotEmpty() == true) {
            optionsList.add(OFFER_LETTER)
        }
        val PARK_PLUS = OtherOption(
            id = ID_PARK_PLUS,
            name = getString(R.string.location_details),
            icon = R.drawable.ic_location_icon
        )
        if (viewModel.currentGig?.businessId == PARKPLUS_BUSINESSID) {
            optionsList.add(PARK_PLUS)
        }
        optionsList.add(IDENTITY_CARD)
        optionsList.add(ATTENDANCE_HISTORY)

        if (status == GigStatus.UPCOMING || status == GigStatus.PENDING) {
            optionsList.add(DECLINE_GIG)
        }

        other_options_recycler_view.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val adapter = OtherOptionsAdapter(
            requireContext(),
            optionsList
        ).apply {
            setListener(this@GigPage2Fragment)
        }
        other_options_recycler_view.adapter = adapter
    }


    override fun onOptionClicked(option: OtherOption) {

        when (option.id) {
            ID_PARK_PLUS -> {

                navigation.navigateTo("gig/TravellingDetailInfoFragment")

            }
            ID_IDENTITY_CARD -> {
                navigation.navigateTo("gig/gigerIdFragment", Bundle().apply {
                    this.putString(
                        INTENT_EXTRA_GIG_ID,
                        viewModel.currentGig?.gigId
                    )
                })
            }
            ID_ATTENDANCE_HISTORY -> {
                val gig = viewModel.currentGig ?: return

                val currentDate = LocalDate.now()

                gigNavigation.openGigAttendanceHistoryScreen(
                    gigDate = LocalDate.of(
                        currentDate.year,
                        currentDate.monthValue,
                        1
                    ), gigTitle = gig.getGigTitle(),
                    companyLogo = gig.getFullCompanyLogo()!!,
                    companyName = gig.getFullCompanyName()!!,
                    jobProfileId = gig.profile.id!!,
                    gigerId = null
                )

            }
            ID_DECLINE_GIG -> {
                showDeclineGigDialog()
            }
            ID_OFFER_LETTER -> {
                //navigate to show offer letter
                navigation.navigateToDocViewerActivity(
                    requireActivity(),
                    viewModel.currentGig?.offerLetter.toString() ?: "",
                    "OFFER_LETTER"
                )
            }

            else -> {

            }
        }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item ?: return false

        return when (item.itemId) {
            R.id.action_help -> {
                // navigate(R.id.contactScreenFragment)
                true
            }
            R.id.action_feedback -> {
                showFeedbackBottomSheet()
                true
            }
            R.id.action_share -> {
                showToast(getString(R.string.feature_under_development_giger_gigs))
                true
            }
            R.id.action_decline_gig -> {
                if (viewModel.currentGig == null)
                    return true

                if (viewModel.currentGig!!.startDateTime.toLocalDateTime() < LocalDateTime.now()) {
                    //Past or ongoing gig

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.cannot_decline_past_gigs_giger_gigs))
                        .setPositiveButton(getString(R.string.okay_text_giger_gigs)) { _, _ -> }
                        .show()

                    return true
                }

                if (viewModel.currentGig != null) {
                    showDeclineGigDialog()
                }
                true
            }
            else -> false
        }
    }

    private fun showFeedbackBottomSheet() {
        RateGigDialogFragment.launch(gigId, childFragmentManager)
    }

    private fun setUpReviewFlow() {
        manager = context?.let { ReviewManagerFactory.create(it) }
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = task.result
            } else {
                // There was some problem, log or handle the error code.
                //@ReviewErrorCode val reviewErrorCode = (task.getException() as Exception)
                Log.d("Error", task.exception.toString())

            }
        }
    }

    private fun showReviewFlow(reviewInfo: ReviewInfo?) {
        if (reviewInfo != null) {
            val flow = activity?.let { manager?.launchReviewFlow(it, reviewInfo) }
            flow?.addOnCompleteListener { task ->
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }

        }
    }

    private fun showDeclineGigDialog() {
        //event

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            val map = mapOf("Giger ID" to it)
            eventTracker.pushEvent(TrackingEventArgs("giger_attempted_decline", map))
        }
        DeclineGigDialogFragment.launch(gigId, childFragmentManager, this@GigPage2Fragment)
    }

    override fun gigDeclined() {
        showToast(getString(R.string.gig_declined_giger_gigs))
    }


    private fun startCameraForCapturingSelfie() {

        CameraActivity.launch(
            this,
            destImage = null,
            shouldUploadToServerToo = true,
            serverParentPath = "attendance"
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_UPGRADE_GPS_SETTINGS -> {

                if (resultCode == Activity.RESULT_OK) {
                    locationHelper.startLocationUpdates()
                } else if (resultCode == Activity.RESULT_CANCELED)
                    showRedirectToGpsPageDialog()

            }
            REQUEST_UPDATE_GPS_SETTINGS_MANUALLY -> {
                locationHelper.startLocationUpdates()
            }
            REQUEST_CODE_UPLOAD_SELFIE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageClickedPath = data?.getStringExtra("image_name")
                    //event
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        val map = mapOf("TL ID" to it, "gigId" to gigId)
                        eventTracker.pushEvent(TrackingEventArgs("giger_marked_checkin", map))
                    }
                    checkForLateOrEarlyCheckIn()
                }
            }
            CameraActivity.REQUEST_CODE_CAPTURE_IMAGE_2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageClickedPath =
                        data?.getStringExtra(CameraActivity.INTENT_EXTRA_UPLOADED_PATH_IN_FIREBASE_STORAGE)
                    Log.d("clickedPath", "$imageClickedPath")
                    //event
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        val map = mapOf("TL ID" to it, "gigId" to gigId)
                        eventTracker.pushEvent(TrackingEventArgs("giger_marked_checkin", map))
                    }
                    checkForLateOrEarlyCheckIn()
                }
            }
            else -> {
            }
        }
    }

    private fun showRedirectToGpsPageDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.gps_not_on_giger_gigs))
            .setMessage(getString(R.string.please_turn_on_gps_giger_gigs))
            .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ ->

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(
                    intent,
                    REQUEST_UPDATE_GPS_SETTINGS_MANUALLY
                )
            }.show()
    }


    override fun onPeopleToExpectClicked(option: ContactPerson) {
//        navigation.navigateTo(
//            "gigContactPersonBottomSheet", bundleOf(
//                GigContactPersonBottomSheet.INTENT_GIG_CONTACT_PERSON_DETAILS to option
//            )
//        )
    }

    override fun onCallManagerClicked(manager: ContactPerson) {
        manager.contactNumber?.let {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it, null))
            startActivity(intent)
        }
    }

    override fun onChatWithManagerClicked(manager: ContactPerson) {
        navigation.navigateTo(
            "chats/chatPage", bundleOf(
                AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
                AppConstants.INTENT_EXTRA_OTHER_USER_ID to manager.uid,
                AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to manager.profilePicture,
                AppConstants.INTENT_EXTRA_OTHER_USER_NAME to manager.name
            )
        )
    }

    override fun onPermissionOkayClicked(
        askPermissionsUsingSystemSdk: Boolean
    ) {
        askForRequiredPermissions(askPermissionsUsingSystemSdk)
    }

    private fun askForRequiredPermissions(
        askPermissionUsingSystemsdk: Boolean
    ) {

        if (askPermissionUsingSystemsdk) {

            val permissionToAskList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA/*,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION*/
                    )
                } else {
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE/*,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION*/
                    )
                }
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }

            requestPermissions(
                permissionToAskList,
                REQUEST_PERMISSIONS_SYTEM_SDK
            )
        } else {

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.need_to_accept_permission_giger_gigs),
                    REQUEST_PERMISSIONS_DEV_REL_LIB,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.need_to_accept_permission_giger_gigs),
                    REQUEST_PERMISSIONS_DEV_REL_LIB,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun showPermissionRequiredAndTheirReasonsDialog(
        askPermissionUsingSystemsdk: Boolean
    ) {

        val permissionRequiredAndTheirReasons = mutableMapOf<String, String>()

        val cameraPermissionGranted = EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.CAMERA
        )
        if (!cameraPermissionGranted) {
            permissionRequiredAndTheirReasons.put(
                "CAMERA",
                getString(R.string.to_click_image_for_checkin_giger_gigs)
            )
        }

        if (Build.VERSION.SDK_INT < ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            val storagePermissionGranted = EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            if (!storagePermissionGranted) {
                permissionRequiredAndTheirReasons.put(
                    "STORAGE",
                    getString(R.string.to_store_image_while_checkin_giger_gigs)
                )
            }
        }

        val locationPermissionGranted = EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!locationPermissionGranted) {
            permissionRequiredAndTheirReasons.put(
                "LOCATION",
                getString(R.string.to_capture_location_for_checkin_giger_gigs)
            )
        }

        PermissionRequiredBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            permissionBottomSheetActionListener = this,
            permissionWithReason = permissionRequiredAndTheirReasons,
            askPermissionUsingSystemSdk = askPermissionUsingSystemsdk
        )
    }

    override fun locationReceiver(location: Location?) {
        this.location = location
        stopLocationUpdates()

        if (imageClickedPath != null) {
            checkForLateOrEarlyCheckIn()
        }
    }

    private fun checkForLateOrEarlyCheckIn() {
        val gig = viewModel.currentGig ?: return
        if(isLocationMandatory(gig)&& location == null){
            checkForGpsStatus()
            return
        }
        Log.e("location",location?.toString()?:"")

        val currentTime = LocalDateTime.now()
        if (!gig.isCheckInMarked() && currentTime.isAfter(gig.checkInBeforeTime.toLocalDateTime())
            && currentTime.isBefore(gig.checkInBeforeBufferTime.toLocalDateTime())
        ) {
            //Early CheckIn
            val earlyCheckInTime = timeFormatter.format(gig.startDateTime.toDate())
            EarlyOrLateCheckInBottomSheet.launchEarlyCheckInBottomSheet(
                childFragmentManager,
                earlyCheckInTime,
                this
            )
        } else if (!gig.isCheckInMarked() && currentTime.isAfter(gig.checkInAfterBufferTime.toLocalDateTime())) {

            //Early CheckIn
            val earlyCheckInTime = timeFormatter.format(gig.startDateTime.toDate())
            EarlyOrLateCheckInBottomSheet.launchLateCheckInBottomSheet(
                childFragmentManager,
                earlyCheckInTime,
                this
            )
        } else if (!gig.isCheckOutMarked() &&
            currentTime.isAfter(gig.checkInAfterTime.toLocalDateTime()) &&
            currentTime.isBefore(gig.checkOutBeforeBufferTime.toLocalDateTime())
        ) {
            //Early CheckIn
            val earlyCheckInTime = timeFormatter.format(gig.endDateTime.toDate())
            EarlyOrLateCheckInBottomSheet.launchEarlyCheckOutBottomSheet(
                childFragmentManager,
                earlyCheckInTime,
                this
            )
        } else if (!gig.isCheckOutMarked() && currentTime.isAfter(gig.checkOutAfterBufferTime.toLocalDateTime())) {

            //Early CheckIn
            val earlyCheckInTime = timeFormatter.format(gig.endDateTime.toDate())
            EarlyOrLateCheckInBottomSheet.launchLateCheckOutBottomSheet(
                childFragmentManager,
                earlyCheckInTime,
                this
            )
        } else {
            startCheckInOrCheckOutProcess()
        }
    }

    override fun onCheckInOkayClicked(
        checkInOrCheckOutTimeAccToUser: Date?
    ) {
        startCheckInOrCheckOutProcess(
            checkInOrCheckOutTimeAccToUser.toFirebaseTimeStamp()
        )
    }

    private fun stopLocationUpdates() {
        this.isRequestingLocation = false
        locationHelper.stopLocationUpdates()
    }

    override fun lastLocationReceiver(location: Location?) {}

    private fun isNecessaryPermissionGranted(): Boolean {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
                return EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
                )
            } else {
                return EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        } else {
            return EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSIONS_DEV_REL_LIB)
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        checkForGpsStatus()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            showPermissionRequiredAndTheirReasonsDialog(false)
        }
    }

    companion object {
        const val TAG = "Gig_page_2"
        const val INTENT_EXTRA_GIG_ID = "gig_id"

        const val REQUEST_PERMISSIONS_DEV_REL_LIB = 100
        const val REQUEST_PERMISSIONS_SYTEM_SDK = 101

        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
        const val REQUEST_UPGRADE_GPS_SETTINGS = 2321
        const val REQUEST_UPDATE_GPS_SETTINGS_MANUALLY = 2322

        private const val ID_IDENTITY_CARD = "apodZsdEbx"
        private const val ID_ATTENDANCE_HISTORY = "TnovE9tzXl"
        private const val ID_DECLINE_GIG = "knnp4f4ZUi"
        private const val ID_OFFER_LETTER = "ID_OFFER_LETTER"
        private const val ID_PARK_PLUS = "ID_PARK_PLUS"
        private const val MAX_ALLOWED_LOCATION_FROM_GIG_IN_METERS = 200L

        const val REMOTE_CONFIG_SHOULD_USE_OLD_CAMERA = "should_use_old_camera"
        const val REMOTE_CONFIG_MIN_TIME_BTW_CHECK_IN_CHECK_OUT = "min_time_btw_check_in_check_out"

        const val PARKPLUS_BUSINESSID = "7lX4d0vaOrjArjH1EnsC"
    }
}