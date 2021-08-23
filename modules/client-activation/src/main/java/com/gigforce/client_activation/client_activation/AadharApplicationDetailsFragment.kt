package com.gigforce.client_activation.client_activation

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.adapters.VerificationViewPagerAdapter
import com.gigforce.client_activation.databinding.AadharApplicationDetailsFragmentBinding
import com.gigforce.client_activation.ui.ClientActivationClickOrSelectImageBottomSheet
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.CurrentAddressDetailDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.NavFragmentsData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.aadhar_application_details_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

enum class AadharCardSides {
    FRONT_SIDE,
    BACK_SIDE
}

@AndroidEntryPoint
class AadharApplicationDetailsFragment : Fragment(), IOnBackPressedOverride,
    ClientActivationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        fun newInstance() = AadharApplicationDetailsFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR = 2331

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_CLICKED_IMAGE_BACK = "back_image"
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
    private var aadharFrontImagePath: String? = null
    private var aadharBackImagePath: String? = null
    lateinit var adapter: VerificationViewPagerAdapter
    var pageClickListener: View.OnClickListener? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null
    private lateinit var mJobProfileId: String
    private var FROM_CLIENT_ACTIVATON: Boolean = false

    fun setPrimaryClick(pageClickListener: View.OnClickListener) {
        this.pageClickListener = pageClickListener
    }

    private var win: Window? = null

    //parmanent address variables (state variables is common with current add)
    var statesList = arrayListOf<State>()
    var stateAdapter: ArrayAdapter<String>? = null
    var statesesMap = mutableMapOf<String, Int>()
    var statesArray = arrayListOf<String>()
//    var selectedState = State()

    var citiesList = arrayListOf<City>()
    var citiesAdapter: ArrayAdapter<String>? = null
    var citiesMap = mutableMapOf<String, Int>()
    var citiesArray = arrayListOf<String>()
    var selectedCity = City()

    var imageFileName = ""

    // current address
    var caCitiesList = arrayListOf<City>()
    var caCitiesAdapter: ArrayAdapter<String>? = null
    var caCitiesMap = mutableMapOf<String, Int>()
    var caCitiesArray = arrayListOf<String>()
    var caSelectedCity = City()
    var caSelectedState = State()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadharApplicationDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        //initviews()
        changeStatusBarColor()
        setViews()
        listeners()
        observer()
        //showFrontAadhar("file:///data/user/0/com.gigforce.app.staging/cache/d5ToQmOn6sdAcPWvjsBuhYWm9kF3_20210820_120708_.jpg")

    }

    var allNavigationList = java.util.ArrayList<String>()
    var intentBundle: Bundle? = null

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {

            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }
    }

    private fun observer() = viewBinding.apply {

        viewModel.statesResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                Log.d("States", it.toList().toString())
                //getting states
                val list = it as ArrayList<State>
                processStates(list)
            }
        })


        viewModel.citiesResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                Log.d("Cities", it.toList().toString())
                //getting states
                val list = it as ArrayList<City>
                processCities(list)

            }
        })

        viewModel.caCitiesResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                Log.d("Cities", it.toList().toString())
                //getting states
                val list = it as ArrayList<City>
                processCACities(list)

            }
        })

        viewModel.verificationResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val kycData = it ?: return@Observer
            Log.d("kycData", "data : $kycData")
            processKycData(kycData)
        })

//        viewModel.addressResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            val addressData = it ?: return@Observer
//            Log.d("addressData", "data : $addressData")
//            populateAddress(addressData)
//        })

        viewModel.updatedResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val updated = it ?: return@Observer
            Log.d("updated", "up $updated")
            progressBar.visibility = View.GONE
            if (updated) {
                showToast("Data uploaded successfully")
            }

        })

//        viewModel.observableAddApplicationSuccess.observe(viewLifecycleOwner, Obser
//        )
        viewModel.observableAddApplicationSuccess.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it) {
                    checkForNextDoc()
                }
            }
        )
    }


    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()
            intentBundle?.putStringArrayList(
                StringConstants.NAVIGATION_STRING_ARRAY.value,
                java.util.ArrayList(navigationsForBundle)
            )
            navigation.navigateTo(
                allNavigationList.get(0), intentBundle
            )
//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(StringConstants.NAVIGATION_STRING_ARRAY.value to navigationsForBundle,if(FROM_CLIENT_ACTIVATON) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false)
//            )

        }
    }
    var aadhaarDetailsDataModel : AadhaarDetailsDataModel? = null
    private fun processKycData(kycData: VerificationBaseModel) = viewBinding.apply {
        aadhaarDetailsDataModel = kycData.aadhaar_card_questionnaire
        //set the values to views
        kycData.aadhaar_card_questionnaire?.let {
            //set front image


            var list = ArrayList<KYCImageModel>()
            it.frontImagePath?.let {
                aadharFrontImagePath = it
                getDBImageUrl(it).let {
                    list.add(
                        KYCImageModel(
                            text = "Please upload your AADHAR card Front side",
                            imagePath = it,
                            imageUploaded = true
                        )
                    )

                }
            }
            it.backImagePath?.let {
                aadharBackImagePath = it
                getDBImageUrl(it).let {
                    list.add(
                        KYCImageModel(
                            text = "Please upload your AADHAR card Back side",
                            imagePath = it,
                            imageUploaded = true
                        )
                    )

                }
            }
            setImageViewPager(list)

            it.aadhaarCardNo.let {
                aadharNo.editText?.setText(it)
            }
            it.dateOfBirth.let {
                dateOfBirth.text = it
                dobLabel.visible()
            }
            it.fName.let {
                fatherNameTil.editText?.setText(it)
            }
            it.addLine1.let {
                addLine1.editText?.setText(it)
            }
            it.addLine2.let {
                addLine2.editText?.setText(it)
            }
            it.state.let {
                if (it.isNotEmpty()) {
                    stateSpinner.setText(it, false)
                    //viewModel.getStates()
                    getCitiesWhenStateNotEmpty(it,true)
                }
            }
            currentAddCheckbox.isChecked = it.currentAddSameAsParmanent
            if (!it.currentAddSameAsParmanent) {
                it.currentAddress?.let { curradd->
                    caAddLine1Input.setText(curradd.addLine1)
                    caAddLine2Input.setText(curradd.addLine2)
                    caPincodeInput.setText(curradd.pincode)
                    caLandmarkInput.setText(curradd.landmark)

                    if(curradd.state.isNotBlank()){
                        caStateSpinner.setText(curradd.state,false)
                        getCitiesWhenStateNotEmpty(curradd.state,false)
                    }
                }
            }
            it.city.let {
                citySpinner.setText(it, false)
            }
            it.pincode.let {
                pincode.editText?.setText(it)
            }
            it.landmark.let {
                landmark.editText?.setText(it)
            }
        }
    }

    private fun getCitiesWhenStateNotEmpty(stateStr: String, parmanentCity : Boolean = true) {
        //get the value from states

        val index = statesesMap.get(stateStr)
        val stateModel = index?.let { it1 -> statesList.get(it1) }
        Log.d("index", "i: $index , map: $statesesMap")
        if (stateModel?.id.toString().isNotEmpty()) {
            if(parmanentCity) {
                viewModel.getCities(stateModel?.id.toString())
            }else{
                viewModel.getCurrentAddCities(stateModel?.id.toString())
            }
            Log.d("index", "i: $index , map: ${stateModel?.id}")
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
                if (p2 <= statesList.size && stateSpinner.text.toString().isNotEmpty()) {
                    val actualIndex = statesesMap.get(stateSpinner.text.toString().trim())
                    //citySpinner.setText("")
//                    citySpinner.postDelayed(Runnable {
//
//                    }, 10)
                    citySpinner.setText("", false)
//                    citySpinner.showDropDown()
                    var selectedState = actualIndex?.let { statesList.get(it) }!!
                    Log.d("selected", "selected : $selectedState")
                    //get the cities
                    //progressBar.visibility = View.VISIBLE
                    viewModel.getCities(selectedState.id)
                }
            }
        }

        citySpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= citiesList.size && citySpinner.text.toString().isNotEmpty()) {
                    val actualIndex = citiesMap.get(citySpinner.text.toString().trim())
                    selectedCity = actualIndex?.let { citiesList.get(it) }!!
                    Log.d("selected", "selected : $selectedCity")

                }

            }

        }


        // current address state and city
        caStateSpinner.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= statesList.size && caStateSpinner.text.toString().isNotEmpty()) {
                    val actualIndex = statesesMap.get(caStateSpinner.text.toString().trim())
                    caCitySpinner.setText("", false)
                    var selectedState = actualIndex?.let { statesList.get(it) }!!
                    Log.d("selected", "selected : $selectedState")
                    viewModel.getCurrentAddCities(selectedState.id)
                }
            }
        }

        caCitySpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= caCitiesList.size && caCitySpinner.text.toString().isNotEmpty()) {
                    val actualIndex = caCitiesMap.get(caCitySpinner.text.toString().trim())
                    caSelectedCity = actualIndex?.let { caCitiesList.get(it) }!!
                    Log.d("selected", "selected : $caSelectedCity")

                }

            }
        }

        stateAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                statesArray
            )
        }
        stateSpinner.setAdapter(stateAdapter)
        stateSpinner.threshold = 1
        stateSpinner.setOnFocusChangeListener { view, b ->
            if (b) {
                stateSpinner.showDropDown()
            }
        }


        citiesAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                citiesArray
            )
        }
        citySpinner.setAdapter(citiesAdapter)
        citySpinner.threshold = 1


        // current address state (state adapter is same as parmanent add) and city
        caStateSpinner.setAdapter((stateAdapter))
        caStateSpinner.threshold = 1
        caStateSpinner.setOnFocusChangeListener { view, b ->
            if (b) {
                caStateSpinner.showDropDown()
            }
        }

        caCitiesAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                caCitiesArray
            )
        }
        caCitySpinner.setAdapter(caCitiesAdapter)
        caCitySpinner.threshold = 1


        aadharNo.editText?.addTextChangedListener(ValidationTextWatcher())
        fatherNameTil.editText?.addTextChangedListener(ValidationTextWatcher())
        dateOfBirth.addTextChangedListener(ValidationTextWatcher())
        stateSpinner.addTextChangedListener(ValidationTextWatcher())
        citySpinner.addTextChangedListener(ValidationTextWatcher())
        addLine1.editText?.addTextChangedListener(ValidationTextWatcher())
        addLine2.editText?.addTextChangedListener(ValidationTextWatcher())
        pincode.editText?.addTextChangedListener(ValidationTextWatcher())
        landmark.editText?.addTextChangedListener(ValidationTextWatcher())


        submitButton.setOnClickListener {
            if (submitButton.text.toString() == "Submit") {
                if (aadharFrontImagePath == null || aadharFrontImagePath?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Upload aadhaar card front photo")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (aadharBackImagePath == null || aadharBackImagePath?.isEmpty() == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Upload aadhaar card back photo")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (aadharNo.editText?.text.toString()
                        .isBlank() || aadharNo.editText?.text.toString().length != 12
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter valid aadhaar number")
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

                if (fatherNameTil.editText?.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Enter father name")
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

                if (stateSpinner.text.toString()
                        .isEmpty() || !statesArray.contains(stateSpinner.text.toString())
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.select_aadhar_state))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (citySpinner.text.toString()
                        .isEmpty() || !citiesArray.contains(citySpinner.text.toString())
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage("Select City")
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (pincodeInput.text.toString()
                        .isBlank() || pincodeInput.text.toString().length != 6
                ) {
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
                //current address validation
                if (!currentAddCheckbox.isChecked) {

                    if (caAddLine1Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage("Enter Current Address Line 1")
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caAddLine2Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage("Enter Current Address Line 2")
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caStateSpinner.text.toString()
                            .isEmpty() || !statesArray.contains(caStateSpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.select_aadhar_state))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caCitySpinner.text.toString()
                            .isEmpty() || !caCitiesArray.contains(caCitySpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage("Select City")
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caPincodeInput.text.toString()
                            .isBlank() || caPincodeInput.text.toString().length != 6
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage("Enter valid pincode")
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caLandmarkInput.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage("Enter Landmark")
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }
                }

                submitData()

            } else {
                checkForNextDoc()
            }
        }

        viewBinding.currentAddCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewBinding.currentAddLayout.gone()
            } else {
                viewBinding.currentAddLayout.visible()
            }
        }

    }

    var anyDataEntered = false

    inner class ValidationTextWatcher : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(text: Editable?) {
            context?.let { cxt ->
                text?.let {

                    if (viewBinding.aadharNo.editText?.text.toString()
                            .isNullOrBlank() && viewBinding.dateOfBirth.text.toString()
                            .isNullOrBlank() && viewBinding.fatherNameTil.editText?.text.toString()
                            .isNullOrBlank() && viewBinding.addLine1.editText?.text.toString()
                            .isNullOrBlank() && viewBinding.addLine2.editText?.text.toString()
                            .isNullOrBlank() && viewBinding.stateSpinner.text.toString()
                            .isNullOrBlank() && viewBinding.citySpinner.text.toString()
                            .isNullOrBlank() && viewBinding.pincode.editText?.text.toString()
                            .isNullOrBlank() && viewBinding.landmark.editText?.text.toString()
                            .isNullOrBlank()
                    ) {
                        viewBinding.submitButton.text = "Skip"
                        anyDataEntered = false
                    } else {
                        viewBinding.submitButton.text = "Submit"
                        anyDataEntered = true
                    }

                }
            }
        }


    }

    private fun submitData() = viewBinding.apply {
        progressBar.visibility = View.VISIBLE
        //else submit the data
//        var permanentAddress = AddressModel(
//            addLine1Input.text.toString(),
//            addLine2Input.text.toString(),
//            landmarkInput.text.toString(),
//            selectedCity.name,
//            selectedState.name,
//            pincodeInput.text.toString()
//        )

        var submitDataModel = AadhaarDetailsDataModel(
            aadharFrontImagePath,
            aadharBackImagePath,
            aadhaarCardNo = aadharNo.editText?.text.toString(),
            dateOfBirth = dateOfBirth.text.toString(),
            fName = fatherNameTil.editText?.text.toString(),
            addLine1 = addLine1Input.text.toString(),
            addLine2 = addLine2Input.text.toString(),
            state = stateSpinner.text.toString(),
            city = citySpinner.text.toString(),
            pincode = pincode.editText?.text.toString(),
            landmark = landmark.editText?.text.toString(),
            currentAddSameAsParmanent = currentAddCheckbox.isChecked,
            currentAddress = if (!currentAddCheckbox.isChecked) CurrentAddressDetailDataModel(
                addLine1 = caAddLine1Input.text.toString(),
                addLine2 = caAddLine2Input.text.toString(),
                state = caStateSpinner.text.toString(),
                city = caCitySpinner.text.toString(),
                pincode = caPincodeInput.text.toString(),
                landmark = caLandmarkInput.text.toString()
            ) else null
        )
        viewModel.setAadhaarDetails(submitDataModel, mJobProfileId)
    }

    private fun populateAddress(address: AddressModel) = viewBinding.apply {
        addLine1Input.setText(address.firstLine)
        addLine2Input.setText(address.secondLine)
        landmarkInput.setText(address.area)
        pincodeInput.setText(address.pincode)
        stateSpinner.setText(address.state, false)
        citySpinner.setText(address.city, false)


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
                text = "Please upload your AADHAR card Front side",
                imageIcon = frontUri,
                imageUploaded = false
            ),
            KYCImageModel(
                text = "Please upload your AADHAR card Back side",
                imageIcon = backUri,
                imageUploaded = false
            )
        )
        setImageViewPager(list)
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions()) {

            ClientActivationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                "Upload Aadhar Card",
                this
            )


//            Log.v("Start Crop", "started")
//        //can use this for a new name every time
//        val timeStamp = SimpleDateFormat(
//            "yyyyMMdd_HHmmss",
//            Locale.getDefault()
//        ).format(Date())
//        imageFileName = "verification/" + PREFIX + "_" + timeStamp + "_.jpg"
//        if (viewBinding.viewPager2.currentItem == 0){
//            currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE
//        }else {
//            currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE
//        }
//
//        val photoCropIntent = Intent()
//        photoCropIntent.putExtra(
//            "purpose",
//            ""
//        )
//        photoCropIntent.putExtra("fbDir", "/verification/")
//        photoCropIntent.putExtra("folder", "verification")
//        photoCropIntent.putExtra("detectFace", 0)
//        photoCropIntent.putExtra("uid", viewModel.uid)
//        photoCropIntent.putExtra("file", imageFileName)
//        navigation.navigateToPhotoCrop(
//            photoCropIntent,
//            REQUEST_CODE_UPLOAD_AADHAR, requireContext(), this@AadharApplicationDetailsFragment
//        )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                startCrop(outputFileUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            imageUriResultCrop?.let {
                progressBar.visible()
                firebaseStorage.reference
                    .child("verification")
                    .child(fileName)
                    .putFile(it).addOnSuccessListener {
                        progressBar.gone()
                        if (imageUriResultCrop != null) {
                            if (viewBinding.viewPager2.currentItem == 0) {
                                aadharFrontImagePath = it.metadata?.path
                                showAadharImage(imageUriResultCrop, 0)
                            } else if (viewBinding.viewPager2.currentItem == 1) {
                                aadharBackImagePath = it.metadata?.path
                                showAadharImage(imageUriResultCrop, 1)
                            }
                        }
                    }.addOnFailureListener {
                        progressBar.gone()
                    }.addOnCanceledListener { progressBar.gone() }
            }


        }
//        else if (requestCode == REQUEST_CODE_UPLOAD_AADHAR && resultCode == Activity.RESULT_OK) {
//
//            val imageUriResultCrop: String? = data?.getStringExtra("image_url")
//            val fileName: String? = data?.getStringExtra("filename")
//            Log.d("fileName", fileName.toString())
//            Log.d("ImageUri1", imageUriResultCrop.toString())
//            if (imageUriResultCrop != null) {
//                if (AadharCardSides.FRONT_SIDE == currentlyClickingImageOfSide) {
//                    aadharFrontImagePath = "verification/" + fileName
//                    showFrontAadhar(imageUriResultCrop)
//                } else if (AadharCardSides.BACK_SIDE == currentlyClickingImageOfSide) {
//                    aadharBackImagePath = "verification/" + fileName
//                    showBackAadhar(imageUriResultCrop)
//                }
//            }
//        }
    }

    private fun showAadharImage(uri: Uri, position: Int) {
        if (position in 0..1)
            setDocumentImage(position, uri)

    }

    var fileName: String = ""
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
        fileName = imageFileName + EXTENSION
        val resultIntent: Intent = Intent()
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        val size = getImageDimensions(uri)
        uCrop.withAspectRatio(size.width.toFloat(), size.height.toFloat())
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(requireContext(), this)
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate))
        return options
    }

    private fun showBackAadhar(image: String?) {
        Log.d("image", "im $image")
        image?.let {
            val uri = Uri.parse(it)
            setDocumentImage(1, uri)
        }

    }

    fun setDocumentImage(position: Int, uri: Uri) {
        adapter.updateData(position, uri)
    }


    private fun getImageDimensions(uri: Uri): Size {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        return Size(imageWidth, imageHeight)
    }

    fun setImageViewPager(list: List<KYCImageModel>) = viewBinding.apply {

        if (list.isEmpty()) {
            viewPager2.gone()
            tabLayout.gone()
        } else {
            viewPager2.visible()
            tabLayout.visible()
            adapter = VerificationViewPagerAdapter {
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

    private fun showFrontAadhar(path: String) {
        Log.d("image", "im $path")
        path.let {
            val uri = Uri.parse(it)
            setDocumentImage(0, uri)
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
        if (FROM_CLIENT_ACTIVATON) {
            var navFragmentsData = activity as NavFragmentsData
            navFragmentsData.setData(
                bundleOf(
                    com.gigforce.core.StringConstants.BACK_PRESSED.value to true

                )
            )
        }
        return false
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

        citiesList.toMutableList().clear()
        citiesList = ArrayList(content.sortedBy { it.name })

        citiesArray.clear()
        citiesMap.clear()
        citiesList.forEachIndexed { index, city ->

            citiesArray.add(city.name)
            citiesMap.put(city.name, index)
        }
        Log.d("map", "$citiesMap")
        //viewBinding.progressBar.visibility = View.GONE
        citiesAdapter?.notifyDataSetChanged()

    }
    var caCityFilled = false
    private fun processCACities(content: ArrayList<City>) {

        caCitiesList.toMutableList().clear()
        caCitiesList = ArrayList(content.sortedBy { it.name })

        caCitiesArray.clear()
        caCitiesMap.clear()
        caCitiesList.forEachIndexed { index, city ->

            caCitiesArray.add(city.name)
            caCitiesMap.put(city.name, index)
        }
        Log.d("map", "$citiesMap")
        //viewBinding.progressBar.visibility = View.GONE
        caCitiesAdapter?.notifyDataSetChanged()

        if(!caCityFilled && caCitiesArray.contains(aadhaarDetailsDataModel?.currentAddress?.city?:"")){
            viewBinding.caCitySpinner.setText(aadhaarDetailsDataModel?.currentAddress?.city?:"",false)
            caCityFilled = true
        }

    }

    private fun processStates(content: ArrayList<State>) {

        statesList.toMutableList().clear()
        statesList = ArrayList(content.sortedBy { it.name })

        statesArray.clear()
        statesesMap.clear()
        statesList.forEachIndexed { index, state ->

            statesArray.add(state.name)
            statesesMap.put(state.name, index)

        }
        Log.d("map", "$statesesMap")
        stateAdapter?.notifyDataSetChanged()
        viewModel.getVerificationData()
        //getCitiesWhenStateNotEmpty(viewBinding.stateSpinner.text.toString().trim())
    }

}