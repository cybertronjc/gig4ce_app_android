package com.gigforce.giger_gigs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
//import com.gigforce.app.modules.gigPage2.viewModels.SharedGigViewModel
//import com.gigforce.app.modules.gigPage2.viewModels.SharedGigViewState
import com.gigforce.giger_gigs.adapters.GigPeopleToExpectAdapter
import com.gigforce.giger_gigs.adapters.GigPeopleToExpectAdapterClickListener
import com.gigforce.giger_gigs.adapters.OtherOptionClickListener
import com.gigforce.giger_gigs.adapters.OtherOptionsAdapter
import com.gigforce.giger_gigs.bottomsheets.EarlyOrLateCheckInBottomSheet
import com.gigforce.giger_gigs.bottomsheets.GigContactPersonBottomSheet
import com.gigforce.giger_gigs.bottomsheets.PermissionRequiredBottomSheet
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragmentResultListener
import com.gigforce.giger_gigs.dialogFragments.RateGigDialogFragment
import com.gigforce.core.datamodels.gigpage.models.*
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.common_ui.utils.LocationUtils
import com.gigforce.common_image_picker.image_capture_camerax.CameraActivity
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.decors.VerticalItemDecorator
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.LocationUpdates
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewState
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.gigpage.ContactPerson
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toFirebaseTimeStamp
import com.gigforce.core.extensions.toLocalDateTime
import com.gigforce.core.extensions.visible
import com.gigforce.core.location.GpsSettingsCheckCallback
import com.gigforce.core.location.LocationHelper
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.dialogFragments.NotInGigRangeDialogFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import kotlinx.android.synthetic.main.fragment_gig_page_2_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_main.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_other_options.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_people_to_expect.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_toolbar.*
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
    EasyPermissions.PermissionCallbacks{

    private val gigSharedViewModel : SharedGigViewModel by activityViewModels()
    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String
    private var location: Location? = null
    private var imageClickedPath: String? = null
    private var isRequestingLocation = false
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

//    private val locationUpdates: LocationUpdates by lazy {
//        LocationUpdates()
//    }

    @Inject lateinit var navigation : INavigation

    private val locationHelper: LocationHelper by lazy {
        LocationHelper(requireContext())
            .apply {
                setRequiredGpsPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                setLocationCallback(locationCallback)
                init()
            }
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null)
                return

            location = locationResult.lastLocation
        }
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    private val peopleToExpectAdapter: GigPeopleToExpectAdapter by lazy {
        GigPeopleToExpectAdapter(requireContext()).apply {
            this.setListener(this@GigPage2Fragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_page_2, container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfFragmentIsVisible()
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()

        if (isNecessaryPermissionGranted())
            checkForGpsStatus()
        else
            showPermissionRequiredAndTheirReasonsDialog()
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
            navigation.navigateTo("gig/gigDetailsFragment",bundleOf(
                com.gigforce.giger_gigs.GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
            ))
//            navigate(
//                R.id.gigDetailsFragment, bundleOf(
//                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
//                )
//            )
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

//        gig_page_timer_layout.setOnClickListener {
//            val gig = viewModel.currentGig ?: return@setOnClickListener
//
//            navigate(
//                    R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
//                    GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to gig.gigId
//            )
//            )
//        }

        expand_iv.setOnClickListener {
            navigation.navigateTo("gig/gigDetailsFragment",bundleOf(
                com.gigforce.giger_gigs.GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
            ))
//            navigate(
//                R.id.gigDetailsFragment, bundleOf(
//                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
//                )
//            )
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
                popupMenu.menu.findItem(R.id.action_decline_gig).isVisible = status == GigStatus.UPCOMING || status == GigStatus.PENDING
                popupMenu.menu.findItem(R.id.action_feedback)
                    .setVisible(status == GigStatus.COMPLETED)
            }

            popupMenu.setOnMenuItemClickListener(this@GigPage2Fragment)
            popupMenu.show()
        }

        checkInCheckOutSliderBtn?.setOnClickListener {

            val gig = viewModel.currentGig ?: return@setOnClickListener

            if (isNecessaryPermissionGranted()) {

                if (!gig.isCheckInAndCheckOutMarked()) {
                    if (imageClickedPath != null) {

                        checkForLateOrEarlyCheckIn()
                    } else {
                        startCameraForCapturingSelfie()
                    }
                } else {
                    //Start regularisation
                    startRegularisation()
                }
            } else {
                showPermissionRequiredAndTheirReasonsDialog()
            }
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
                locationHelper.startLocationUpdates()
            }

            override fun gpsSettingsNotAvailable() {
                if(!isAdded) return
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
            showAlertDialog("Image has not been captured")
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
                val maxAllowedDistanceFromGigString = firebaseRemoteConfig.getString("max_checkin_distance_from_gig")
                val maxAllowedDistanceFromGig : Long = if(maxAllowedDistanceFromGigString.isEmpty())
                    MAX_ALLOWED_LOCATION_FROM_GIG_IN_METERS
                else
                    maxAllowedDistanceFromGigString.toLong()

                if (distanceBetweenGigAndUser <= maxAllowedDistanceFromGig) {
                    markAttendance(checkInTimeAccToUser,distanceBetweenGigAndUser)
                } else {
                    showLocationNotInRangeDialog(distanceBetweenGigAndUser)
                    return
                }
            } else{
                markAttendance(checkInTimeAccToUser,distanceBetweenGigAndUser)
            }
        } else {
            markAttendance(checkInTimeAccToUser, -1.0f)
        }
    }

    private fun showLocationNotInRangeDialog(
            distanceFromGig: Float
    ) {
        NotInGigRangeDialogFragment.launch(distanceFromGig,childFragmentManager)
    }

    private fun markAttendance(
            checkInTimeAccToUser: Timestamp?,
            distanceBetweenGigAndUser : Float
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

        viewModel.markAttendance(
                location = location,
                locationPhysicalAddress = locationPhysicalAddress,
                image = imageClickedPath!!,
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = "test",
                distanceBetweenGigAndUser = distanceBetweenGigAndUser
        )
    }

    private fun showAlertDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        gigSharedViewModel.gigSharedViewModelState
                .observe(viewLifecycleOwner, Observer {
                    it ?: return@Observer

                    when (it) {
                        is SharedGigViewState.UserOkayWithNotBeingInLocationRange -> markAttendance(
                                null,
                                it.distanceBetweenGigAndUser
                        )
                        else -> {
                        }
                    }
                })

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
                            showToast("Checkout Marked.")
                            showFeedbackBottomSheet()
                        } else {
                            showToast("Check-in marked")
                        }
                    }
                    is Lce.Error -> {
                        checkInCheckOutSliderBtn.isEnabled = true
                        showAlertDialog("Error while marking attendance, $it")
                    }
                    else -> {
                    }
                }
            })

        viewModel.watchGig(gigId, true)
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
            checkInCheckOutSliderBtn.text = "Check-in"
        } else if (!gig.isCheckOutMarked()) {

            val checkInTime = gig.attendance!!.checkInTime?.toLocalDateTime()
            val currentTime = LocalDateTime.now()

            val minutes = Duration.between(checkInTime, currentTime).toMinutes()

            if (minutes > 15L) {
                checkInCheckOutSliderBtn.visible()
                checkInCheckOutSliderBtn.text = "Check-out"
            } else {
                checkInCheckOutSliderBtn.gone()
            }
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

        gig_type.text = if (gig.isFullDay) ": Full time" else ": Part time"

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

            if (gig.businessContact != null)
                contactPersons.add(gig.businessContact!!)

            if (gig.agencyContact != null)
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
        userFeedbackTV.text = "User feedback : ${gig.gigUserFeedback}"
        userFeedbackRatingBar.rating = gig.gigRating
    }

    private fun showOtherOptions(gig: Gig) {
        val status = GigStatus.fromGig(gig)

        val optionList = if (status == GigStatus.UPCOMING || status == GigStatus.PENDING) {
            listOf(
                IDENTITY_CARD,
                ATTENDANCE_HISTORY,
                DECLINE_GIG
            )
        } else {
            listOf(
                IDENTITY_CARD,
                ATTENDANCE_HISTORY
            )
        }

        other_options_recycler_view.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val adapter = OtherOptionsAdapter(
            requireContext(),
            optionList
        ).apply {
            setListener(this@GigPage2Fragment)
        }
        other_options_recycler_view.adapter = adapter
    }


    override fun onOptionClicked(option: OtherOption) {

        when (option.id) {
            ID_IDENTITY_CARD -> {
                navigation.navigateTo("gig/gigerIdFragment",Bundle().apply {
                    this.putString(
                        INTENT_EXTRA_GIG_ID,
                        viewModel.currentGig?.gigId
                    )
                })
//                navigate(R.id.giger_id_fragment, Bundle().apply {
//                    this.putString(
//                        INTENT_EXTRA_GIG_ID,
//                        viewModel.currentGig?.gigId
//                    )
//                })
            }
            ID_ATTENDANCE_HISTORY -> {
                val gig = viewModel.currentGig ?: return

                val currentDate = LocalDate.now()
                navigation.navigateTo("gig/gigMonthlyAttendanceFragment",bundleOf(
                    com.gigforce.giger_gigs.GigMonthlyAttendanceFragment.INTENT_EXTRA_SELECTED_DATE to LocalDate.of(
                        currentDate.year,
                        currentDate.monthValue,
                        1
                    ),
                    com.gigforce.giger_gigs.GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_LOGO to gig.getFullCompanyLogo(),
                    com.gigforce.giger_gigs.GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_NAME to gig.getFullCompanyName(),
                    com.gigforce.giger_gigs.GigMonthlyAttendanceFragment.INTENT_EXTRA_GIG_ORDER_ID to gig.gigOrderId,
                    com.gigforce.giger_gigs.GigMonthlyAttendanceFragment.INTENT_EXTRA_ROLE to gig.getGigTitle()
                ))
//                navigate(
//                    R.id.gigMonthlyAttendanceFragment, bundleOf(
//                        GigMonthlyAttendanceFragment.INTENT_EXTRA_SELECTED_DATE to LocalDate.of(
//                            currentDate.year,
//                            currentDate.monthValue,
//                            1
//                        ),
//                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_LOGO to gig.getFullCompanyLogo(),
//                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_NAME to gig.getFullCompanyName(),
//                        GigMonthlyAttendanceFragment.INTENT_EXTRA_GIG_ORDER_ID to gig.gigOrderId,
//                        GigMonthlyAttendanceFragment.INTENT_EXTRA_ROLE to gig.getGigTitle()
//                    )
//                )
            }
            ID_DECLINE_GIG -> {
                showDeclineGigDialog()
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
                showToast("This feature is under development")
                true
            }
            R.id.action_decline_gig -> {
                if (viewModel.currentGig == null)
                    return true

                if (viewModel.currentGig!!.startDateTime.toLocalDateTime() < LocalDateTime.now()) {
                    //Past or ongoing gig

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Cannot decline past or ongoing gig")
                        .setPositiveButton(getString(R.string.okay_text)) { _, _ -> }
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

    private fun showDeclineGigDialog() {
        DeclineGigDialogFragment.launch(gigId, childFragmentManager, this@GigPage2Fragment)
    }

    override fun gigDeclined() {
        showToast("Gig Declined")
    }


    private fun startCameraForCapturingSelfie() {
        val shouldUserOldCamString =
            firebaseRemoteConfig.getString(REMOTE_CONFIG_SHOULD_USE_OLD_CAMERA)

        val shouldUserOldCam =
            if (shouldUserOldCamString.isEmpty()) false else shouldUserOldCamString.toBoolean()
        if (shouldUserOldCam) {

            val intent = Intent()
//            startActivityForResult(
//                intent,
//                REQUEST_CODE_UPLOAD_SELFIE_IMAGE
//            )
            navigation.navigateToAttendanceImageCaptureActivity(intent,
                REQUEST_CODE_UPLOAD_SELFIE_IMAGE,requireContext(),this)
        } else {
            CameraActivity.launch(
                this,
                destImage = null,
                shouldUploadToServerToo = true,
                serverParentPath = "attendance"
            )
        }
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
                    checkForLateOrEarlyCheckIn()
                }
            }
            CameraActivity.REQUEST_CODE_CAPTURE_IMAGE_2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageClickedPath =
                        data?.getStringExtra(CameraActivity.INTENT_EXTRA_UPLOADED_PATH_IN_FIREBASE_STORAGE)
                    checkForLateOrEarlyCheckIn()
                }
            }
            else -> {
            }
        }
    }

    private fun showRedirectToGpsPageDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Gps not turned on")
            .setMessage("Please turn on location service and set Gps Accuracy to High")
            .setPositiveButton("Okay") { _, _ ->

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(
                    intent,
                    REQUEST_UPDATE_GPS_SETTINGS_MANUALLY
                )
            }.show()
    }


    override fun onPeopleToExpectClicked(option: ContactPerson) {
        navigation.navigateTo("gigContactPersonBottomSheet",bundleOf(
            GigContactPersonBottomSheet.INTENT_GIG_CONTACT_PERSON_DETAILS to option
        ))
//        navigate(
//            R.id.gigContactPersonBottomSheet, bundleOf(
//                GigContactPersonBottomSheet.INTENT_GIG_CONTACT_PERSON_DETAILS to option
//            )
//        )
    }

    override fun onCallManagerClicked(manager: ContactPerson) {
        manager.contactNumber?.let {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
            startActivity(intent)
        }
    }

    override fun onChatWithManagerClicked(manager: ContactPerson) {
        navigation.navigateTo("chats/chatPage",bundleOf(
            AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
            AppConstants.INTENT_EXTRA_OTHER_USER_ID to manager.uid,
            AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to manager.profilePicture,
            AppConstants.INTENT_EXTRA_OTHER_USER_NAME to manager.name
        ))
//        navigate(
//            R.id.chatPageFragment, bundleOf(
//                AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
//                AppConstants.INTENT_EXTRA_OTHER_USER_ID to manager.uid,
//                AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to manager.profilePicture,
//                AppConstants.INTENT_EXTRA_OTHER_USER_NAME to manager.name
//            )
//        )
    }

    override fun onPermissionOkayClicked() {
        askForRequiredPermissions()
    }

    private fun askForRequiredPermissions() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.",
                    REQUEST_PERMISSIONS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.",
                    REQUEST_PERMISSIONS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun showPermissionRequiredAndTheirReasonsDialog() {
        PermissionRequiredBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            permissionBottomSheetActionListener = this,
            permissionWithReason = PERMISSION_AND_REASONS
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
//        locationUpdates.stopLocationUpdates()
        locationHelper.stopLocationUpdates()
    }

    override fun lastLocationReceiver(location: Location?) {}

    private fun isNecessaryPermissionGranted(): Boolean {

       return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.hasPermissions(
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        checkForGpsStatus()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            showPermissionRequiredAndTheirReasonsDialog()
        }
    }

    companion object {
        const val TAG = "Gig_page_2"
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"

        const val REQUEST_PERMISSIONS = 100
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE_2 = 2334
        const val REQUEST_UPGRADE_GPS_SETTINGS = 2321
        const val REQUEST_UPDATE_GPS_SETTINGS_MANUALLY = 2322

        private const val ID_IDENTITY_CARD = "apodZsdEbx"
        private const val ID_ATTENDANCE_HISTORY = "TnovE9tzXl"
        private const val ID_DECLINE_GIG = "knnp4f4ZUi"
        private const val MAX_ALLOWED_LOCATION_FROM_GIG_IN_METERS = 200L

        const val REMOTE_CONFIG_SHOULD_USE_OLD_CAMERA = "should_use_old_camera"

        private val IDENTITY_CARD = OtherOption(
            id = ID_IDENTITY_CARD,
            name = "Identity Card",
            icon = R.drawable.ic_identity_card
        )

        private val ATTENDANCE_HISTORY = OtherOption(
            id = ID_ATTENDANCE_HISTORY,
            name = "Attendance History",
            icon = R.drawable.ic_attendance
        )

        private val DECLINE_GIG = OtherOption(
            id = ID_DECLINE_GIG,
            name = "Decline Gig",
            icon = R.drawable.ic_gig_decline
        )

        private val PERMISSION_AND_REASONS: HashMap<String, String> = hashMapOf(
            "LOCATION" to "To Capture Location For CheckIn",
            "CAMERA" to "To Click Image for CheckIn",
            "STORAGE" to "To Store Image captured while CheckIn"
        )
    }


}