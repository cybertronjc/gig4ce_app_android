package com.gigforce.app.modules.gigPage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.utils.Lce
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_navigation_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.callCardView
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.contactPersonTV
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.startNavigationSliderBtn
import java.text.SimpleDateFormat
import java.util.*


class GigAttendancePageFragment : BaseFragment() {
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    var isGPSRequestCompleted = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_FINE_LOCATION = 100
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormatter = SimpleDateFormat("HH:MM:SS")

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
                    updateAttendanceToDB()
                }
            }
    }

    private fun initView() {
        cross_btn.setOnClickListener { activity?.onBackPressed() }

        callCardView.setOnClickListener {

            if (gig != null) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.contactNo, null))
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
        gigIdTV.text = gig.gigId
        try {
            durationTextTV.text =
                "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"
        } catch (e: Exception) {

        }
        shiftTV.text = "${gig.duration} per Day "
        addressTV.text = gig.address
        wageTV.text = "${gig.gigAmount} per Day "

        contactPersonTV.text = gig.gigContactDetails?.contactName

        if (gig.gigLocationDetails != null) {
            //          fullMapAddresTV.text = gig.gigLocationDetails?.fullAddress
//            addMarkerOnMap(
//                latitude = gig.gigLocationDetails!!.latitude!!,
//                longitude = gig.gigLocationDetails!!.longitude!!
//            )
        } else {
            //make location layout invisivle
        }
        try {
            if (gig.attendance!!.checkInMarked) {
                startNavigationSliderBtn.text = "Check out"
                punchInTimeTV.text = "${timeFormatter.format(gig.attendance?.checkInTime)}"
            }
            if (gig.attendance!!.checkOutMarked) {
                startNavigationSliderBtn.gone()
                punchOutTimeTV.text = "${timeFormatter.format(gig.attendance?.checkOutTime)}"
            }
        }catch (e:Exception){}

    }


    private fun updateGPS() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionForGPS()
        }
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

    private fun updateAttendanceToDB() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!isGPSRequestCompleted) {
                updateGPS()
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                updateAttendanceOnDBCall(it)
            }

        } else {
            requestPermissionForGPS()
        }

    }

    fun updateAttendanceOnDBCall(location: Location) {
        var geocoder = Geocoder(requireContext())
        var locationAddress = ""
        try {
            var addressArr = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            locationAddress = addressArr.get(0).getAddressLine(0)
        } catch (e: java.lang.Exception) {
        }
        if (gig!!.attendance==null || !gig!!.attendance!!.checkInMarked) {
            var markAttendance =
                GigAttendance(
                    true,
                    Date(),
                    location.latitude,
                    location.longitude,
                    "",
                    locationAddress
                )
            viewModel.markAttendance(markAttendance, gigId)
        }
        else{
            gig!!.attendance!!.setCheckout(true,Date(),location.latitude,
                location.longitude,"",
                locationAddress)
            viewModel.markAttendance(gig!!.attendance!!, gigId)

        }

    }

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
                    updateGPS()
                } else {
                    showToast("This APP require GPS permission to work properly")
                }
            }
        }
    }
}