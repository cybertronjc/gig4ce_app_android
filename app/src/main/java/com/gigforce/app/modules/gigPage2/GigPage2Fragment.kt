package com.gigforce.app.modules.gigPage2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.*
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.*
import com.gigforce.app.modules.gigPage2.models.ContactPerson
import com.gigforce.app.modules.gigPage2.models.Gig
import com.gigforce.app.modules.gigPage2.adapters.GigPeopleToExpectAdapter
import com.gigforce.app.modules.gigPage2.adapters.GigPeopleToExpectAdapterClickListener
import com.gigforce.app.modules.gigPage2.adapters.OtherOptionClickListener
import com.gigforce.app.modules.gigPage2.adapters.OtherOptionsAdapter
import com.gigforce.app.modules.gigPage2.bottomsheets.EarlyOrLateCheckInBottomSheet
import com.gigforce.app.modules.gigPage2.bottomsheets.GigContactPersonBottomSheet
import com.gigforce.app.modules.gigPage2.bottomsheets.PermissionRequiredBottomSheet
import com.gigforce.app.modules.gigPage2.dialogFragments.RateGigDialogFragment
import com.gigforce.app.modules.gigPage2.models.AttendanceType
import com.gigforce.app.modules.gigPage2.models.GigStatus
import com.gigforce.app.modules.gigPage2.models.OtherOption
import com.gigforce.app.modules.gigPage2.viewModels.GigViewModel
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.app.utils.*
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_2.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_address.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_feedback.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_gig_type.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_main.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_other_options.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_people_to_expect.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_toolbar.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class GigPage2Fragment : BaseFragment(),
        OtherOptionClickListener,
        PopupMenu.OnMenuItemClickListener,
        DeclineGigDialogFragmentResultListener,
        GigPeopleToExpectAdapterClickListener,
        PermissionRequiredBottomSheet.PermissionBottomSheetActionListener,
        LocationUpdates.LocationUpdateCallbacks,
        EarlyOrLateCheckInBottomSheet.OnEarlyOrLateCheckInBottomSheetClickListener {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String
    private var location: Location? = null
    private var imageClickedPath: String? = null
    private var isRequestingLocation = false

    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
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
    ) = inflateView(R.layout.fragment_gig_page_2, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfFragmentIsVisible()
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        startLocationUpdates()
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
            navigate(
                    R.id.gigDetailsFragment, bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
            )
            )
        }

        image_view.setOnClickListener {

            val gig = viewModel.currentGig ?: return@setOnClickListener
            if (gig.latitude != null && gig.longitude != 0.0) {
                val uri = "http://maps.google.com/maps?q=loc:${gig.latitude},${gig.longitude} (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            } else if (gig.geoPoint != null) {
                val uri = "http://maps.google.com/maps?q=loc:${gig.geoPoint!!.latitude},${gig.geoPoint!!.longitude} (Gig Location)"
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
            navigate(
                    R.id.gigDetailsFragment, bundleOf(
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
                popupMenu.menu.findItem(R.id.action_decline_gig).setVisible(status == GigStatus.UPCOMING)
                popupMenu.menu.findItem(R.id.action_feedback).setVisible(status == GigStatus.COMPLETED)
            }

            popupMenu.setOnMenuItemClickListener(this@GigPage2Fragment)
            popupMenu.show()
        }

        checkInCheckOutSliderBtn?.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {
                        val gig = viewModel.currentGig ?: return

                        if (isNecessaryPermissionGranted()) {

                            if (!gig.isCheckInAndCheckOutMarked()) {
                                if (imageClickedPath != null) {

                                    if (location == null) {
                                        showAlertDialog("Please wait while your current location is captured")
                                    } else {
                                        checkForLateOrEarlyCheckIn()
                                    }
                                } else {
                                    startCameraForCapturingSelfie()
                                }
                            } else {
                                //Start regularisation
                                startRegularisation()
                            }
                        } else {
                            checkInCheckOutSliderBtn?.resetSlider()
                            showPermissionRequiredAndTheirReasonsDialog()
                        }
                    }
                }
    }

    override fun onResume() {
        super.onResume()

        if (isRequestingLocation) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates.setLocationUpdateCallbacks(this)
        isRequestingLocation = true
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

        val locationPhysicalAddress = if(location != null) {
            LocationUtils.getPhysicalAddressFromLocation(
                    context = requireContext(),
                    latitude = location!!.latitude,
                    longitude = location!!.longitude
            )
        } else {
            ""
        }

        viewModel.markAttendance(
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                locationPhysicalAddress = locationPhysicalAddress,
                image = imageClickedPath!!,
                checkInTimeAccToUser = checkInTimeAccToUser,
                remarks = "test"
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

                    when (it) {
                        Lce.Loading -> {
                        }
                        is Lce.Content -> {

                            if (it.content == AttendanceType.CHECK_OUT) {
                                showToast("Checkout Marked.")
                                showFeedbackBottomSheet()
                            } else {
                                showToast("Check-in marked")
                            }
                        }
                        is Lce.Error -> {
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
        gig_ellipses_iv.isVisible = status == GigStatus.COMPLETED ||
                status == GigStatus.UPCOMING
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

        checkInCheckOutSliderBtn.visible()
        checkInCheckOutSliderBtn.text = if (!gig.isCheckInMarked()) {
            "Check-in"
        } else if (gig.isCheckInMarked()) {
            "Check-out"
        } else {
            "Regularise"
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

        userFeedbackRatingBar.rating = gig.gigRating
    }

    private fun showOtherOptions(gig: Gig) {
        val status = GigStatus.fromGig(gig)

        val optionList = if (status == GigStatus.UPCOMING) {
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
                navigate(R.id.giger_id_fragment, Bundle().apply {
                    this.putString(
                            INTENT_EXTRA_GIG_ID,
                            viewModel.currentGig?.gigId
                    )
                })
            }
            ID_ATTENDANCE_HISTORY -> {
                val gig = viewModel.currentGig ?: return

                val currentDate = LocalDate.now()
                navigate(
                        R.id.gigMonthlyAttendanceFragment, bundleOf(
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_SELECTED_DATE to LocalDate.of(
                                currentDate.year,
                                currentDate.monthValue,
                                1
                        ),
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_LOGO to gig.getFullCompanyLogo(),
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_NAME to gig.getFullCompanyName(),
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_GIG_ORDER_ID to gig.gigOrderId,
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_ROLE to gig.getGigTitle()
                )
                )
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

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LocationUpdates.REQUEST_PERMISSIONS_REQUEST_CODE -> if (
                    PermissionUtils.permissionsGrantedCheck(grantResults)
            ) {
                locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
            }
            REQUEST_PERMISSIONS -> {

                var allPermsGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermsGranted = false
                        break
                    }
                }

                if (allPermsGranted) {
                    startCameraForCapturingSelfie()
                } else {
                    showToast("Please grant all permissions")
                }
            }
        }
    }

    private fun startCameraForCapturingSelfie() {
        val intent = Intent(context, ImageCaptureActivity::class.java)
        startActivityForResult(
                intent,
                REQUEST_CODE_UPLOAD_SELFIE_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkInCheckOutSliderBtn?.resetSlider()

        when (requestCode) {
            REQUEST_CODE_UPLOAD_SELFIE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageClickedPath = data?.getStringExtra("image_name")
                    checkForLateOrEarlyCheckIn()
                }
            }
            LocationUpdates.REQUEST_CHECK_SETTINGS -> if (resultCode == Activity.RESULT_OK) locationUpdates.startUpdates(
                    requireActivity() as AppCompatActivity
            )
            else -> {
            }
        }
    }


    override fun onPeopleToExpectClicked(option: ContactPerson) {
        navigate(
                R.id.gigContactPersonBottomSheet, bundleOf(
                GigContactPersonBottomSheet.INTENT_GIG_CONTACT_PERSON_DETAILS to option
        )
        )
    }

    override fun onCallManagerClicked(manager: ContactPerson) {
        manager.contactNumber?.let {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.toString(), null))
            startActivity(intent)
        }
    }

    override fun onChatWithManagerClicked(manager: ContactPerson) {

        navigate(R.id.chatPageFragment, bundleOf(
                ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to manager.uid,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to manager.profilePicture,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to manager.name)
        )
    }

    override fun onPermissionOkayClicked() {
        askForRequiredPermissions()
    }

    private fun askForRequiredPermissions() {
        requestPermissions(
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS
        )
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
                currentTime.isBefore(gig.checkOutBeforeBufferTime.toLocalDateTime())) {
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
        locationUpdates.stopLocationUpdates()
    }

    override fun lastLocationReceiver(location: Location?) {}

    private fun isNecessaryPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        const val TAG = "Gig_page_2"
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"

        const val REQUEST_PERMISSIONS = 100
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333

        private const val ID_IDENTITY_CARD = "apodZsdEbx"
        private const val ID_ATTENDANCE_HISTORY = "TnovE9tzXl"
        private const val ID_DECLINE_GIG = "knnp4f4ZUi"

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