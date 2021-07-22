package com.gigforce.ambassador.user_rollment.currentAddress

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationConstants
import com.gigforce.ambassador.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.AppConstants
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.ambassador.PostalOffice
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.selectItemWithText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_user_current_address.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddCurrentAddressFragment : Fragment(),IOnBackPressedOverride {

    private val viewModel: UserDetailsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var userId: String? = null
    private var userName: String? = null
    private var cameFromEnrollment = false

    @Inject lateinit var navigation : INavigation
    private var navFragmentsData : NavFragmentsData?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_user_current_address, container, false)

    var navigationsForBundle = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navFragmentsData = activity as NavFragmentsData
        getDataFromIntents(arguments, savedInstanceState)
        initializeNavigations()
        setUpUiForAmbOrUser()
        initListeners()
        initViewModel()
    }
    private fun initializeNavigations() {
        navigationsForBundle.add("userinfo/addUserPanCardInfoFragment")
        navigationsForBundle.add("userinfo/addUserDrivingLicenseInfoFragment")
        navigationsForBundle.add("userinfo/addUserAadharCardInfoFragment")
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
        } else {
            breifing_layout.visible()
            ready_to_change_location_chipgroup.visible()
            ready_to_change_location_label.visible()
            arround_current_add_seekbar.visible()
            pref_distance_label.visible()
            seekbardependent.visible()
            maxDistanceTV.visible()
            minDistanceTV.visible()
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            cameFromEnrollment = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
                false
            )
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let

            val pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
            pin_code_et.setText(pincode)
        }

        savedInstanceState?.let {
            cameFromEnrollment = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
                false
            )
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
        outState.putBoolean(
            AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
            cameFromEnrollment
        )

    }

    private fun initListeners() {

        toolbar_layout.apply {

            showTitle(getString(R.string.user_local_address))
            hideActionMenu()
            setBackButtonListener(View.OnClickListener {
                if (userId == null) {
                    if (cameFromEnrollment) {
                        onBackPressed()
                        return@OnClickListener
                    }
                    activity?.onBackPressed()
                } else {
                    showGoBackConfirmationDialog()
                }
            })
//            setBackButtonListener {
//
//                if (userId == null) {
//                    if (cameFromEnrollment) {
//                        onBackPressed()
//                        return@setBackButtonListener
//                    }
//                    activity?.onBackPressed()
//                } else {
//                    showGoBackConfirmationDialog()
//                }
//            }
        }
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
                    (progress * (seekBar.width - 2 * seekBar.thumbOffset)) / seekBar.max
                seekbardependent.text = progress.toString() + " " + getString(R.string.km)
                seekbardependent.x = seekBar.x + value + seekBar.thumbOffset / 2
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
                    add(0, City(name = getString(R.string.select_district)))
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

        viewModel.updateUserCurrentAddressDetails(
            uid = userId,
            pinCode = pin_code_et.text.toString(),
            addressLine1 = address_line_1_et.text.toString(),
            addressLine2 = address_line_2_et.text.toString(),
            state = state,
            city = city
        )
    }

    var allPostoffices = ArrayList<PostalOffice>()
    private fun initViewModel() {
        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?:return@Observer
                when (it) {
                    Lse.Loading -> {
                        // UtilMethods.showLoading(requireContext())
                    }
                    Lse.Success -> {
                        // UtilMethods.hideLoading()

                        if (userId == null) {
                            showToast(getString(R.string.current_address_details_sub))
                            navigation.popBackStack()
                        } else {
                            showToast(getString(R.string.user_current_address_details_sub))
                            navigation.navigateTo("userinfo/addUserBankDetailsInfoFragment",bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle
                            ))
//                            navigate(
//                                R.id.addUserBankDetailsInfoFragment, bundleOf(
//                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
//                                )
//                            )
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
                            selectDataOnStateSpinner()
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

        profileViewModel.getProfileData()
            .observe(viewLifecycleOwner, Observer {

                if (!it.address.current.isEmpty()) {
                    pin_code_et.setText(it.address.current.pincode)
                    address_line_1_et.setText(it.address.current.firstLine)
                    address_line_2_et.setText(it.address.current.secondLine)
                    state_spinner.selectItemWithText(it.address.current.state)

                    Handler().postDelayed({
                        city_spinner?.selectItemWithText(it.address.current.city)
                    }, 100)
                }

            })

    }

    private fun selectDataOnStateSpinner() {
        try {

            for (index in 0..state_spinner.adapter.count - 1) {
                val item = state_spinner.adapter.getItem(index)
                allPostoffices.mapIndexed { index1, postalOffice ->
                    if (item.toString().equals(postalOffice.state)) {
                        state_spinner.setSelection(index)
                        return
                    }
                }
            }
        } catch (e:Exception){

        }
    }

    private fun populateStateAndCitySpinner() {
        val states = viewModel.states.toMutableList().apply {
            add(0, State(name = getString(R.string.select_state)))
        }

        val adapter: ArrayAdapter<State> =
            ArrayAdapter<State>(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        state_spinner.adapter = adapter

        val cities = viewModel.cities.toMutableList().apply {
            add(0, City(name = getString(R.string.select_district)))
        }
        val cityAdapter: ArrayAdapter<City> =
            ArrayAdapter<City>(requireContext(), android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        city_spinner.adapter = cityAdapter
    }

    override fun onBackPressed(): Boolean {
        if (cameFromEnrollment) {
            navFragmentsData?.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            navigation.popBackStack()
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
        navigation.popBackStack("ambassador/users_enrolled",inclusive = false)
//        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
            .show()
    }
}