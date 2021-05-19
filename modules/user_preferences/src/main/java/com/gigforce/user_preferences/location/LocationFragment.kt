package com.gigforce.user_preferences.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.navigation.INavigation
import com.gigforce.user_preferences.R
import com.gigforce.user_preferences.SharedPreferenceViewModel
import com.gigforce.user_preferences.prefdatamodel.PreferencesDataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.location_settings_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationFragment : Fragment() {
    companion object {
        fun newInstance() = LocationFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferenceDataModel: PreferencesDataModel
    private lateinit var profileDataModel: ProfileData
    @Inject lateinit var navigation : INavigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.location_settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
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
        workFromHomeSwitch.isChecked = preferenceDataModel.isWorkFromHome
        textView79.text = convertAddressToString(viewModel.getPermanentAddress())
        textView81.text = convertAddressToString(viewModel.getCurrentAddress())
        if (viewModel.getCurrentAddress()!!.isEmpty()) {
            arroundCurrentAddSwitch.isEnabled = false
            preferredDistanceTV.text = getString(R.string.add_current_address)
        } else {
            arroundCurrentAddSwitch.isEnabled = true
            preferredDistanceTV.text =
                viewModel.getCurrentAddress()?.preferred_distance.toString() + " " + getString(
                    R.string.km_around
                )
        }
        arroundCurrentAddSwitch.isChecked = viewModel.getCurrentAddress()?.preferredDistanceActive!!
    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS", address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
        back_arrow_iv.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        imageview_plus.setOnClickListener(View.OnClickListener {
            if (viewModel.getCurrentAddress()!!.isEmpty()) {
                navigation.navigateTo("preferences/preferredLocationFragment")
                navigation.navigateTo("preferences/currentAddressEditFragment")
//                navigate(R.id.preferredLocationFragment)
//                navigate(R.id.currentAddressEditFragment)
            } else navigation.navigateTo("preferences/preferredLocationFragment") //navigate(R.id.preferredLocationFragment)
        })
        permanentAddLayout.setOnClickListener(View.OnClickListener {
            if (profileDataModel.address.home.isEmpty()) navigation.navigateTo("preferences/permanentAddressEditFragment") //navigate(R.id.permanentAddressEditFragment)
            else navigation.navigateTo("preferences/permanentAddressViewFragment") //navigate(R.id.permanentAddressViewFragment)
        })
        currentAddLayout.setOnClickListener(View.OnClickListener {
            if (profileDataModel.address.current.isEmpty()) navigation.navigateTo("preferences/currentAddressEditFragment") //navigate(R.id.currentAddressEditFragment)
            else navigation.navigateTo("preferences/currentAddressViewFragment") //navigate(R.id.currentAddressViewFragment)
        })
        workFromHomeSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if (viewModel.getCurrentAddress()!!.isEmpty()) {
                navigation.navigateTo("preferences/currentAddressEditFragment") //navigate(R.id.currentAddressEditFragment)
            } else
                viewModel.setWorkFromHome(isChecked)
        }
        arroundCurrentAddressLayout.setOnClickListener { view ->
            if (!viewModel.getCurrentAddress()!!.isEmpty()) {
                navigation.navigateTo("preferences/arrountCurrentAddress") //navigate(R.id.arrountCurrentAddress)
            } else {
                showToast(getString(R.string.please_add_current_address))
            }
        }
        arroundCurrentAddSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if (viewModel.getCurrentAddress()?.preferred_distance != 0)
                viewModel.setCurrentAddressPreferredDistanceActive(isChecked)
            else
                navigation.navigateTo("preferences/arrountCurrentAddress")
//                navigate(R.id.arrountCurrentAddress)
        }
    }

}