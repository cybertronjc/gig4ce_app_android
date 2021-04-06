package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.currentAddress

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.selectItemWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.models.City
import com.gigforce.app.modules.ambassador_user_enrollment.models.PostalOffice
import com.gigforce.app.modules.ambassador_user_enrollment.models.State
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_user_current_address.*
import kotlinx.android.synthetic.main.fragment_user_current_address_main.*
import java.util.*

class AddUserCurrentAddressFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String
    var allPostoffices = ArrayList<PostalOffice>()

    private var mode: Int = EnrollmentConstants.MODE_ADD
    private var profileData: ProfileData? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_user_current_address, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
        getProfileForUser()
    }

    private fun getProfileForUser() {
        if (mode == EnrollmentConstants.MODE_EDIT) {
            viewModel.getProfileForUser(userId)
            skip_btn.visible()
        } else {
            skip_btn.gone()
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let

            val pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
            pin_code_et.setText(pincode)
        }

        savedInstanceState?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
    }

    private fun initListeners() {
        pin_code_et.textChanged {
            pin_code_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00
            if (pin_code_okay_iv.isVisible) {
                state_spinner.setSelection(0)
                city_spinner.setSelection(0)
                viewModel.loadCityAndStateUsingPincode(it.toString())
            }
        }

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

        toolbar_layout.showTitle("User Local Address")
        toolbar_layout.hideActionMenu()
        toolbar_layout.setBackButtonListener {
            showGoBackConfirmationDialog()
        }

        state_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {

                if (state_spinner.childCount != 0 && state_spinner.selectedItemPosition != 0) {
                    val state = state_spinner.selectedItem as State
                    filterCitiesByStateAndSetOnCities(state.id)
                    for (index in 0..city_spinner.adapter.count - 1) {
                        val item = city_spinner.adapter.getItem(index)
                        allPostoffices.mapIndexed { index1, postalOffice ->
                            if (item.toString().equals(postalOffice.district)) {
                                city_spinner.setSelection(index)
                                return
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        permanent_state_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {

                if (permanent_state_spinner.childCount != 0 && permanent_state_spinner.selectedItemPosition != 0) {
                    val state = permanent_state_spinner.selectedItem as State
                    filterPermanentCitiesByStateAndSetOnCities(state.id)
                }
            }

            private fun filterPermanentCitiesByStateAndSetOnCities(id: String) {
                val cities = viewModel.cities.filter {
                    it.stateCode == id
                }.sortedBy {
                    it.name
                }.toMutableList().apply {
                    add(0, City(name = "Select District"))
                }

                val permanentCityAdapter: ArrayAdapter<City> =
                        ArrayAdapter(
                                requireContext(),
                                R.layout.layout_spinner_item,
                                cities
                        )
                permanentCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                permanent_city_spinner.adapter = permanentCityAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        localite_migrant_chipgroup.setOnCheckedChangeListener { group, checkedId ->

            if (checkedId == R.id.migrant_no) {
                permanent_address_layout.gone()

            } else {
                permanent_address_layout.visible()

            }
        }

        skip_btn.setOnClickListener {

            navigate(
                    R.id.addUserBankDetailsInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )
        }
    }

    private fun validateDataAndSubmit() {
//        if (pin_code_et.text.isBlank() || pin_code_et.text.toString().toInt() < 10_00_00) {
//            pin_code_error.visible()
//            pin_code_error.text = "Provide a valid Pin Code"
//            return
//        } else {
//            pin_code_error.gone()
//            pin_code_error.text = null
//        }
//
//        if (address_line_1_et.text.isBlank()) {
//            address_line_1_error.visible()
//            address_line_1_error.text = "Please provide House No/ Street No"
//            return
//        } else {
//            address_line_1_error.gone()
//            address_line_1_error.text = null
//        }
//
//        if (address_line_2_et.text.isBlank()) {
//            address_line_2_error.visible()
//            address_line_2_error.text = "Please provide Area / Village / Town"
//            return
//        } else {
//            address_line_2_error.gone()
//            address_line_2_error.text = null
//        }

        if (state_spinner.childCount == 0 || state_spinner.selectedItemPosition == 0) {
            state_error.visible()
            state_error.text = "Select state name"
            return
        } else {
            state_error.gone()
            state_error.text = null
        }

        if (city_spinner.childCount == 0 || city_spinner.selectedItemPosition == 0) {

            city_error.visible()
            city_error.text = "Select district name"
            return
        } else {
            city_error.gone()
            city_error.text = null
        }

        if (permanent_address_layout.isVisible && (permanent_state_spinner.childCount == 0 || permanent_state_spinner.selectedItemPosition == 0)) {
            permanent_state_error.visible()
            permanent_state_error.text = "Select state name"
            return
        } else {
            permanent_state_error.gone()
            permanent_state_error.text = null
        }

        if (permanent_address_layout.isVisible && (permanent_city_spinner.childCount == 0 || permanent_city_spinner.selectedItemPosition == 0)) {
            permanent_city_error.visible()
            permanent_city_error.text = "Select state name"
            return
        } else {
            permanent_city_error.gone()
            permanent_city_error.text = null
        }


        val state = (state_spinner.selectedItem as State).name
        val city = (city_spinner.selectedItem as City).name

        var homeCity = ""
        var homeState = ""

        if (permanent_address_layout.isVisible) {
            homeCity = (permanent_city_spinner.selectedItem as City).name
            homeState = (permanent_state_spinner.selectedItem as State).name
        } else {
            homeState = state
            homeCity = city
        }


        viewModel.updateUserCurrentAddressDetails(
                uid = userId,
                pinCode = pin_code_et.text.toString(),
                addressLine1 = address_line_1_et.text.toString(),
                addressLine2 = address_line_2_et.text.toString(),
                state = state,
                city = city,
                homeCity = homeCity,
                homeState = homeState
        )
    }

    private fun initViewModel() {

        viewModel.pincodeResponse
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> {
                            UtilMethods.showLoading(requireContext())
//                        allPostoffices.clear()
                        }
                        is Lce.Content -> {
                            UtilMethods.hideLoading()
                            if (it.content.status.equals("Success")) {
                                allPostoffices = it.content.postOffice
                                for (index in 0 until state_spinner.adapter.count) {
                                    val item = state_spinner.adapter.getItem(index)
                                    allPostoffices.mapIndexed { index1, postalOffice ->
                                        if (item.toString().equals(postalOffice.state)) {
                                            state_spinner.setSelection(index)
                                            return@Observer
                                        }
                                    }
                                }
                            } else {
                                state_spinner.setSelection(0)
                                city_spinner.setSelection(0)
                            }
                        }
                        is Lce.Error -> {
                            UtilMethods.hideLoading()
                            showAlertDialog("", it.error)
//                        allPostoffices.clear()
                        }
                    }
                })

        viewModel.profile
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> {
                            //Show init loading state
                            user_address_main_layout.gone()
                            address_error.gone()
                            loading_user_address.visible()
                        }
                        is Lce.Content -> {
                            loading_user_address.gone()
                            address_error.gone()
                            user_address_main_layout.visible()

                            setUserDataOnView(it.content)
                        }
                        is Lce.Error -> {
                            user_address_main_layout.gone()
                            loading_user_address.gone()
                            address_error.visible()

                            address_error.text = it.error
                        }
                    }
                })



        viewModel.submitUserDetailsState
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lse.Loading -> {
                            // UtilMethods.showLoading(requireContext())
                        }
                        Lse.Success -> {
                            // UtilMethods.hideLoading()

                            if (userId == null) {
                                showToast("Current Address Details submitted")
                                activity?.onBackPressed()
                            } else {
                                showToast("User Current Address Details submitted")
                                navigate(
                                        R.id.addUserBankDetailsInfoFragment, bundleOf(
                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
                                )
                                )
                            }
                        }
                        is Lse.Error -> {
                            //  UtilMethods.hideLoading()
                            showAlertDialog("Could not submit address info", it.error)
                        }
                    }
                })

        viewModel.citiesAndStateLoadState.observe(
                viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    UtilMethods.showLoading(requireContext())
                }
                Lse.Success -> {
                    UtilMethods.hideLoading()

                    populateStateAndCitySpinner()
                }
                is Lse.Error -> {
                    UtilMethods.hideLoading()
                    showToast("Unable to load cities and states")
                }
            }
        }
        )
        viewModel.loadCityAndStates()

    }

    private fun setUserDataOnView(content: ProfileData) {
        profileData = content

        content.address.current.apply {
            pin_code_et.setText(pincode)
            address_line_1_et.setText(this.firstLine)
            address_line_2_et.setText(this.area)

            state_spinner.selectItemWithText(this.state)
            city_spinner.selectItemWithText(this.city)
        }

        if (content.address.isCurrentAddressAndPermanentAddressTheSame()) {
            localite_migrant_chipgroup.check(R.id.migrant_no)
            permanent_address_layout.gone()

        } else {
            localite_migrant_chipgroup.check(R.id.migrant_yes)
            permanent_address_layout.visible()

            permanent_state_spinner.selectItemWithText(content.address.home.state)
            permanent_city_spinner.selectItemWithText(content.address.home.city)
        }
    }

    private fun populateStateAndCitySpinner() {
        val states = viewModel
                .states
                .sortedWith(compareBy { it.name })
                .toMutableList().apply {
                    add(0, State(name = "Select State"))
                }

        val adapter: ArrayAdapter<State> =
                ArrayAdapter(requireContext(), R.layout.layout_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        state_spinner.adapter = adapter
        permanent_state_spinner.adapter = adapter

        val cities = viewModel.cities.sortedWith(compareBy { it.name }).toMutableList().apply {
            add(0, City(name = "Select District"))
        }
        val cityAdapter: ArrayAdapter<City> =
                ArrayAdapter(requireContext(), R.layout.layout_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        city_spinner.adapter = cityAdapter
        permanent_city_spinner.adapter = cityAdapter

        if (profileData != null) {
            state_spinner.selectItemWithText(profileData!!.address.current.state)
            permanent_state_spinner.selectItemWithText(profileData!!.address.home.state)

            Handler().postDelayed({
                permanent_city_spinner.selectItemWithText(profileData!!.address.home.city)
                city_spinner.selectItemWithText(profileData!!.address.current.city)
            }, 500)
        }
    }

    override fun onBackPressed(): Boolean {

        showGoBackConfirmationDialog()
        return true
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to go back")
                .setPositiveButton("Yes") { _, _ -> goBackToUsersList() }
                .setNegativeButton("No") { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {
        findNavController().navigateUp()
//        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun filterCitiesByStateAndSetOnCities(id: String) {
        val cities = viewModel.cities.filter {
            it.stateCode == id
        }.sortedBy {
            it.name
        }.toMutableList().apply {
            add(0, City(name = getString(R.string.select_district)))
        }

        val cityAdapter: ArrayAdapter<City> =
                ArrayAdapter<City>(
                        requireContext(),
                        R.layout.layout_spinner_item,
                        cities
                )
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        city_spinner.adapter = cityAdapter
    }


}