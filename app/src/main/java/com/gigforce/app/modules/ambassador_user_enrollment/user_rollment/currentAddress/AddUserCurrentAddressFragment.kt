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
import com.gigforce.app.core.selectChipWithText
import com.gigforce.app.core.selectItemWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.models.City
import com.gigforce.app.modules.ambassador_user_enrollment.models.State
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_user_current_address.*
import kotlinx.android.synthetic.main.fragment_user_current_address_main.*

class AddUserCurrentAddressFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_user_current_address, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let

            val pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
            pin_code_et.setText(pincode)
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    private fun initListeners() {
        pin_code_et.textChanged {
            pin_code_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00
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
                }
            }

            private fun filterCitiesByStateAndSetOnCities(id: String) {
                val cities = viewModel.cities.filter {
                    it.stateCode == id
                }.toMutableList().apply {
                    add(0, City(name = "Select District"))
                }

                val cityAdapter: ArrayAdapter<City> =
                        ArrayAdapter(
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
            showAlertDialog("Invalid Pincode", "Provide a valid Pin Code")
            return
        }

        if (address_line_1_et.text.isBlank()) {
            showAlertDialog("Provide Address Line 1", "Please provide address line 1")
            return
        }

        if (address_line_2_et.text.isBlank()) {
            showAlertDialog("Provide Address Line 2", "Please provide address line 2")
            return
        }

        if (state_spinner.childCount == 0 || state_spinner.selectedItemPosition == 0) {
            showAlertDialog("Provide State", "Please select state name")
            return
        }

        if (city_spinner.childCount == 0 || city_spinner.selectedItemPosition == 0) {
            showAlertDialog("Provide City", "Please select district name")
            return
        }

        if (userId != null) {

            if (ready_to_change_location_chipgroup.checkedChipId == -1) {
                showAlertDialog("", "Select if you want to change your location")
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

    private fun initViewModel() {

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

        viewModel.getProfileForUser(userId)

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
        content.address.current.apply {
            pin_code_et.setText(pincode)
            address_line_1_et.setText(this.firstLine)
            address_line_2_et.setText(this.secondLine)

            state_spinner.selectItemWithText(this.state)
            city_spinner.selectItemWithText(this.city)
            arround_current_add_seekbar.progress = this.preferred_distance
        }

        if (content.isCurrentAddressAndPermanentAddressTheSame()) {
            localite_migrant_chipgroup.check(R.id.migrant_no)
            permanent_address_layout.gone()

            how_do_you_came_to_know_label.gone()
            how_do_you_came_to_know_chipgroup.gone()

            ready_to_change_location_label.visible()
            ready_to_change_location_chipgroup.visible()

            if(content.readyToChangeLocationForWork){
                ready_to_change_location_chipgroup.check(R.id.chip_location_change_yes)
            } else {
                ready_to_change_location_chipgroup.check(R.id.chip_location_change_no)
            }
        } else {
            localite_migrant_chipgroup.check(R.id.migrant_yes)
            permanent_address_layout.visible()

            permanent_state_spinner.selectItemWithText(content.address.home.state)
            permanent_city_spinner.selectItemWithText(content.address.home.city)

            ready_to_change_location_label.gone()
            ready_to_change_location_chipgroup.gone()

            how_do_you_came_to_know_label.visible()
            how_do_you_came_to_know_chipgroup.visible()

            if (content.howYouCameToKnowAboutCurrentJob != null) {
                how_do_you_came_to_know_chipgroup.selectChipWithText(content.howYouCameToKnowAboutCurrentJob!!)
            }
        }
    }

    private fun populateStateAndCitySpinner() {
        val states = viewModel.states.toMutableList().apply {
            add(0, State(name = "Select State"))
        }

        val adapter: ArrayAdapter<State> =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        state_spinner.adapter = adapter

        val cities = viewModel.cities.toMutableList().apply {
            add(0, City(name = "Select district"))
        }
        val cityAdapter: ArrayAdapter<City> =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        city_spinner.adapter = cityAdapter
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
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }
}