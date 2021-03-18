package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.currentAddress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.ambassador.PostalOffice
import com.gigforce.core.datamodels.State
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_user_current_address.*
import java.util.*

class AddCurrentAddressFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private var userId: String? = null
    private var userName: String? = null
    private var cameFromEnrollment = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_current_address, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        setUpUiForAmbOrUser()
        initListeners()
        initViewModel()
    }

    private fun setUpUiForAmbOrUser() {
        if (userId == null) {
            //Amb
            breifing_layout.gone()
            ready_to_change_location_chipgroup.gone()
            ready_to_change_location_label.gone()
            arround_current_add_seekbar.gone()
            pref_distance_label.gone()
            seekbardependent.gone()
            maxDistanceTV.gone()
            minDistanceTV.gone()
            toolbar_text.text = getString(R.string.add_current_address)
        } else {
            breifing_layout.visible()
            ready_to_change_location_chipgroup.visible()
            ready_to_change_location_label.visible()
            arround_current_add_seekbar.visible()
            pref_distance_label.visible()
            seekbardependent.visible()
            maxDistanceTV.visible()
            minDistanceTV.visible()
            toolbar_text.text = getString(R.string.user_local_address)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            cameFromEnrollment = it.getBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, false)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let

            val pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
            pin_code_et.setText(pincode)
        }

        savedInstanceState?.let {
            cameFromEnrollment = it.getBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, false)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
        outState.putBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, cameFromEnrollment)

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

        arround_current_add_seekbar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                        (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekbardependent.text = progress.toString() + " " + getString(R.string.km)
                seekbardependent.setX(seekBar.getX() + value + seekBar.getThumbOffset() / 2)
                //textView.setY(100); just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

        ic_back_iv.setOnClickListener {
            if (userId == null) {
                if (cameFromEnrollment) {
                    onBackPressed()
                    return@setOnClickListener
                }
                activity?.onBackPressed()
            } else {
                showGoBackConfirmationDialog()
            }
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

            private fun filterCitiesByStateAndSetOnCities(id: String) {
                val cities = viewModel.cities.filter {
                    it.stateCode == id
                }.toMutableList().apply {
                    add(0,
                        City(name = getString(R.string.select_district))
                    )
                }

                val cityAdapter: ArrayAdapter<City> =
                        ArrayAdapter<City>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                cities
                        )
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                city_spinner.adapter = cityAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun validateDataAndSubmit() {
        if (pin_code_et.text.isNotBlank() && pin_code_et.text.toString().toInt() < 10_00_00) {
            showAlertDialog(
                    getString(R.string.invalid_pincode),
                    getString(R.string.provide_valid_pincode)
            )
            return
        }

        if (address_line_1_et.text.isBlank()) {
            showAlertDialog(
                    getString(R.string.provide_address_line_1),
                    getString(R.string.please_provide_address_line_1)
            )
            return
        }

        if (address_line_2_et.text.isBlank()) {
            showAlertDialog(
                    getString(R.string.provide_address_line_2),
                    getString(R.string.please_provide_address_line)
            )
            return
        }

        if (state_spinner.childCount == 0 || state_spinner.selectedItemPosition == 0) {
            showAlertDialog(
                    getString(R.string.provide_state),
                    getString(R.string.please_select_state_name)
            )
            return
        }

        if (city_spinner.childCount == 0 || city_spinner.selectedItemPosition == 0) {
            showAlertDialog(
                    getString(R.string.provide_city),
                    getString(R.string.please_select_district_name)
            )
            return
        }

        if (userId != null) {

            if (ready_to_change_location_chipgroup.checkedChipId == -1) {
                showAlertDialog("", getString(R.string.change_your_location))
                return
            }
        }

        val state = (state_spinner.selectedItem as State).name
        val city = (city_spinner.selectedItem as City).name

        var progress = arround_current_add_seekbar.progress
        if (progress < 5) progress = 5

        viewModel.updateUserCurrentAddressDetails(
                uid = userId,
                pinCode = pin_code_et.text.toString(),
                addressLine1 = address_line_1_et.text.toString(),
                addressLine2 = address_line_2_et.text.toString(),
                state = state,
                city = city,
                preferredDistanceInKm = progress,
                readyToChangeLocationForWork = ready_to_change_location_chipgroup.checkedChipId == R.id.chip_location_change_yes
        )
    }

    var allPostoffices = ArrayList<PostalOffice>()
    private fun initViewModel() {
        viewModel.submitUserDetailsState
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lse.Loading -> {
                            // UtilMethods.showLoading(requireContext())
                        }
                        Lse.Success -> {
                            // UtilMethods.hideLoading()

                            if (userId == null) {
                                showToast(getString(R.string.current_address_details_sub))
                                popBackState()
                            } else {
                                showToast(getString(R.string.user_current_address_details_sub))
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
                            showAlertDialog(getString(R.string.could_not_submit_address_info), it.error)

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
                    showToast(getString(R.string.unable_to_load_cities_and_states))
                }
            }
        }
        )
        viewModel.loadCityAndStates()

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
                                for (index in 0..state_spinner.adapter.count - 1) {
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

    }

    private fun populateStateAndCitySpinner() {
        val states = viewModel.states.toMutableList().apply {
            add(0,
                State(name = getString(R.string.select_state))
            )
        }

        val adapter: ArrayAdapter<State> =
                ArrayAdapter<State>(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        state_spinner.adapter = adapter

        val cities = viewModel.cities.toMutableList().apply {
            add(0,
                City(name = getString(R.string.select_district))
            )
        }
        val cityAdapter: ArrayAdapter<City> =
                ArrayAdapter<City>(requireContext(), android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        city_spinner.adapter = cityAdapter
    }

    override fun onBackPressed(): Boolean {
        if (cameFromEnrollment) {
            navFragmentsData?.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            popBackState()
            return true
        }

        if (userId == null) {
            return false
        } else {
            showGoBackConfirmationDialog()
            return true
        }
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(getString(R.string.are_u_sure_u_want_to_go_back))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> goBackToUsersList() }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {
        if (cameFromEnrollment) {
            onBackPressed()
            return
        }
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
                .show()
    }
}