package com.gigforce.client_activation.client_activation.info.joiningform

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.info.hubform.HubServerDM
import com.gigforce.client_activation.databinding.JoiningFormFragmentBinding
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.CurrentAddressDetailDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.NavFragmentsData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class JoiningFormFragment : Fragment(), IOnBackPressedOverride {


    private val viewModel: JoiningFormViewModel by viewModels()
    private lateinit var mJobProfileId: String
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    var allNavigationList = ArrayList<String>()
    var intentBundle: Bundle? = null

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


    // Hub state,city, name
    var hubStateAdapter: ArrayAdapter<String>? = null
    var hubStatesMap = mutableMapOf<String, Int>()
    var hubStatesArray = arrayListOf<String>()

    var hubCitiesAdapter: ArrayAdapter<String>? = null
    var hubCitiesMap = mutableMapOf<String, Int>()
    var hubCitiesArray = arrayListOf<String>()

    var hubNameAdapter: ArrayAdapter<String>? = null
    var hubNameMap = mutableMapOf<String, Int>()
    var hubNameArray = arrayListOf<String>()


    var aadhaarDetailsDataModel: AadhaarDetailsDataModel? = null
    private lateinit var viewBinding: JoiningFormFragmentBinding

    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null

    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = JoiningFormFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        changeStatusBarColor()
        listener()
        initViews()
        observer()
    }

    var serverHubData: HubServerDM? = null
    var hubCityFilled = false
    var hubNameFilled = false
    private fun observer() {
        viewModel.dojObserver.observe(viewLifecycleOwner,{
            it?.dateOfJoining?.let {
                viewBinding.dateOfJoining.setText(DateHelper.getDateInDDMMYYYYHiphen(it))
            }
        })
        viewModel.profileData.observe(viewLifecycleOwner, {
            setProfileRelatedDataToField(it)
        })
        viewModel.updatedResult.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val updated = it ?: return@Observer
            Log.d("updated", "up $updated")
            viewBinding.progressBar.visibility = View.GONE
            if (updated) {
                showToast("Data uploaded successfully")
                checkForNextDoc()
            } else {
                showToast("Data not uploaded successfully!! Try again!!")
            }

        })

        viewModel.hub_submitted_data.observe(viewLifecycleOwner, {
            serverHubData = it
            setHubState()
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

        viewModel.caCitiesResult.observe(viewLifecycleOwner, {
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

        viewModel.hub_states.observe(viewLifecycleOwner, {
            viewBinding.progressBar.gone()
            viewModel.loadHubData(mJobProfileId)   //need to uncomment
            hubStatesArray.clear()
            hubStatesArray.addAll(it.distinct())
            hubStatesArray.sort()
            hubStatesArray.forEachIndexed { index, data ->
                hubStatesMap.put(data, index)
            }
            hubStateAdapter?.notifyDataSetChanged()
        })

        viewModel.hub_cities.observe(viewLifecycleOwner, {
            viewBinding.progressBar.gone()
            hubCitiesArray.clear()
            hubCitiesArray.addAll(it.distinct())
            hubCitiesArray.forEachIndexed { index, data ->
                hubCitiesMap.put(data, index)
            }
            hubCitiesAdapter?.notifyDataSetChanged()
            if (!hubCityFilled) {
                hubCityFilled = true
                setHubCity()
            }
        })

        viewModel.hub_names.observe(viewLifecycleOwner, {
            viewBinding.progressBar.gone()
            hubNameArray.clear()
            hubNameArray.addAll(it.distinct())
            hubNameArray.forEachIndexed { index, data ->
                hubNameMap.put(data, index)
            }
            hubNameAdapter?.notifyDataSetChanged()
            if (!hubNameFilled) {
                hubNameFilled = true
                setHubName()
            }
        })

    }

    private fun setProfileRelatedDataToField(profileData: ProfileData?) {
        viewBinding.emailId.editText?.setText(profileData?.email)
        viewBinding.emergencyContact.editText?.setText(profileData?.emergencyContact)
        viewBinding.maritalStatusSpinner.setSelection(
            resources.getStringArray(R.array.marital_status).indexOf(profileData?.maritalStatus)
        )
    }

    private fun setHubName() {
        serverHubData?.hubName?.let {
            viewBinding.hubName.setText(it, false)
        }
    }

    private fun setHubState() {
        serverHubData?.stateName?.let {
            viewBinding.hubState.setText(it, false)
            viewModel.loadHubCities(it)
        }
    }

    private fun setHubCity() {
        serverHubData?.hubCity?.let {
            viewBinding.hubCity.setText(it, false)
            viewModel.loadHubNames(viewBinding.hubState.text.toString(), it)
        }
    }


    private fun processKycData(kycData: VerificationBaseModel) = viewBinding.apply {
        aadhaarDetailsDataModel = kycData.aadhaar_card_questionnaire
        //set the values to views
        kycData.aadhaar_card_questionnaire?.let {
            //set front image
            it.aadhaarCardNo.let {
                aadharNo.editText?.setText(it)
            }
            it.dateOfBirth.let {
                dateOfBirth.text = it
                dobLabel.visible()
            }
            it.fName.let {
                fatherName.editText?.setText(it)
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
                    getCitiesWhenStateNotEmpty(it, true)
                }
            }
            currentAddCheckbox.isChecked = it.currentAddSameAsParmanent
            if (!it.currentAddSameAsParmanent) {
                it.currentAddress?.let { curradd ->
                    caAddLine1Input.setText(curradd.addLine1)
                    caAddLine2Input.setText(curradd.addLine2)

                    if (curradd.state?.isNotBlank() == true) {
                        caStateSpinner.setText(curradd.state, false)
                        getCitiesWhenStateNotEmpty(curradd.state ?: "", false)
                    }
                }
            }
            it.city.let {
                citySpinner.setText(it, false)
            }

        }
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
            Log.d("index", "i: $index , map: ${stateModel?.id}")
        }
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
        viewModel.getVerificationData(userIdToUse.toString())
    }

    private fun initViews() {
        userIdToUse = if (userId != null) {
            userId
        }else{
            user?.uid
        }
        viewModel.getStates()
        viewModel.loadHubStates(mJobProfileId)
        viewModel.loadProfileData()
        viewModel.loadApplicationDataDOJ(mJobProfileId)
    }

    var selectedDOB: Date? = null
    private val dateOfBirthPicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDOB = newCal.time
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

    private val dateOfJoiningPicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.dateOfJoining.text = DateHelper.getDateInDDMMYYYYHiphen(newCal.time)
                viewBinding.dojLabel.visible()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun listener() = viewBinding.apply {
        dateOfBirthLabel.setOnClickListener {
            dateOfBirthPicker.show()
        }

        dateOfJoiningLabel.setOnClickListener {
            dateOfJoiningPicker.show()
        }

        appBar.apply {
            setBackButtonListener {
                activity?.onBackPressed()
            }
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

        // hub related onitemclick

        hubState.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 <= hubStatesArray.size && hubState.text.toString().isNotBlank()) {
                    val actualIndex = hubStatesMap.get(hubState.text.toString().trim())
                    hubCity.setText("", false)
                    hubName.setText("", false)
                    actualIndex?.let {
                        if (it >= 0) {
                            viewModel.loadHubCities(hubStatesArray.get(it))
                        }
                    }
                }
            }
        }

        hubCity.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 <= hubCitiesArray.size && hubCity.text.toString().isNotBlank()) {
                    val actualIndex = hubCitiesMap.get(hubCity.text.toString().trim())
                    hubName.setText("", false)
                    actualIndex?.let {
                        if (it >= 0) {
                            viewModel.loadHubNames(hubState.text.toString(), hubCitiesArray.get(it))
                        }
                    }
                }
            }
        }

        // hub related onitemclick end


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


        // Hub state city name
        //state
        hubStateAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                hubStatesArray
            )
        }
        hubState.setAdapter(hubStateAdapter)
        hubState.threshold = 1
        hubState.setOnFocusChangeListener { view, b ->
            if (b) {
                hubState.showDropDown()
            }
        }

        // city
        hubCitiesAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                hubCitiesArray
            )
        }
        hubCity.setAdapter(hubCitiesAdapter)
        hubCity.threshold = 1
        hubCity.setOnFocusChangeListener { view, b ->
            if (b) {
                hubCity.showDropDown()
            }
        }

        // name
        hubNameAdapter = context?.let { it1 ->
            ArrayAdapter(
                it1,
                android.R.layout.simple_spinner_dropdown_item,
                hubNameArray
            )
        }
        hubName.setAdapter(hubNameAdapter)
        hubName.threshold = 1
        hubName.setOnFocusChangeListener { view, b ->
            if (b) {
                hubName.showDropDown()
            }
        }







        fatherName.editText?.addTextChangedListener(ValidationTextWatcher())
        emailId.editText?.addTextChangedListener(ValidationTextWatcher())
        emergencyContact.editText?.addTextChangedListener(ValidationTextWatcher())
        aadharNo.editText?.addTextChangedListener(ValidationTextWatcher())
        addLine1.editText?.addTextChangedListener(ValidationTextWatcher())
        addLine2.editText?.addTextChangedListener(ValidationTextWatcher())
        stateSpinner.addTextChangedListener(ValidationTextWatcher())
        citySpinner.addTextChangedListener(ValidationTextWatcher())
        caAddLine1.editText?.addTextChangedListener(ValidationTextWatcher())
        caAddLine2.editText?.addTextChangedListener(ValidationTextWatcher())
        caStateSpinner.addTextChangedListener(ValidationTextWatcher())
        caCitySpinner.addTextChangedListener(ValidationTextWatcher())
        hubState.addTextChangedListener(ValidationTextWatcher())
        hubCity.addTextChangedListener(ValidationTextWatcher())
        hubName.addTextChangedListener(ValidationTextWatcher())


        submitButton.setOnClickListener {
            if (anyDataEntered) {

                if (fatherName.editText?.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter father name")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (dateOfBirth.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Select date of birth")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (dateOfJoining.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Select date of joining")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (maritalStatusSpinner.selectedItem.toString() == resources.getStringArray(R.array.marital_status)[0]) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Select marital status")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                if(emailId.editText?.text.toString().isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailId.editText?.text.toString()).matches() ){
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter a valid email")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
//                if (emailId.editText?.text.toString().isBlank() && emailId.editText?.text.toString().contains("@") && emailId.editText?.text.toString().contains(".") && emailId.editText?.text.toString().length>) {
//
//                }

                if (emergencyContact.editText?.text.toString()
                        .isBlank() || emergencyContact.editText?.text.toString().length != 10
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter emergency contact number")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (aadharNo.editText?.text.toString()
                        .isBlank() || aadharNo.editText?.text.toString().length != 12
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter valid aadhaar number")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }


                if (addLine1Input.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter Address Line 1")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (addLine2Input.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Enter Address Line 2")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (stateSpinner.text.toString()
                        .isEmpty() || !statesArray.contains(stateSpinner.text.toString())
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage(getString(R.string.select_aadhar_state))
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (citySpinner.text.toString()
                        .isEmpty() || !citiesArray.contains(citySpinner.text.toString())
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Select City")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                //current address validation
                if (!currentAddCheckbox.isChecked) {

                    if (caAddLine1Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_client))
                            .setMessage("Enter Current Address Line 1")
                            .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caAddLine2Input.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_client))
                            .setMessage("Enter Current Address Line 2")
                            .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caStateSpinner.text.toString()
                            .isEmpty() || !statesArray.contains(caStateSpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_client))
                            .setMessage(getString(R.string.select_aadhar_state))
                            .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }

                    if (caCitySpinner.text.toString()
                            .isEmpty() || !caCitiesArray.contains(caCitySpinner.text.toString())
                    ) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_client))
                            .setMessage("Select City")
                            .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                            .show()
                        return@setOnClickListener
                    }
                }

                if (hubState.text.isNullOrBlank() || !hubStatesArray.contains(hubState.text.toString())) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Please select hub state")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (hubCity.text.isNullOrBlank() || !hubCitiesArray.contains(hubCity.text.toString())) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Please select hub city")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (hubName.text.isNullOrBlank() || !hubNameArray.contains(hubName.text.toString())) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_client))
                        .setMessage("Please select hub name")
                        .setPositiveButton(getString(R.string.okay_client)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                submitData()
            } else {
                checkForNextDoc()
            }
        }

        currentAddCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
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
                    if (false)//need to work on this
                    {
                        viewBinding.submitButton.text =
                            resources.getString(R.string.skip_client)//"Skip"
                        anyDataEntered = false
                    } else {
                        viewBinding.submitButton.text =
                            resources.getString(R.string.submit_client)//"Submit"
                        anyDataEntered = true
                    }

                }
            }
        }
    }

    private fun changeStatusBarColor() {
        var win: Window? = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        win?.statusBarColor = resources.getColor(R.color.stateBarColor)
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {

            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
            userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
            userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
        }
    }


    private fun submitData() = viewBinding.apply {

        var submitDataModel = AadhaarDetailsDataModel(
            aadhaarCardNo = aadharNo.editText?.text.toString(),
            dateOfBirth = dateOfBirth.text.toString(),
            fName = fatherName.editText?.text.toString(),
            addLine1 = addLine1Input.text.toString(),
            addLine2 = addLine2Input.text.toString(),
            state = stateSpinner.text.toString(),
            city = citySpinner.text.toString(),
            currentAddSameAsParmanent = currentAddCheckbox.isChecked,
            currentAddress = if (!currentAddCheckbox.isChecked) CurrentAddressDetailDataModel(
                addLine1 = caAddLine1Input.text.toString(),
                addLine2 = caAddLine2Input.text.toString(),
                state = caStateSpinner.text.toString(),
                city = caCitySpinner.text.toString(),
            ) else null
        )
        var dob = selectedDOB ?: DateHelper.getDateFromDDMMYYYY(dateOfBirth.text.toString())
        var doj = DateHelper.getDateFromDDMMYYYY(dateOfJoining.text.toString())

        doj?.let { dojObj ->
            dob?.let {
                progressBar.visibility = View.VISIBLE
                viewModel.submitJoiningFormData(
                    submitDataModel,
                    mJobProfileId,
                    hubState.text.toString(),
                    hubCity.text.toString(),
                    hubName.text.toString(),
                    emailId.editText?.text.toString(),
                    it,
                    fatherName.editText?.text.toString(),
                    maritalStatusSpinner.selectedItem.toString(),
                    emergencyContact.editText?.text.toString(),
                    dojObj
                )
            }
        }
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
        }
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
}