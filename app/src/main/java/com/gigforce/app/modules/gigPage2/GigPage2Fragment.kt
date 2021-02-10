package com.gigforce.app.modules.gigPage2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.*
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.*
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.gigPage.models.GigPeopleToExpect
import com.gigforce.app.modules.gigPage2.adapters.GigPeopleToExpectAdapter
import com.gigforce.app.modules.gigPage2.adapters.GigPeopleToExpectAdapterClickListener
import com.gigforce.app.modules.gigPage2.adapters.OtherOptionClickListener
import com.gigforce.app.modules.gigPage2.adapters.OtherOptionsAdapter
import com.gigforce.app.modules.gigPage2.models.OtherOption
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.gigforce.app.utils.VerticalItemDecorator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_2.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_address.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_completiton_payment_details.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_gig_type.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_info.gig_address_tv
import kotlinx.android.synthetic.main.fragment_gig_page_2_main.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_other_options.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_payment_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_people_to_expect.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_timer_layout.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_toolbar.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class GigPage2Fragment : BaseFragment(),
        OtherOptionClickListener,
        PopupMenu.OnMenuItemClickListener,
        DeclineGigDialogFragmentResultListener,
        GigPeopleToExpectAdapterClickListener,
        PermissionRequiredBottomSheet.PermissionBottomSheetActionListener {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String
    private var location: Location? = null
    private var imageClickedPath: String? = null

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

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
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
    }

    private fun initUi() {

        details_label.setOnClickListener {
            navigate(
                    R.id.gigDetailsFragment, bundleOf(
                    GigDetailsFragment.INTENT_EXTRA_GIG_ID to viewModel.currentGig?.gigId
            )
            )
        }

        people_to_expect_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        people_to_expect_rv.addItemDecoration(VerticalItemDecorator(30))
        people_to_expect_rv.adapter = peopleToExpectAdapter

        gig_attendance.setOnClickListener {
            val gig = viewModel.currentGig ?: return@setOnClickListener
            val gigStartEndTime = gig.startDateTime!!.toLocalDateTime()

            if (gig.isMonthlyGig) {
                navigate(
                        R.id.gigMonthlyAttendanceFragment, bundleOf(
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_SELECTED_DATE to LocalDate.of(
                                gigStartEndTime.year,
                                gigStartEndTime.monthValue,
                                1
                        ),
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_LOGO to gig.companyLogo,
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_COMPANY_NAME to gig.companyName,
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_ROLE to gig.title,
                        GigMonthlyAttendanceFragment.INTENT_EXTRA_RATING to gig.gigRating
                )
                )
            } else {
                navigate(
                        R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
                        GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to gig.gigId
                )
                )
            }
        }

        gig_page_timer_layout.setOnClickListener {
            val gig = viewModel.currentGig ?: return@setOnClickListener

            navigate(
                    R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
                    GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to gig.gigId
            )
            )
        }

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

        gig_ellipses_iv.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_gig_2, popupMenu.menu)

            viewModel.currentGig?.let {

                if (it.isPresentGig() || it.isPastGig()) {
                    popupMenu.menu.findItem(R.id.action_decline_gig).setVisible(false)
                } else {
                    popupMenu.menu.findItem(R.id.action_decline_gig).setVisible(true)
                }
            }

            popupMenu.setOnMenuItemClickListener(this@GigPage2Fragment)
            popupMenu.show()
        }
        listener()
    }


    private fun listener() {
        checkInCheckOutSliderBtn?.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {

            override fun onSlideComplete(view: SlideToActView) {

                if (isNecessaryPermissionGranted()) {

                    if (checkInCheckOutSliderBtn.text == "CheckIn" || checkInCheckOutSliderBtn.text == "CheckOut")
                        startCheckInOrCheckOutProcess()
                    else
                        if (checkInCheckOutSliderBtn.text == "CheckIn")
                            startRegularisation()
                } else {
                    showPermissionRequiredAndTheirReasonsDialog()
                }
            }
        }
    }

    private fun startRegularisation() {

    }

    private fun startCheckInOrCheckOutProcess(
            checkInTimeAccToUser: Timestamp = Timestamp.now()
    ) {

        if (location == null) {
            showAlertDialog("No Location Found");
            return
        }

        if (imageClickedPath == null) {
            showAlertDialog("Image has not been captured");
            return
        }

        viewModel.markAttendance(
                latitude = location!!.latitude,
                longitude = location!!.longitude,
                image = imageClickedPath!!,
                checkInTimeAccToUser = checkInTimeAccToUser
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

        viewModel.watchGig(gigId)
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


        showCommonDetails(gig)

        if (gig.isPastGig())
            showPastGigDetails(gig)
        else if (gig.isPresentGig())
            showPresentGigDetails(gig)
        else if (gig.isUpcomingGig())
            showFutureGigDetails(gig)
        else {
//            FirebaseCrashlytics.getInstance().apply {
//                log("Gig did not qualify for any criteria")
//            }
        }
    }

    private fun showCommonDetails(gig: Gig) {

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                        .load(gig.companyLogo)
                        .placeholder(getCircularProgressDrawable())
                        .into(company_logo_iv)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                        .getReference("companies_gigs_images")
                        .child(gig.companyLogo!!)

                GlideApp.with(requireContext())
                        .load(imageRef)
                        .placeholder(getCircularProgressDrawable())
                        .into(company_logo_iv)
            }
        } else {
            val companyInitials = if (gig.companyName.isNullOrBlank())
                "C"
            else
                gig.companyName!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            company_logo_iv.setImageDrawable(drawable)
        }

        gig_title_tv.text = gig.title
        gig_company_name_tv.text = "@ ${gig.companyName}"

        gig_type.text = if (gig.isMonthlyGig) ": Monthly" else ": Daily"

        if (gig.endDateTime != null) {
            val startDate = gig.startDateTime!!.toLocalDate()
            val endDate = gig.endDateTime!!.toLocalDate()

            if (startDate.isEqual(endDate))
                gig_duration.text = ": ${dateFormatter.format(gig.startDateTime!!.toDate())}"
            else
                gig_duration.text =
                        ": ${dateFormatter.format(gig.startDateTime!!.toDate())} - ${
                            dateFormatter.format(
                                    gig.endDateTime!!.toDate()
                            )
                        }"
        } else {
            gig_duration.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
        }

        image_view.isVisible = gig.latitude != null && gig.latitude != 0.0
        gig_address_tv.text = gig.address

        if (gig.contactPersons.isNotEmpty()) {
            people_to_expect_layout.visible()
            divider_below_people_to_expect.visible()

            peopleToExpectAdapter.updatePeopleToExpect(gig.contactPersons)
        } else {
            people_to_expect_layout.gone()
            divider_below_people_to_expect.gone()
        }


        if (gig.isMonthlyGig) {
            gross_wage_label.text = "Gross payment : "
            gross_wage.text =
                    if (gig.gigAmount == 0.0) "As per contract" else "Rs ${gig.gigAmount}/mo"
        } else {
            gross_wage_label.text = "Gross wage per hour : "
            gross_wage.text =
                    if (gig.gigAmount == 0.0) "As per contract" else "Rs ${gig.gigAmount}/hr"
        }

        showOtherOptions()
    }

    private fun showOtherOptions() {

        val optionList = listOf(
                IDENTITY_CARD,
                ATTENDANCE_HISTORY,
                DECLINE_GIG
        )

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

    private fun showPastGigDetails(gig: Gig) {

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                        .load(gig.companyLogo)
                        .placeholder(getCircularProgressDrawable())
                        .into(company_logo_iv)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                        .getReference("companies_gigs_images")
                        .child(gig.companyLogo!!)

                GlideApp.with(requireContext())
                        .load(imageRef)
                        .placeholder(getCircularProgressDrawable())
                        .into(company_logo_iv)
            }
        } else {
            val companyInitials = if (gig.companyName.isNullOrBlank())
                "C"
            else
                gig.companyName!![0].toString().toUpperCase()
            val drawable =
                    TextDrawable.builder().beginConfig().textColor(R.color.colorPrimary).endConfig()
                            .buildRound(
                                    companyInitials,
                                    ResourcesCompat.getColor(resources, R.color.white, null)
                            )

            company_logo_iv.setImageDrawable(drawable)
        }

        checkInCheckOutSliderBtn?.gone()
        gig_checkin_time_tv.gone()
        gig_page_completiton_layout.visible()

        if (gig.isCheckInAndCheckOutMarked()) {
            gig_page_completiton_layout.visible()
            amount_tv.text = "${gig.gigAmount} Rs"
        } else {
            gig_page_completiton_layout.gone()
        }

        gig_page_top_bar.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_pink,
                        null
                )
        )
        gig_page_timer_layout.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_pink_light,
                        null
                )
        )

        gig_date_tv.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
        gig_status_tv.text = "Completed"
        gig_status_iv.setImageResource(R.drawable.round_green)

        if (gig.isCheckInAndCheckOutMarked()) {
            val checkInTime = gig.attendance!!.checkInTime!!
            val checkOutTime = gig.attendance!!.checkOutTime!!

            val diffInMillisec: Long = checkOutTime.time - checkInTime.time
            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins"
            val checkoutTime = gig.attendance!!.checkOutTime
            gig_checkin_time_tv.text = "${timeFormatter.format(checkInTime)} - ${
                timeFormatter.format(checkoutTime)
            }"
        } else if (gig.isCheckInMarked()) {
            val gigStartDateTime = gig.startDateTime!!.toDate()
            val currentTime = Date().time

            val diffInMillisec: Long = currentTime - gigStartDateTime.time
            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = "No check-out marked"
            gig_checkin_time_tv.text = "${timeFormatter.format(gigStartDateTime)} -"
        } else {
            gig_timer_tv.text = "No Check-in"
            gig_checkin_time_tv.gone()
        }
    }

    private fun showPresentGigDetails(gig: Gig) {
        gig_page_completiton_layout.gone()

        if (gig.isCheckInAndCheckOutMarked()) {
            //Attendance have been marked show it
            checkInCheckOutSliderBtn?.gone()
        } else {
            //Show Check In Controls
            checkInCheckOutSliderBtn?.visible()

            if (!gig.isCheckInMarked()) {
                checkInCheckOutSliderBtn?.let {
                    if (it.isCompleted()) {
                        it.resetSlider()
                    }
                }
                checkInCheckOutSliderBtn?.text = "Check-in"
            } else if (!gig.isCheckOutMarked()) {
                checkInCheckOutSliderBtn?.let {
                    if (it.isCompleted()) {
                        it.resetSlider()
                    }
                }

                checkInCheckOutSliderBtn?.text = "Check-out"
            }
        }


        gig_page_top_bar.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_green,
                        null
                )
        )
        gig_page_timer_layout.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_green_light,
                        null
                )
        )

        gig_date_tv.text = "Today, ${dateFormatter.format(gig.startDateTime!!.toDate())}"
        gig_status_tv.text = "Ongoing"
        gig_status_iv.setImageResource(R.drawable.round_green)

        if (gig.isCheckInMarked()) {
            val gigStartDateTime = gig.attendance?.checkInTime!!
            updateCurrentGigTimer(gigStartDateTime)
            startCountUpTimer()
            gig_checkin_time_tv.text = "Since ${timeFormatter.format(gigStartDateTime)}, Today"
        } else {
            gig_timer_tv.text = "No Checkin yet"
            gig_checkin_time_tv.gone()
        }
    }

    private var countUpTimer: CountUpTimer? = null
    private fun startCountUpTimer() {

        if (countUpTimer == null) {
            countUpTimer = object : CountUpTimer(3000000) {

                override fun onTick(second: Int) {
                    val startDateTime = viewModel.currentGig?.attendance?.checkInTime

                    if (startDateTime != null)
                        updateCurrentGigTimer(startDateTime)
                }

            }
            countUpTimer?.start()
        }
    }

    private fun updateCurrentGigTimer(gigStartDateTime: Date) {
        val currentTime = Date().time
        val diffInMillisec: Long = currentTime - gigStartDateTime.time

        Log.d("Timer", "Time Diff : $diffInMillisec")

        val diffInHours: Long = if (diffInMillisec > 3600000)
            TimeUnit.MILLISECONDS.toHours(diffInMillisec)
        else
            0L
        val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60
        val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec) % 60

        gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins : $diffInSec Sec"
    }

    private fun showFutureGigDetails(gig: Gig) {
        checkInCheckOutSliderBtn?.gone()
        gig_page_completiton_layout.gone()
        gig_checkin_time_tv.gone()

        gig_status_iv.setImageResource(R.drawable.round_yellow)
        gig_status_tv.text = "Pending"

        gig_page_top_bar.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_orange,
                        null
                )
        )
        gig_page_timer_layout.setBackgroundColor(
                ResourcesCompat.getColor(
                        resources,
                        R.color.gig_orange_light,
                        null
                )
        )

        gig_date_tv.text = "${dateFormatter.format(gig.startDateTime!!.toDate())}"
        val gigStartDateTime = gig.startDateTime!!.toDate()
        val currentTime = Date().time

        val diffInMillisec: Long = gigStartDateTime.time - currentTime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)

        if (diffInHours > 24) {
            val days = diffInHours / 24
            val hours = diffInHours % 24

            gig_timer_tv.text = buildSpannedString {
                scale(0.7f) {
                    append("Starts in ")
                }
                append("$days Days : $hours Hrs")
            }

        } else {
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

            gig_timer_tv.text = buildSpannedString {
                scale(0.7f) {
                    append("Starts in ")
                }
                append("$diffInHours Hrs : $diffInMin Mins")
            }

            gig_checkin_time_tv.text = null
        }
    }

    override fun onOptionClicked(option: OtherOption) {

        when (option.id) {
            ID_IDENTITY_CARD -> {
                navigate(R.id.giger_id_fragment, Bundle().apply {
                    this.putString(
                            GigPageFragment.INTENT_EXTRA_GIG_ID,
                            viewModel.currentGig?.gigId
                    )
                })
            }
            ID_ATTENDANCE_HISTORY -> {
                //show attendance History
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
                navigate(R.id.contactScreenFragment)
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

                if (viewModel.currentGig!!.startDateTime!!.toLocalDateTime() < LocalDateTime.now()) {
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
        activity?.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        countUpTimer?.cancel()
        countUpTimer = null
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGPSRequestCompleted = true
                    initializeGPS()
                } else {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkInCheckOutSliderBtn?.resetSlider()
        if (resultCode == Activity.RESULT_OK && requestCode == GigAttendancePageFragment.REQUEST_CODE_UPLOAD_SELFIE_IMAGE) {
            if (data != null) {
                selfieImg = data.getStringExtra("image_name")
            }
            checkAndUpdateAttendance()
        }
    }


    override fun onPeopleToExpectClicked(option: GigPeopleToExpect) {
        navigate(
                R.id.gigContactPersonBottomSheet, bundleOf(
                GigContactPersonBottomSheet.INTENT_GIG_CONTACT_PERSON_DETAILS to option
        )
        )
    }

    override fun onCallManagerClicked(manager: GigPeopleToExpect) {
        TODO("Not yet implemented")
    }

    override fun onChatWithManagerClicked(manager: GigPeopleToExpect) {
        TODO("Not yet implemented")
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
                icon = R.drawable.ic_clothes
        )

        private val ATTENDANCE_HISTORY = OtherOption(
                id = ID_ATTENDANCE_HISTORY,
                name = "Attendance History",
                icon = R.drawable.ic_compensation
        )

        private val DECLINE_GIG = OtherOption(
                id = ID_DECLINE_GIG,
                name = "Decline Gig",
                icon = R.drawable.ic_id_card
        )

        private val PERMISSION_AND_REASONS: HashMap<String, String> = hashMapOf(
                "LOCATION" to "To Capture Location For CheckIn",
                "CAMERA" to "To Click Image for CheckIn",
                "STORAGE" to "To Store Image captured while CheckIn"
        )
    }
}