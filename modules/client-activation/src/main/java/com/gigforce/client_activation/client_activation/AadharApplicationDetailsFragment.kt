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
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.databinding.AadharApplicationDetailsFragmentBinding
import com.gigforce.client_activation.ui.ClientActivationClickOrSelectImageBottomSheet
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.verification.KYCdata
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.GlideApp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.powermenu.kotlin.showAsDropDown
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var aadharFrontImagePath: Uri? = null
    private var win: Window? = null
    var statesList = arrayListOf<State>()
    var citiesList = arrayListOf<City>()
    var arrayAdapter: ArrayAdapter<String>? = null
    var citiesAdapter: ArrayAdapter<String>? = null
    var citiesMap = mutableMapOf<String, Int>()
    var statesesMap = mutableMapOf<String, Int>()
    var citiesArray = arrayListOf<String>()
    var statesArray = arrayListOf<String>()
    var selectedCity = City()
    var selectedState = State()

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
        changeStatusBarColor()
        setViews()
        listeners()
        observer()

    }

    private fun observer() = viewBinding.apply{


        viewModel.statesResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()){
                Log.d("States", it.toList().toString())
                //getting states
                val list = it as ArrayList<State>
                processStates(list)

            }
        })


        viewModel.citiesResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()){
                Log.d("Cities", it.toList().toString())
                //getting states
                val list = it as ArrayList<City>
                processCities(list)

            }
        })

        viewModel.verificationResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val kycData = it ?: return@Observer
            Log.d("kycData", "data : $kycData")
            processKycData(kycData)
        })

        viewModel.addressResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val addressData = it ?: return@Observer
            Log.d("addressData", "data : $addressData")
            populateAddress(addressData)
        })
    }

    private fun processKycData(kycData: VerificationBaseModel) = viewBinding.apply{
        //set the values to views
        kycData.aadhar_card?.let {
            //set front image
            aadharFrontImagePath = Uri.parse(it.frontImage?.toString())


            if (it.frontImage != null) {
                if (it.frontImage!!.startsWith("http", true)) {
                    Glide.with(requireContext()).load(it.frontImage)
                        .placeholder(getCircularProgressDrawable()).into(aadharCardFrontImg)
                } else {
                    firebaseStorage
                        .reference
                        .child("verification")
                        .child(it.frontImage!!)
                        .downloadUrl.addOnSuccessListener {
                            Glide.with(requireContext()).load(it)
                                .placeholder(getCircularProgressDrawable()).into(aadharCardFrontImg)
                        }.addOnFailureListener {
                            print("ee")
                        }
                }
            }

            it.aadharCardNo?.let {
                aadharNo.editText?.setText(it)
            }
        }
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

    private fun listeners() = viewBinding.apply {

        viewModel.getStates()
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

        stateSpinner.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= statesList.size && stateSpinner.text.toString().isNotEmpty()){
                    val actualIndex = statesesMap.get(stateSpinner.text.toString().trim())
                    //citySpinner.setText("")
//                    citySpinner.postDelayed(Runnable {
//
//                    }, 10)
                    citySpinner.setText("", false)
//                    citySpinner.showDropDown()
                    selectedState = actualIndex?.let { statesList.get(it) }!!
                    Log.d("selected", "selected : $selectedState")
                    //get the cities
                    viewModel.getCities(selectedState.id)
                }
            }


        }

        citySpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= citiesList.size && citySpinner.text.toString().isNotEmpty()){
                    val actualIndex = citiesMap.get(citySpinner.text.toString().trim())
                    selectedCity = actualIndex?.let { citiesList.get(it) }!!
                    Log.d("selected", "selected : $selectedCity")

                }

            }

        }


        arrayAdapter = context?.let { it1 -> ArrayAdapter(it1,android.R.layout.simple_spinner_dropdown_item, statesArray ) }
        stateSpinner.setAdapter(arrayAdapter)
        stateSpinner.threshold = 1


        citiesAdapter = context?.let { it1 -> ArrayAdapter(it1,android.R.layout.simple_spinner_dropdown_item, citiesArray ) }
        citySpinner.setAdapter(citiesAdapter)
        citySpinner.threshold = 1

//        stateSpinner.setOnClickListener {
//            stateSpinner.showDropDown()
//        }
//        citySpinner.setOnClickListener {
//            citySpinner.showDropDown()
//        }

        stateSpinner.setOnFocusChangeListener { view, b ->
            if (b){
                stateSpinner.showDropDown()
            }
        }
        submitButton.setOnClickListener {

            if (aadharNo.editText?.text.toString().isBlank() || aadharNo.editText?.text.toString().length != 12) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Enter valid aadhaar number")
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

            if (pincodeInput.text.toString().isBlank() || pincodeInput.text.toString().length != 6) {
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


            //else submit the data
            var permanentAddress = AddressModel(
                addLine1Input.text.toString(),
                addLine2Input.text.toString(),
                landmarkInput.text.toString(),
                selectedCity.name,
                selectedState.name,
                pincodeInput.text.toString()
            )
        }
    }

    private fun populateAddress(address: AddressModel) = viewBinding.apply{
        addLine1Input.setText(address.firstLine)
        addLine2Input.setText(address.secondLine)
        landmarkInput.setText(address.area)
//        citySpinner.setText(address.city)
//        stateSpinner.setText(address.state)
        pincodeInput.setText(address.pincode)

        stateSpinner.postDelayed(Runnable {
            stateSpinner.setText(address.state, false)
            //stateSpinner.showDropDown()
        }, 5)

        citySpinner.postDelayed(Runnable {
            citySpinner.setText(address.city, false)
            //citySpinner.showDropDown()
        }, 5)

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
        viewModel.getVerificationData()
        viewModel.getAddressData()
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

    private fun processCities(content: ArrayList<City>) {
        citiesArray.clear()
        citiesList.toMutableList().clear()
        //citiesArray.add("Choose City...")
        citiesList = content
        citiesMap.clear()
        citiesList.forEachIndexed { index, city ->

            citiesArray.add(city.name)
            citiesMap.put(city.name, index)
        }

        citiesAdapter?.notifyDataSetChanged()
    }

    private fun processStates(content: ArrayList<State>) {
        statesArray.clear()
        statesList.toMutableList().clear()
        //citiesArray.add("Choose City...")
        statesList = content
        statesesMap.clear()
        statesList.forEachIndexed { index, city ->

            statesArray.add(city.name)
            statesesMap.put(city.name, index)
        }

        arrayAdapter?.notifyDataSetChanged()
    }

}