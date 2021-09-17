package com.gigforce.verification.mainverification.aadhardetail

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadharDetailInfoFragmentBinding
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI

class AadharDetailInfoFragment : Fragment(), VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 102
        private const val REQUEST_CAPTURE_IMAGE = 1001
        private const val REQUEST_PICK_IMAGE = 1002
    }

    private val viewModel: AadharDetailInfoViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private lateinit var adapter : AdhaarDetailViewPagerAdapter
    //parmanent address variables (state variables is common with current add)
    var statesList = arrayListOf<State>()
    var stateAdapter: ArrayAdapter<String>? = null
    var statesesMap = mutableMapOf<String, Int>()
    var statesArray = arrayListOf<String>()

    var citiesList = arrayListOf<City>()
    var citiesAdapter: ArrayAdapter<String>? = null
    var citiesMap = mutableMapOf<String, Int>()
    var citiesArray = arrayListOf<String>()
    var selectedCity = City()

    private lateinit var viewBinding: AadharDetailInfoFragmentBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadharDetailInfoFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        viewModel.getStates()


        val frontUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.ic_aadhar_front))
                .appendPath(resources.getResourceTypeName(R.drawable.ic_aadhar_front))
                .appendPath(resources.getResourceEntryName(R.drawable.ic_aadhar_front))
                .build()
        val backUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.ic_aadhar_front))
                .appendPath(resources.getResourceTypeName(R.drawable.ic_aadhar_front))
                .appendPath(resources.getResourceEntryName(R.drawable.ic_aadhar_front))
                .build()
        val list = listOf(
                KYCImageModel(
                        text = getString(R.string.upload_aadhar_card_front_side_client),
                        imageIcon = frontUri,
                        imageUploaded = false
                ),
                KYCImageModel(
                        text = getString(R.string.upload_aadhar_card_back_side_client),
                        imageIcon = backUri,
                        imageUploaded = false
                )
        )
        setImageViewPager(list)
    }

    fun setImageViewPager(list: List<KYCImageModel>) = viewBinding.apply {

        if (list.isEmpty()) {
            viewPager2.gone()
            tabLayout.gone()
        } else {
            viewPager2.visible()
            tabLayout.visible()
            adapter = AdhaarDetailViewPagerAdapter {
                checkForPermissionElseShowCameraGalleryBottomSheet()
            }
            adapter.setItem(list)
            viewPager2.adapter = adapter
            if (list.size == 1) {
                tabLayout.gone()
            }
            Log.d("adapter", "" + adapter.itemCount + " list: " + list.toString())
            TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            }.attach()
        }

    }

    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions()) {
            VerificationClickOrSelectImageBottomSheet.launch(
                    parentFragmentManager,
                    getString(R.string.upload_pan_card_veri),
                    this
            )
        } else
            requestStoragePermission()
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestStoragePermission() {

        requestPermissions(
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
        )
    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                startCropImage(outputFileUri)
                Log.d("image", outputFileUri.toString())
            }
        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
            Log.d("ImageUri", imageUriResultCrop.toString())
            clickedImagePath = imageUriResultCrop
            showPanInfoCard(clickedImagePath!!)
        }
    }

    private fun startCropImage(imageUri: Uri): Unit {
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)

    }

    private fun showPanInfoCard(panInfoPath: Uri) {
        //call ocr api
        activeLoader(true)
        callKycOcrApi(panInfoPath)
    }

    private fun activeLoader(activate: Boolean) {
        if (activate) {
            viewBinding.progressBar.visible()
            viewBinding.submitButton.isEnabled = false
        } else {
            viewBinding.progressBar.gone()
            viewBinding.submitButton.isEnabled = true
        }
    }

    private fun callKycOcrApi(path: Uri) {

        var image: MultipartBody.Part? = null
        if (path != null) {
            val file = File(URI(path.toString()))
            Log.d("Register", "Nombre del archivo " + file.name)
            // create RequestBody instance from file
            val requestFile: RequestBody =
                    RequestBody.create(MediaType.parse("image/png"), file)
            // MultipartBody.Part is used to send also the actual file name
            image =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
        }
        image?.let {
            viewModel.getKycOcrResult("adhaar", if(viewBinding.viewPager2.currentItem == 0) "front" else "back" , it)
        }
    }
}