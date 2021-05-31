package com.gigforce.app.modules.markattendance

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.common_ui.ext.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Mode
import kotlinx.android.synthetic.main.mark_attendance_fragment.*

class MarkAttendanceFragment : Fragment() {
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    val PERMISSION_FINE_LOCATION = 100

    companion object {
        fun newInstance() = MarkAttendanceFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mark_attendance_fragment, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
//        updateGPS()
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(CameraListener())
        listener()
    }
    var mCaptureTime :Long = 0
    private fun listener() {
        button3.setOnClickListener(View.OnClickListener {
            if (cameraView.getMode() == Mode.VIDEO) {
                showToast("Can't take HQ pictures while in VIDEO mode.")

            }else {
                if (!cameraView.isTakingPicture()) {
                    mCaptureTime = System.currentTimeMillis()
                    showToast("Capturing picture...")
                    cameraView.takePicture()
                }
            }
        })


    }

    private inner class CameraListener : com.otaliastudios.cameraview.CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)

        }
    }

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                updateUI(it)
            }
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),PERMISSION_FINE_LOCATION)
            }
        }
    }
    private fun updateUI(location: Location) {
        lat.text = location.latitude.toString()
        log.text = location.longitude.toString()
        var geocoder = Geocoder(requireContext())
        var locationAddress = ""
        try {
            var addressArr =  geocoder.getFromLocation(location.latitude,location.longitude,1)
            address.text = addressArr.get(0).getAddressLine(0)
            locationAddress = addressArr.get(0).getAddressLine(0)
        }catch (e: Exception){
            address.text = "Not Working"
        }
//        var gigsRepositoryTest = GigsRepositoryTest()
//        var markAttendance = MarkAttendance(true, Date(),location.latitude,location.longitude,"",locationAddress)
//        gigsRepositoryTest.markAttendance(markAttendance)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_FINE_LOCATION -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS()
                }
                else{
                    showToast("This app require GPS permission to work properly")
                }
            }
        }
    }

}