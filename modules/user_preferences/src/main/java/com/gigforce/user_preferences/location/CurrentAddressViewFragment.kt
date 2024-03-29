package com.gigforce.user_preferences.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.navigation.INavigation
import com.gigforce.user_preferences.R
import com.gigforce.common_ui.viewmodels.userpreferences.SharedPreferenceViewModel
import com.gigforce.core.datamodels.user_preferences.PreferencesDataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.current_address_view_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrentAddressViewFragment: Fragment() {
    companion object {
        fun newInstance() = LocationFragment()
        const val INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT = "user_came_from_amb_screen"
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferenceDataModel: PreferencesDataModel
    private lateinit var profileDataModel: ProfileData
    private var didUserCameFromAmbassadorScreen = false
    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.current_address_view_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
        viewModel.getAllData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments,savedInstanceState)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            didUserCameFromAmbassadorScreen = it.getBoolean(INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT)
        }

        savedInstanceState?.let {
            didUserCameFromAmbassadorScreen = it.getBoolean(INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, didUserCameFromAmbassadorScreen)
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })

    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileData ->
            viewModel.setProfileDataModel(profileData)
            initializeViews()
        })

    }

    private fun initializeViews() {
        preferenceDataModel = viewModel.getPreferenceDataModel()
        profileDataModel = viewModel.getProfileDataModel()
        back_arrow_iv.setOnClickListener { activity?.onBackPressed() }
        var currentAddress = profileDataModel.address.current
        var permanentAddress =  profileDataModel.address.home
        populateAddress(currentAddress)
        workFromHomeSwitch.isEnabled = !permanentAddress.isEmpty()
        workFromHomeSwitch.isChecked = currentAddress.isSame(permanentAddress)
    }

    private fun populateAddress(address: AddressModel){
        line1view.text = address.firstLine
        line2view.text = address.secondLine
        areaview.text = address.area
        cityview.text = address.city
        stateview.text = address.state
        pincodeview.text = address.pincode

    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS",address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address_pref)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
        editCurrentLocation.setOnClickListener {
            navigation.navigateTo("preferences/currentAddressEditFragment")
//            navigate(R.id.currentAddressEditFragment)
        }

    }

}