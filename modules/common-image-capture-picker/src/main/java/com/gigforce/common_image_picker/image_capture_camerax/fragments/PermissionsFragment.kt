package com.gigforce.common_image_picker.image_capture_camerax.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewModel

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

/**
 * This [Fragment] requests permissions and, once granted, it will navigate to the next fragment
 */
class PermissionsFragment : Fragment() {

    private val sharedCameraViewModel: CaptureImageSharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(CaptureImageSharedViewModel::class.java)
    }

    private lateinit var permissionListTV : TextView
    private lateinit var cameraPermissionBtn : View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera_permission, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasPermissions(requireContext())) {
            // If permissions have already been granted, proceed
            sharedCameraViewModel.allPermissionGranted()
        } else {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionListTV = view.findViewById(R.id.camera_permission_list_textview)
        val permissionList =  hashMapOf(
            "CAMERA" to "To Click Image for CheckIn"
        )

        permissionListTV.text = buildSpannedString {
            permissionList.forEach {

                append(it.key)
                append(" - ")
                append(it.value)
                append("\n")
            }
        }

        view.findViewById<View>(R.id.camera_permission_okay_button).setOnClickListener {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Takes the user to the success fragment when permission is granted
                sharedCameraViewModel.allPermissionGranted()
            } else {
                Toast.makeText(context, getString(R.string.permission_denied_common), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        val TAG = PermissionsFragment::class.java.simpleName

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun getInstance(): PermissionsFragment = PermissionsFragment()
    }
}
