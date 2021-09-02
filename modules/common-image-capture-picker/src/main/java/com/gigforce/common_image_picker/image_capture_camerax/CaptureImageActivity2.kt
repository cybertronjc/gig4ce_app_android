package com.gigforce.common_image_picker.image_capture_camerax

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.image_capture_camerax.fragments.CameraFragment
import com.gigforce.common_image_picker.image_capture_camerax.fragments.ImageViewerFragment
import com.gigforce.common_image_picker.image_capture_camerax.fragments.PermissionsFragment
import com.gigforce.core.base.BaseActivity
import java.io.File

class CameraActivity : BaseActivity() {

    private val sharedCameraViewModel: CaptureImageSharedViewModel by lazy {
        ViewModelProvider(this).get(CaptureImageSharedViewModel::class.java)
    }

    private lateinit var container: FrameLayout

    //Arguments
    private var destinationImagePath: File? = null
    private var shouldUploadToFirebaseStorageToo: Boolean = false
    private var parentDirectoryNameInFirebaseStorage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgumentsFrom(intent, savedInstanceState)

        setContentView(R.layout.activity_camera)
        container = findViewById(R.id.fragment_container)
        initViewModel()

        if (PermissionsFragment.hasPermissions(this)) {
            sharedCameraViewModel.allPermissionGranted()
        } else {
            loadPermissionFragment()
        }
    }

    private fun getArgumentsFrom(intent: Intent?, savedInstanceState: Bundle?) {
        intent?.let {

            val firebasePathString = it.getStringExtra(INTENT_EXTRA_DESTINATION_IMAGE_PATH)
            if (firebasePathString != null) {
                destinationImagePath = File(firebasePathString)
            }
            shouldUploadToFirebaseStorageToo =
                it.getBooleanExtra(INTENT_EXTRA_SHOULD_UPLOAD_TO_FIREBASE_TOO, false)
            parentDirectoryNameInFirebaseStorage =
                it.getStringExtra(INTENT_EXTRA_FIREBASE_STORAGE_PARENT_PATH)
        }

        savedInstanceState?.let {

            val firebasePathString = it.getString(INTENT_EXTRA_DESTINATION_IMAGE_PATH)
            if (firebasePathString != null) {
                destinationImagePath = File(firebasePathString)
            }
            shouldUploadToFirebaseStorageToo =
                it.getBoolean(INTENT_EXTRA_SHOULD_UPLOAD_TO_FIREBASE_TOO, false)
            parentDirectoryNameInFirebaseStorage =
                it.getString(INTENT_EXTRA_FIREBASE_STORAGE_PARENT_PATH)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_DESTINATION_IMAGE_PATH, destinationImagePath?.absolutePath)
        outState.putBoolean(
            INTENT_EXTRA_SHOULD_UPLOAD_TO_FIREBASE_TOO,
            shouldUploadToFirebaseStorageToo
        )
        outState.putString(
            INTENT_EXTRA_FIREBASE_STORAGE_PARENT_PATH,
            parentDirectoryNameInFirebaseStorage
        )
    }


    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    override fun onBackPressed() {
        val imageViewerFragment = supportFragmentManager.findFragmentByTag(ImageViewerFragment.TAG)
        if (imageViewerFragment != null) {
            replaceImagePreviewFragmentWithCameraViewFragment()
        } else {
            super.onBackPressed()
        }
    }

    private fun initViewModel() {

        sharedCameraViewModel
            .captureImageSharedViewModelState
            .observe(this, Observer {

                when (it) {
                    CaptureImageSharedViewState.CameraPermissionGranted -> {
                        //Open Camera Image preview
                        openCameraFragment()
                    }
                    is CaptureImageSharedViewState.CapturedImageApproved -> {

                        val intent = Intent()
                        intent.putExtra(INTENT_EXTRA_FINAL_IMAGE_URI, Uri.fromFile(it.image))
                        intent.putExtra(
                            INTENT_EXTRA_UPLOADED_PATH_IN_FIREBASE_STORAGE,
                            it.uploadedPathInFirebaseStorageIfUploaded
                        )
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                    CaptureImageSharedViewState.CapturedImageDiscarded -> {
                        replaceImagePreviewFragmentWithCameraViewFragment()
                    }
                    is CaptureImageSharedViewState.ImageCaptured -> {
                        showImagePreviewFragment(it.image)
                    }
                }
            })

    }

    private fun loadPermissionFragment() {

        val permissionFragment = PermissionsFragment.getInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.fragment_container,
            permissionFragment,
            PermissionsFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }


    private fun showImagePreviewFragment(image: File) {

        val imageViewerFragment = ImageViewerFragment.getInstance(
            file = image,
            shouldUploadToFirebaseStorageToo,
            parentDirectoryNameInFirebaseStorage
        )

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            imageViewerFragment,
            ImageViewerFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun replaceImagePreviewFragmentWithCameraViewFragment() {

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraFragment = CameraFragment.getInstance(
            cameraId = cameraManager.cameraIdList[1],
            pixelFormat = ImageFormat.JPEG
        )

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            cameraFragment,
            CameraFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun openCameraFragment() {

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty()) return

        if (cameraIdList.size > 1) {
            val cameraFragment = CameraFragment.getInstance(
                cameraId = cameraIdList[1],
                pixelFormat = ImageFormat.JPEG
            )

            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(
                R.id.fragment_container,
                cameraFragment,
                CameraFragment.TAG
            )
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.commit()
        }
    }

    companion object {
        /** Combination of all flags required to put activity into immersive mode */
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L

        const val REQUEST_CODE_CAPTURE_IMAGE_2 = 2323

        //Incoming Intents
        const val INTENT_EXTRA_DESTINATION_IMAGE_PATH = "dest_image_path"
        const val INTENT_EXTRA_SHOULD_UPLOAD_TO_FIREBASE_TOO = "shouldUploadToServerToo"
        const val INTENT_EXTRA_FIREBASE_STORAGE_PARENT_PATH = "serverParentPath"

        //Returning Intents
        const val INTENT_EXTRA_FINAL_IMAGE_URI = "final_image_uri"
        const val INTENT_EXTRA_UPLOADED_PATH_IN_FIREBASE_STORAGE = "firebase_storage"

        fun launch(
            fragment: Fragment,
            destImage: File? = null,
            shouldUploadToServerToo: Boolean = false,
            serverParentPath: String? = null
        ) {
            val intent = Intent(fragment.requireContext(), CameraActivity::class.java)
                .apply {
                    putExtra(INTENT_EXTRA_DESTINATION_IMAGE_PATH, destImage) //Serializable
                    putExtra(INTENT_EXTRA_SHOULD_UPLOAD_TO_FIREBASE_TOO, shouldUploadToServerToo)
                    putExtra(INTENT_EXTRA_FIREBASE_STORAGE_PARENT_PATH, serverParentPath)
                }

            fragment.startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE_2)
        }
    }
}
