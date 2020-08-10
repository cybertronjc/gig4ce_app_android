package com.gigforce.app.modules.gigPage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.modules.markattendance.ImageCaptureActivity
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import java.text.SimpleDateFormat
import java.util.*


class GigAttendancePageFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
    }

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_FINE_LOCATION = 100
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm aa", Locale.getDefault())

    private val viewModel: GigViewModel by viewModels()

    private lateinit var gigId: String
    private var gig: Gig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_attendance, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel(savedInstanceState)

        requestPermissionForGPS()
        listener()
    }

    private fun listener() {
        startNavigationSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        var intent = Intent(context, ImageCaptureActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_UPLOAD_SELFIE_IMAGE)

//                        camera_cl.visible()
                    } else {
                        requestPermissionForGPS()
                        startNavigationSliderBtn.resetSlider()
                    }
                }
            }

    }

    private fun initView() {
        cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        ellipses_btn.setOnClickListener {

            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_gig_attendance, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this@GigAttendancePageFragment)
            popupMenu.show()
        }

        seeMoreBtn.setOnClickListener {

            navigate(R.id.presentGigPageFragment, Bundle().apply {
                this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, gigId)
                this.putBoolean(GigPageFragment.INTENT_EXTRA_COMING_FROM_CHECK_IN, true)
            })
        }

        messageCardView.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        favoriteCB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && gig?.isFavourite!!.not()) {
                viewModel.favoriteGig(gigId)
                showToast("Marked As Favourite")
            } else if (!isChecked && gig?.isFavourite!!) {
                viewModel.unFavoriteGig(gigId)
                showToast("Unmarked As Favourite")
            }
        }

        callCardView.setOnClickListener {

            if (gig?.gigContactDetails?.contactNumber != 0L) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.gigContactDetails?.contactNumber?.toString(), null))
                startActivity(intent)
            }
        }
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {
        gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(INTENT_EXTRA_GIG_ID)!!
        } else {
            arguments?.getString(INTENT_EXTRA_GIG_ID)!!
        }
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> setGigDetailsOnView(it.content)
                    is Lce.Error -> {
                    }
                }
            })

        viewModel.watchGig(gigId)
    }

    private fun setGigDetailsOnView(gig: Gig) {
        this.gig = gig
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = "Gig Id : ${gig.gigId}"

        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                Glide.with(requireContext())
                    .load(gig.companyLogo)
                    .into(companyLogoIV)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(gig.companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->
                        Glide.with(requireContext())
                            .load(fileUri)
                            .into(companyLogoIV)
                    }
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

            companyLogoIV.setImageDrawable(drawable)
        }

        if (gig.endDateTime != null) {
            durationTextTV.text =
                "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"
            shiftTV.text =
                "${timeFormatter.format(gig.startDateTime!!.toDate())} - ${timeFormatter.format(gig.endDateTime!!.toDate())}"
        } else {
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "
            shiftTV.text = "${timeFormatter.format(gig.startDateTime!!.toDate())} - "
        }

        val gigAmountText = if (gig.gigAmount == 0.0)
            "--"
        else {
            if (gig.isMonthlyGig)
                "Gross Payment : Rs ${gig.gigAmount} per Month"
            else
                "Gross Payment : Rs ${gig.gigAmount} per Hour"
        }

        wageTV.text = gigAmountText
        addressTV.text = gig.address

        if (gig.isFavourite && favoriteCB.isChecked.not()) {
            favoriteCB.isChecked = true
        } else if (gig.isFavourite.not() && favoriteCB.isChecked) {
            favoriteCB.isChecked = false
        }

        contactPersonTV.text = gig.gigContactDetails?.contactName
        callCardView.isVisible = gig.gigContactDetails?.contactNumber != 0L

        addressTV.setOnClickListener {

            //Launch Map
            val lat = gig.latitude
            val long = gig.longitude

            if (lat != null) {
                val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        if (gig.latitude != null) {
            addressTV.text = prepareAddress(gig.address)
        } else {
            addressTV.text = gig.address
        }

        if (gig.isPresentGig()) {

            if (gig.isCheckInAndCheckOutMarked()) {
                //Attendance have been marked show it
                startNavigationSliderBtn.gone()

                if (gig.isCheckInMarked())
                    punchInTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkInTime!!)
                else
                    punchInTimeTV.text = "--:--"

                if (gig.isCheckOutMarked())
                    punchOutTimeTV.text =
                        timeFormatter.format(gig.attendance!!.checkOutTime!!)
                else
                    punchOutTimeTV.text = "--:--"
            } else {
                //Show Check In Controls
                startNavigationSliderBtn.visible()

                if (!gig.isCheckInMarked()) {

                    if (startNavigationSliderBtn.isCompleted()) {
                        startNavigationSliderBtn.resetSlider()
                    }

                    attendanceCardView.setBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_pink,
                            null
                        )
                    )

                    startNavigationSliderBtn.text = "Check In"
                } else if (!gig.isCheckOutMarked()) {
                    if (startNavigationSliderBtn.isCompleted()) {
                        startNavigationSliderBtn.resetSlider()
                    }

                    if (gig.isCheckInMarked())
                        punchInTimeTV.text =
                            timeFormatter.format(gig.attendance!!.checkInTime!!)
                    else
                        punchInTimeTV.text = "--:--"

                    startNavigationSliderBtn.text = "Check Out"
                }
            }
        } else if (gig.isPastGig()) {
            startNavigationSliderBtn.gone()

            if (gig.isCheckInMarked())
                punchInTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkInTime!!)
            else
                punchInTimeTV.text = "--:--"

            if (gig.isCheckOutMarked())
                punchOutTimeTV.text =
                    timeFormatter.format(gig.attendance!!.checkOutTime!!)
            else
                punchOutTimeTV.text = "--:--"

        } else {
            startNavigationSliderBtn.gone()
        }
    }

    private fun prepareAddress(address: String): SpannableString {
        if (address.isBlank())
            return SpannableString("")

        val string = SpannableString(address + GigPageFragment.TEXT_VIEW_ON_MAP)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), address.length + 1, string.length - 1, 0)
        return string
    }

    private fun initializeGPS() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    fun requestPermissionForGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_FINE_LOCATION
            )
        }
    }

    private fun checkAndUpdateAttendance() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!isGPSRequestCompleted) {
                initializeGPS()
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                updateAttendanceOnDBCall(it)
            }


        } else {
            requestPermissionForGPS()
        }

    }

    var selfieImg: String = ""

    fun updateAttendanceOnDBCall(location: Location) {
        var geocoder = Geocoder(requireContext())
        var locationAddress = ""
        try {
            var addressArr = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            locationAddress = addressArr.get(0).getAddressLine(0)
        } catch (e: java.lang.Exception) {
        }
        if (gig!!.attendance == null || !gig!!.attendance!!.checkInMarked) {
            var markAttendance =
                GigAttendance(
                    true,
                    Date(),
                    location.latitude,
                    location.longitude,
                    selfieImg,
                    locationAddress
                )
            viewModel.markAttendance(markAttendance, gigId)

        } else {
            gig!!.attendance!!.setCheckout(
                true, Date(), location.latitude,
                location.longitude, selfieImg,
                locationAddress
            )
            viewModel.markAttendance(gig!!.attendance!!, gigId)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startNavigationSliderBtn.resetSlider()
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_UPLOAD_SELFIE_IMAGE) {
            if (data != null)
                selfieImg = data.getStringExtra("image_name")
            checkAndUpdateAttendance()
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_UPLOAD_SELFIE_IMAGE && resultCode == Activity.RESULT_OK) {
//            selfieImg = data?.getStringExtra("filename")
//            var profilePicRef: StorageReference =
//                FirebaseStorage.getInstance().reference.child("attendance").child(imageName.toString())
//            selfieImg = profilePicRef.toString()
//            updateAttendanceToDB()
//            showToast(profilePicRef.toString())
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGPSRequestCompleted = true
                    initializeGPS()
                } else {
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item ?: return false

        when (item.itemId) {
            R.id.action_help -> {
                navigate(R.id.contactScreenFragment)
                return true
            }
            R.id.action_share -> {
                return true
            }
            else -> return false

        }
    }
}