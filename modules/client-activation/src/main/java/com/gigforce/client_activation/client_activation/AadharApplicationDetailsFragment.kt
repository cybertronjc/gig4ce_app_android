package com.gigforce.client_activation.client_activation

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.gigforce.client_activation.R
import com.gigforce.client_activation.databinding.AadharApplicationDetailsFragmentBinding
import com.gigforce.client_activation.ui.ClientActivationClickOrSelectImageBottomSheet
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.GlideApp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AadharApplicationDetailsFragment : Fragment(), IOnBackPressedOverride, ClientActivationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        fun newInstance() = AadharApplicationDetailsFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_STATE = "state"
        const val INTENT_EXTRA_AADHAR_NO = "aadhar_no"
        private const val REQUEST_CAPTURE_IMAGE = 1011
        private const val REQUEST_PICK_IMAGE = 1012

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig

    private val viewModel: AadharApplicationDetailsViewModel by viewModels()
    private lateinit var viewBinding: AadharApplicationDetailsFragmentBinding
    private var aadharFrontImagePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadharApplicationDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //getDataFromIntents(savedInstanceState)
        //initviews()
        setViews()
        listeners()
        observer()

    }

    private fun observer() {

    }

    private fun listeners() = viewBinding.apply {


        aadharCardFrontImg.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }

        dateOfBirthLabel.setOnClickListener {
            dateOfBirthPicker.show()
        }

        appBarAadhar.apply {
            setBackButtonListener(View.OnClickListener {
//                navigation.popBackStack()
                activity?.onBackPressed()
            })
        }

        submitButton.setOnClickListener {


            if (aadharNo.editText?.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter aadhaar number")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (fatherNameTil.editText?.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter father name")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (dateOfBirth.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Select date of birth")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (stateSpinner.text.equals("Select State")) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.select_aadhar_state))
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (citySpinner.text.equals("Select City")) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Select City")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }
            if (addLine1Input.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter Address Line 1")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (addLine2Input.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter Address Line 2")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (pincodeInput.text.toString().isBlank() || pincodeInput.text.toString().length == 6) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter valid pincode")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (landmarkInput.text.toString().isBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter Landmark")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

        }
    }

    private fun getDDMMYYYYFormat(str: String): String {
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val date = sdf.parse(str)
            if (Date().after(date)) {
                return ""
            }
            return DateHelper.getDateInDDMMYYYY(date)
        } catch (e: Exception) {
            return ""
        }
    }

    private fun setViews() {

    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            ClientActivationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                "Upload Driving License",
                this
            )
        else
            requestStoragePermission()
    }

    private val dateOfBirthPicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.dateOfBirth.text = DateHelper.getDateInDDMMYYYYHiphen(newCal.time)
                viewBinding.dateOfBirthLabel.visible()
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {

                var allPermsGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermsGranted = false
                        break
                    }
                }

                if (allPermsGranted)
                    ClientActivationClickOrSelectImageBottomSheet.launch(
                        parentFragmentManager,
                        "Upload Driving License",
                        this
                    )
                else {
                    showToast("Please grant storage permission")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                startCrop(outputFileUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            Log.d("ImageUri", imageUriResultCrop.toString())

                aadharFrontImagePath = imageUriResultCrop
                showFrontAadhar(aadharFrontImagePath!!)


            val baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                val bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            }
        }
    }

    private fun startCrop(uri: Uri): Unit {
        Log.v("Start Crop", "started")
        //can use this for a new name every time
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"
        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(requireContext().cacheDir, imageFileName + EXTENSION))
        )
        val resultIntent: Intent = Intent()
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        val size = getImageDimensions(uri)
        uCrop.withAspectRatio(size.width.toFloat(), size.height.toFloat())
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(requireContext(), this)
    }

    private fun getImageDimensions(uri: Uri): Size {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        return Size(imageWidth, imageHeight)
    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
//        options.setMaxBitmapSize(1000)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(true)
        options.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarTitle("Crop or rotate")
        return options
    }

    private fun showFrontAadhar(aadharFrontImagePath: Uri) {
        context?.let {
            GlideApp.with(it)
                .load(aadharFrontImagePath)
                .into(viewBinding.aadharCardFrontImg)
        }
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

    override fun onBackPressed(): Boolean {

        return true
    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }


}