package com.gigforce.client_activation.client_activation.info.pf_esic

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.AadharApplicationDetailsFragment
import com.gigforce.client_activation.databinding.AadharApplicationDetailsFragmentBinding
import com.gigforce.client_activation.databinding.PfesicFormDetailsFragmentBinding
import com.gigforce.client_activation.ui.ClientActivationClickOrSelectImageBottomSheet
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.profile.PFESICDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.aadhar_application_details_fragment.*
import kotlinx.android.synthetic.main.pfesic_form_details_fragment.*
import kotlinx.android.synthetic.main.pfesic_form_details_fragment.progressBar
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PFESICFormDetailsFragment : Fragment(), IOnBackPressedOverride,
    ClientActivationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener{

    companion object {
        fun newInstance() = PFESICFormDetailsFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR = 2331

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_CLICKED_IMAGE_BACK = "back_image"
        private const val REQUEST_CAPTURE_IMAGE = 1011
        private const val REQUEST_PICK_IMAGE = 1012

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    private val viewModel: PFESICFormDetailsViewModel by viewModels()
    private lateinit var viewBinding: PfesicFormDetailsFragmentBinding
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig

    private var win: Window? = null
    var signaturePath = ""
    var fileName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = PfesicFormDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //getDataFromIntents(savedInstanceState)
        //initviews()
        changeStatusBarColor()
        setViews()
        listeners()
        observer()


    }

    private fun observer() {

        viewModel.pfEsicSumbitResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val updated = it ?: return@Observer
            Log.d("updated", "up $updated")
            progressBar.visibility = View.GONE
            if (updated) {
                showToast("Data uploaded successfully")
            }
        })

        viewModel.pfEsicResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val pfEsicData = it ?: return@Observer
            Log.d("pfEsicData", "data : $pfEsicData")
            processpfEsicData(pfEsicData)
        })
    }

    private fun processpfEsicData(pfEsicData: PFESICDataModel) = viewBinding.apply {

        pfEsicData?.isAlreadyExists?.let {
            if (it == true){
                checkedLayout.visibility = View.VISIBLE
                uncheckedLayout.visibility = View.GONE
            } else {
                checkedLayout.visibility = View.GONE
                uncheckedLayout.visibility = View.VISIBLE
            }
        }

        pfEsicData?.esicNumber?.let {
            esicNumber.editText?.setText(it)
        }

         pfEsicData?.uanNumber?.let {
            uanNumber.editText?.setText(it)
        }

         pfEsicData?.pfNumber?.let {
            pfNumber.editText?.setText(it)
        }

         pfEsicData?.nomineeName?.let {
            nomineeName.editText?.setText(it)
        }

         pfEsicData?.rNomineeName?.let {
            relationNominee.editText?.setText(it)
        }

        pfEsicData?.dobNominee?.let {
            dateOfBirth?.setText(it)
        }

        pfEsicData?.signature?.let {
            signaturePath = it
            getDBImageUrl(it).let {
                showSignatureImage(it)
            }
        }
    }

    private fun showSignatureImage(signature: String?) {
        context?.let { Glide.with(it).load(signature).placeholder(ShimmerHelper.getShimmerDrawable()).into(viewBinding.signatureImage) }
    }

    private fun setViews() {

    }

    private fun listeners() = viewBinding.apply {

        dateOfBirthLabel.setOnClickListener {
            dateOfBirthPicker.show()
        }

        appBar.apply {
            setBackButtonListener(View.OnClickListener {
//                navigation.popBackStack()
                activity?.onBackPressed()
            })
        }

        pfesicCheckbox.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkedLayout.visibility = View.VISIBLE
                uncheckedLayout.visibility = View.GONE
            } else {
                checkedLayout.visibility = View.GONE
                uncheckedLayout.visibility = View.VISIBLE
            }
        }

        submitButton.setOnClickListener {
            if (pfesicCheckbox.isChecked){
                if (esicNumber.editText?.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter ESIC number")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (uanNumber.editText?.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter UAN number")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (pfNumber.editText?.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter PF number")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                submitData()
            } else {
                 if (nomineeName.editText?.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter nominee name")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (relationNominee.editText?.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter relation with nominee")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (dateOfBirth.text?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Select DoB of nominee")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                submitData()
            }
        }

        signatureImage.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
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

    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions()) {

            ClientActivationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                "Upload Signature",
                this
            )

        } else
            requestStoragePermission()
    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return buildConfig.getStorageBaseUrl() + modifiedString
            } catch (egetDBImageUrl: Exception) {
                return null
            }
        }
        return null
    }

    private fun submitData() = viewBinding.apply{

        //creating datamodel for data submission
        val pfesicDataModel = PFESICDataModel(
            esicNumber = esicNumber.editText?.text?.toString(),
            uanNumber = uanNumber.editText?.text?.toString(),
            pfNumber = pfNumber.editText?.text?.toString(),
            nomineeName = nomineeName.editText?.text?.toString(),
            rNomineeName = relationNominee.editText?.text?.toString(),
            dobNominee = dateOfBirth.text.toString(),
            signature = signaturePath
        )

        viewModel.setDetails(pfesicDataModel)
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
                viewBinding.dobLabel.visible()
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }


    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        win?.statusBarColor = resources.getColor(R.color.stateBarColor)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                startCropImage(outputFileUri)
            }


        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
            imageUriResultCrop?.let {
                progressBar.visible()
                firebaseStorage.reference
                    .child("profile_pics")
                    .child(fileName)
                    .putFile(it).addOnSuccessListener {
                        progressBar.gone()
                        if (imageUriResultCrop != null) {
                            signaturePath = it.metadata?.path.toString()
                        }
                    }.addOnFailureListener {
                        progressBar.gone()
                    }.addOnCanceledListener { progressBar.gone() }
            }


        }
    }

    private fun startCropImage(imageUri: Uri): Unit {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"
        fileName = imageFileName + EXTENSION
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    override fun onBackPressed(): Boolean {
        TODO("Not yet implemented")
    }

}