package com.gigforce.verification.mainverification.aadhardetail

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.CurrentAddressDetailDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadharDetailInfoFragmentBinding
import com.gigforce.verification.mainverification.OLDStateHolder
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.gigforce.verification.mainverification.pancard.VerificationScreenStatus
import com.gigforce.verification.util.VerificationConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class AadharDetailInfoFragment : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 102
        private const val REQUEST_CAPTURE_IMAGE = 1001
        private const val REQUEST_PICK_IMAGE = 1002
        private const val FATHER = "father"
    }

    private val viewModel: AadharDetailInfoViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private var aadharFrontImagePath: String? = null
    private var aadharBackImagePath: String? = null
    private var mJobProfileId: String = ""
    private var FROM_CLIENT_ACTIVATON: Boolean = false

    @Inject
    lateinit var buildConfig: IBuildConfig

    @Inject
    lateinit var navigation: INavigation

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

    // current address
    var caCitiesList = arrayListOf<City>()
    var caCitiesAdapter: ArrayAdapter<String>? = null
    var caCitiesMap = mutableMapOf<String, Int>()
    var caCitiesArray = arrayListOf<String>()
    var caSelectedCity = City()

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
        getDataFromIntent(savedInstanceState)
        initviews()
        setViews()
        observer()
        listener()
    }

    private fun initviews() {
        viewBinding.toplayoutblock.setIdonthaveDocContent(
            resources.getString(R.string.no_doc_title_aadhaar_veri),
            resources.getString(R.string.no_doc_subtitle_aadhaar_veri)
        )
    }

    var allNavigationList = ArrayList<String>()
    var intentBundle: Bundle? = null
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {

            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
        } ?: run {
            arguments?.let {

                FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
                intentBundle = it
                mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            }

        }

    }

    var anyDataEntered = false
    var verificationScreenStatus = VerificationScreenStatus.DEFAULT

    inner class ValidationTextWatcher :
        TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            context?.let { cxt ->
                if (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.OCR_COMPLETED || verificationScreenStatus == VerificationScreenStatus.STARTED) {
                    text?.toString().let {
                        viewBinding.apply {
                            if (aadharNo.editText?.text
                                    .isNullOrBlank() && addLine1Input.text.toString()
                                    .isNullOrBlank() && addLine2Input.text.toString()
                                    .isNullOrBlank() && pincodeInput.text.toString()
                                    .isNullOrBlank() && landmarkInput.text.toString()
                                    .isNullOrEmpty()
                            ) {
                                verificationScreenStatus = VerificationScreenStatus.DEFAULT
                                submitButton.text = getString(R.string.skip_veri)
                                anyDataEntered = false
                            } else {
                                verificationScreenStatus = VerificationScreenStatus.STARTED
                                submitButton.text = getString(R.string.submit_veri)
                                anyDataEntered = true
                            }
                        }

                    }
                }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

    }

    var oldStateHolder = OLDStateHolder("")
    private fun listener() = viewBinding.apply {

        toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            checkForPermissionElseShowCameraGalleryBottomSheet()
        })

        toplayoutblock.setChangeTextListener {
            allFieldsEnable(true)
            viewBinding.toplayoutblock.toggleChangeTextView(false)
            verificationScreenStatus = VerificationScreenStatus.STARTED
            viewBinding.submitButton.text = getString(R.string.submit_veri)
        }

        toplayoutblock.setOnCheckedChangeListener { p1, b1 ->
            if (b1) {
                oldStateHolder.submitButtonCta = viewBinding.submitButton.text.toString()
                viewBinding.submitButton.text = getString(R.string.skip_veri)
                viewBinding.belowLayout.gone()
            } else {
                viewBinding.submitButton.text = oldStateHolder.submitButtonCta
                viewBinding.belowLayout.visible()
            }
        }

        dateOfBirthLabel.setOnClickListener {
            dateOfBirthPicker.show()
        }

        stateSpinner.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("selectedIndex", "ind: $p2")
                //get the state code
                if (p2 <= statesList.size && stateSpinner.text.toString().isNotEmpty()) {
                    val actualIndex = statesesMap.get(stateSpinner.text.toString().trim())
                    citySpinner.setText("", false)
                    var selectedState = actualIndex?.let { statesList.get(it) }!!
                    Log.d("selected", "selected : $selectedState")
                    viewModel.getCities(selectedState.id)
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

        submitButton.setOnClickListener {
            if (viewBinding.toplayoutblock.isDocDontOptChecked() || verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.COMPLETED) {
                checkForNextDoc()
            } else {
                if (anyDataEntered) {
                    if (aadharFrontImagePath == null || aadharFrontImagePath?.isEmpty() == true) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.upload_aadhar_front_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (aadharBackImagePath == null || aadharBackImagePath?.isEmpty() == true) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.upload_aadhar_back_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (name.editText?.text.toString()
                            .isBlank() || name.editText?.text.toString().length < 3
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_name_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (aadharNo.editText?.text.toString()
                            .isBlank() || aadharNo.editText?.text.toString().length != 12
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_valid_aadhar_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (dateOfBirth.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.select_dob_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }
                    if (fatherNameTil.editText?.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_father_name_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }
                    if (addLine1Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_add1_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (addLine2Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_add2_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (stateSpinner.text.toString()
                            .isEmpty() || !statesArray.contains(stateSpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.select_state_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (citySpinner.text.toString()
                            .isEmpty() || !citiesArray.contains(citySpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.select_city_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (pincodeInput.text.toString()
                            .isBlank() || pincodeInput.text.toString().length != 6
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_veri))
                            .setMessage(getString(R.string.enter_valid_pin_veri))
                            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

//                    if (landmarkInput.text.toString().isBlank()) {
//                        MaterialAlertDialogBuilder(requireContext())
//                                .setTitle(getString(R.string.alert_veri))
//                                .setMessage(getString(R.string.enter_landmark_veri))
//                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
//                                .show()
//                        return@setOnClickListener
//                    }

                    //current address validation
                    if (!currentAddCheckbox.isChecked) {

                        if (caAddLine1Input.text.toString().isBlank()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.curr_add1_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        if (caAddLine2Input.text.toString().isBlank()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.curr_add2_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        if (caStateSpinner.text.toString()
                                .isEmpty() || !statesArray.contains(caStateSpinner.text.toString())
                        ) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.select_state_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        if (caCitySpinner.text.toString()
                                .isEmpty() || !caCitiesArray.contains(caCitySpinner.text.toString())
                        ) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.select_city_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        if (caPincodeInput.text.toString()
                                .isBlank() || caPincodeInput.text.toString().length != 6
                        ) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_valid_pin_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

//                        if (caLandmarkInput.text.toString().isBlank()) {
//                            MaterialAlertDialogBuilder(requireContext())
//                                    .setTitle(getString(R.string.alert_veri))
//                                    .setMessage(getString(R.string.enter_landmark_veri))
//                                    .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
//                                    .show()
//                            return@setOnClickListener
//                        }
                    }

                    submitData()

                } else {
                    checkForNextDoc()
                }
            }
        }


        landmarkInput.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        pincodeInput.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        addLine2Input.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        addLine1Input.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        dateOfBirth.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        aadharNo.editText?.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        appBarAadhar.apply {
            setBackButtonListener {
                activity?.onBackPressed()
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

    private fun submitData() = viewBinding.apply {
        viewBinding.progressBar.visibility = View.VISIBLE
        var submitDataModel = AadhaarDetailsDataModel(
            frontImagePath = aadharFrontImagePath,
            backImagePath = aadharBackImagePath,
            aadhaarCardNo = aadharNo.editText?.text.toString(),
            dateOfBirth = dateOfBirth.text.toString(),
            fName = fatherNameTil.editText?.text.toString(),
            addLine1 = addLine1Input.text.toString(),
            addLine2 = addLine2Input.text.toString(),
            state = stateSpinner.text.toString(),
            city = citySpinner.text.toString(),
            pincode = pincode.editText?.text.toString(),
            landmark = landmark.editText?.text.toString(),
            name = name.editText?.text.toString(),
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
        if (FROM_CLIENT_ACTIVATON)
            viewModel.setAadhaarDetails(submitDataModel, false, mJobProfileId)
        else
            viewModel.setAadhaarDetails(submitDataModel, false, "")

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
                com.gigforce.common_ui.StringConstants.NAVIGATION_STRING_ARRAY.value,
                java.util.ArrayList(navigationsForBundle)
            )
            navigation.navigateTo(
                allNavigationList.get(0), intentBundle
            )
        }
    }

    private var dobYear = 1990
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
            dobYear,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }
    var ocrCity = ""
    var ocrCityDetected = false
    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            it?.let {
                if (it.status) {
                    verificationScreenStatus = VerificationScreenStatus.OCR_COMPLETED
                    if (!it.name.isNullOrBlank() || !it.dateOfBirth.isNullOrBlank() || !it.aadhaarNumber.isNullOrBlank() || !it.gender.isNullOrBlank()) {
                        viewBinding.apply {
                            it.name?.let {
                                if (it.isNotEmpty()) {
                                    name.editText?.setText(it)
                                }
                            }
                            it.dateOfBirth?.let {
                                if (it.isNotEmpty()) {
                                    if (it.contains("-") && it.length > 8) {
                                        dateOfBirth.text = it
                                        dobLabel.visible()
                                    } else if (it.length == 4) {
                                        try {
                                            dobYear = it.toInt()
                                        } catch (e: Exception) {

                                        }
                                    }
                                }
                            }
                            it.aadhaarNumber?.let {
                                if (it.isNotEmpty() && !it.contains("X") && !it.contains("x")) {
                                    var aadharNotemp = it
                                    if (it.contains(""))
                                        aadharNotemp = it.split(" ").joinToString(separator = "")
                                    aadharNo.editText?.setText(aadharNotemp)
                                }
                            }
                            toplayoutblock.uploadStatusLayout(
                                AppConstants.UPLOAD_SUCCESS,
                                getString(R.string.upload_success_veri),
                                getString(R.string.info_of_aadhar_success_veri)
                            )
                        }

                    } else if (!it.city.isNullOrEmpty() || !it.state.isNullOrEmpty() || !it.pinCode.isNullOrEmpty() || !it.district.isNullOrEmpty() || !it.address1.isNullOrEmpty() || !it.address2.isNullOrEmpty()) {
                        viewBinding.apply {
                            it.address1?.let {
                                addLine1.editText?.setText(it)
                            }
                            it.address2?.let {
                                addLine2.editText?.setText(it)
                            }
                            it.pinCode?.let {
                                pincode.editText?.setText(it)
                            }

//                            it.state?.let { state ->
//                                if (statesArray.contains(state)) {
//                                    stateSpinner.setText(state)
//                                    it.city?.let {
//                                        ocrCity = it
//                                        ocrCityDetected = true
//                                        getCitiesWhenStateNotEmpty(it)
//                                    }
//                                }
//
//                            }

                            it.state.let { state ->
                                if (state?.isNotEmpty() == true) {
                                    if (statesArray.contains(state)) {
                                        stateSpinner.setText(state, false)
                                        getCitiesWhenStateNotEmpty(state)
                                        cityAutofillRequire = true
                                        if (it.city?.isNotBlank() == true) {
                                            autofillCityName = it.city
                                        }
                                    }
                                }
                            }


                            toplayoutblock.uploadStatusLayout(
                                AppConstants.UPLOAD_SUCCESS,
                                getString(R.string.upload_success_veri),
                                getString(R.string.info_of_aadhar_success_veri)
                            )
                        }
                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            getString(R.string.unable_to_fetch_info_veri),
                            getString(R.string.enter_aadhar_details_veri)
                        )
                    }

                } else {
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        getString(R.string.unable_to_fetch_info_veri),
                        getString(R.string.enter_aadhar_details_veri)
                    )
                }
            }
        })
        viewModel.updatedResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val updated = it ?: return@Observer
            Log.d("updated", "up $updated")
            viewBinding.progressBar.gone()
            if (updated) {
                showToast(getString(R.string.data_uploaded_veri))
                viewModel.getVerificationData()
            }

        })
        viewModel.statesResult.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                Log.d("States", it.toList().toString())
                //getting states
                val list = it as ArrayList<State>
                processStates(list)
            }
        })

        viewModel.citiesResult.observe(viewLifecycleOwner, {
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
            it.aadhaar_card_questionnaire?.apply {
                if (verified == true) {
                    allFieldsEnable(false)
                    viewBinding.toplayoutblock.toggleChangeTextView(true)
                    viewBinding.submitButton.text = getString(R.string.next_veri)
                    verificationScreenStatus = VerificationScreenStatus.COMPLETED

                } else {
                    viewBinding.submitButton.text = getString(R.string.submit_veri)
                }
            }
        })

        viewModel.profileNominee.observe(viewLifecycleOwner, Observer {
            setNomineeCheckbox(it?.pfNominee)
        })

        viewModel.observableAddApplicationSuccess.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it) {
                    checkForNextDoc()
                }
            }
        )
    }

    private fun setNomineeCheckbox(it: String?) = viewBinding.apply {
        nomineeCheckbox.isChecked = !it.isNullOrEmpty() && it == FATHER

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

        if (!caCityFilled && caCitiesArray.contains(
                aadhaarDetailsDataModel?.currentAddress?.city ?: ""
            )
        ) {
            viewBinding.caCitySpinner.setText(
                aadhaarDetailsDataModel?.currentAddress?.city ?: "",
                false
            )
            caCityFilled = true
        }

    }

    private fun allFieldsEnable(enable: Boolean) = viewBinding.apply {
        name.editText?.isEnabled = enable
        aadharNo.editText?.isEnabled = enable
        dateOfBirthLabel.isEnabled = enable
        fatherNameTil.editText?.isEnabled = enable
        addLine1Input.isEnabled = enable
        addLine2Input.isEnabled = enable
        stateSpinner.isEnabled = enable
        citySpinner.isEnabled = enable
        pincode.editText?.isEnabled = enable
        landmark.editText?.isEnabled = enable
        nomineeCheckbox.isEnabled = enable

        currentAddCheckbox.isEnabled = enable
        caAddLine1Input.isEnabled = enable
        caAddLine2Input.isEnabled = enable
        caStateSpinner.isEnabled = enable
        caCitySpinner.isEnabled = enable
        caPincodeInput.isEnabled = enable
        caLandmarkInput.isEnabled = enable


        if (enable) toplayoutblock.enableImageClick() else toplayoutblock.disableImageClick()

        if (enable) {
            viewBinding.toplayoutblock.setVerificationSuccessfulView(
                getString(R.string.aadhaar_card_veri),
                getString(R.string.you_need_to_upload_veri)
            )
            viewBinding.toplayoutblock.showUploadHere()
            viewBinding.toplayoutblock.statusDialogLayoutvisibilityGone()

        } else {
            viewBinding.toplayoutblock.setVerificationSuccessfulView(getString(R.string.aadhar_submitted_veri))

            viewBinding.toplayoutblock.uploadStatusLayout(
                AppConstants.UPLOAD_SUCCESS,
                getString(R.string.document_submitted_veri),
                getString(R.string.aadhar_submitted_successfully_veri)
            )
            viewBinding.toplayoutblock.viewChangeOnVerified()
        }
    }

    var aadhaarDetailsDataModel: AadhaarDetailsDataModel? = null
    private fun processKycData(kycData: VerificationBaseModel) = viewBinding.apply {
        aadhaarDetailsDataModel = kycData.aadhaar_card_questionnaire
        //set the values to views
        kycData.aadhaar_card_questionnaire?.let {
            //set front image

            name.editText?.setText(it.name)
            var list = ArrayList<KYCImageModel>()
            it.frontImagePath?.let {
                aadharFrontImagePath = it
                getDBImageUrl(it).let {
                    list.add(
                        KYCImageModel(
                            text = getString(R.string.upload_front_side_first_veri),
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
                            text = getString(R.string.upload_aadhar_card_back_side_veri),
                            imagePath = it,
                            imageUploaded = true
                        )
                    )

                }
            }
            viewBinding.toplayoutblock.setImageViewPager(list)

            it.aadhaarCardNo.let {
                aadharNo.editText?.setText(it)
            }
            it.dateOfBirth.let {
                if (it.isNotEmpty()) {
                    dateOfBirth.text = it
                    dobLabel.visible()
                }
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
            it.state.let { state ->
                if (state.isNotEmpty()) {
                    if (statesArray.contains(state)) {
                        stateSpinner.setText(state, false)
                        getCitiesWhenStateNotEmpty(state)
                        cityAutofillRequire = true
                        if (it.city.isNotBlank()) {
                            autofillCityName = it.city
                        }
                    }
                }
            }

            currentAddCheckbox.isChecked = it.currentAddSameAsParmanent
            if (!it.currentAddSameAsParmanent) {
                it.currentAddress?.let { curradd ->
                    caAddLine1Input.setText(curradd.addLine1)
                    caAddLine2Input.setText(curradd.addLine2)
                    caPincodeInput.setText(curradd.pincode)
                    caLandmarkInput.setText(curradd.landmark)

                    if (curradd.state?.isNotBlank() == true) {
                        caStateSpinner.setText(curradd.state, false)
                        getCitiesWhenStateNotEmpty(curradd.state ?: "", false)
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

    private fun getCitiesWhenStateNotEmpty(stateStr: String, parmanentCity: Boolean = true) {
        //get the value from states

        val index = statesesMap.get(stateStr)
        val stateModel = index?.let { it1 -> statesList.get(it1) }
        Log.d("index", "i: $index , map: $statesesMap")
        if (stateModel?.id.toString().isNotEmpty()) {
            if (parmanentCity) {
                viewModel.getCities(stateModel?.id.toString())
            } else {
                viewModel.getCurrentAddCities(stateModel?.id.toString())
            }
        }
    }

    private fun setViews() {
        viewBinding.toplayoutblock.whyweneeditInvisible()
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
                text = getString(R.string.upload_aadhar_card_front_side_veri),
                imageIcon = frontUri,
                imageUploaded = false
            ),
            KYCImageModel(
                text = getString(R.string.upload_aadhar_card_back_side_veri),
                imageIcon = backUri,
                imageUploaded = false
            )
        )
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    var cityAutofillRequire = false
    var autofillCityName = ""
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
        if (ocrCityDetected) {
            if (citiesArray.contains(ocrCity))
                viewBinding.citySpinner.setText(ocrCity, false)
            ocrCityDetected = false
        }
        if (cityAutofillRequire) {
            if (citiesArray.contains(autofillCityName))
                viewBinding.citySpinner.setText(autofillCityName, false)
            else
                viewBinding.citySpinner.setText("", false)
            cityAutofillRequire = false
        }
    }

    private fun processStates(content: ArrayList<State>) {

        statesList.toMutableList().clear()
        statesList = ArrayList(content.filter { it.name != "" }.sortedBy { it.name })

        statesArray.clear()
        statesesMap.clear()
        statesList.forEachIndexed { index, state ->

            statesArray.add(state.name)
            statesesMap.put(state.name, index)

        }
        Log.d("map", "$statesesMap")
        stateAdapter?.notifyDataSetChanged()
        viewModel.getVerificationData()
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions()) {
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                getString(R.string.upload_aadhar_card_veri),
                this
            )
        } else
            requestStoragePermission()
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
                    VerificationClickOrSelectImageBottomSheet.launch(
                        parentFragmentManager,
                        getString(R.string.upload_aadhar_card_veri),
                        this
                    )
                else {
                    showToast(getString(R.string.grant_storage_permission_veri))
                }
            }
        }
    }


    private fun hasStoragePermissions(): Boolean {
        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK){
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else{
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
    }


    private fun requestStoragePermission() {

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK){
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else{
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }


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
            val imageUriResultCrop: Uri? =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
            Log.d("ImageUri", imageUriResultCrop.toString())
            clickedImagePath = imageUriResultCrop
            if (imageUriResultCrop != null) {
                showAadharImage(
                    imageUriResultCrop,
                    viewBinding.toplayoutblock.viewPager2.currentItem
                )
                uploadImage(imageUriResultCrop)
            }

        }
    }

    private fun showAadharImage(uri: Uri, position: Int) {
        if (position in 0..1) {
            if (position == 0) aadharFrontImagePath = uri.toString()
            else aadharBackImagePath = uri.toString()
            viewBinding.toplayoutblock.setDocumentImage(position, uri)
        }

    }

    private fun startCropImage(imageUri: Uri): Unit {
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)

    }

    private fun uploadImage(panInfoPath: Uri) {
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
            viewModel.getKycOcrResult(
                "aadhar",
                if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) "front" else "back",
                it
            )
        }
    }
}